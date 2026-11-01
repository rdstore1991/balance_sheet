package com.mfstracker.app.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.mfstracker.app.data.local.TrackerRecordEntity
import com.mfstracker.app.util.NumberFormatUtils.formatCsvAmount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

data class ExportResult(
    val fileName: String,
    val displayLocation: String,
    val uri: Uri? = null
)

object CsvExporter {

    private const val EXPORT_SUBFOLDER = "MFS_Tracker"
    private const val MIME_CSV = "text/csv"
    private val UTF8_BOM = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())

    /**
     * Generates a structured RFC-4180 CSV report covering Part 1 (MFS Balances),
     * Part 2 (Expense & Income Log), and Part 3 (Reconciliation & MSA Summary).
     */
    fun buildCsvContent(record: TrackerRecordEntity): String {
        val sb = StringBuilder()

        // Header block
        sb.appendRow("MFS FUND & EXPENSE TRACKER")
        sb.appendRow("Date", record.dateString, "Time", record.timeString)
        sb.appendLine()

        // Part 1: MFS Account Balances
        sb.appendRow("PART 1: I. MFS ACCOUNT BALANCES")
        sb.appendRow("SL#", "Account Number", "MFS Provider", "Current Balance (BDT)")
        for (item in record.mfsBalances) {
            // Prefix account number with tab/quote safe text or standard CSV quoting
            sb.appendRow(
                item.slNo.toString(),
                "=\"${item.accountNumber}\"",
                item.provider,
                item.balance.formatCsvAmount()
            )
        }
        sb.appendRow("TOTAL MFS BALANCES", "", "", record.totalMfsBalance.formatCsvAmount())
        sb.appendLine()

        // Part 2: Expense & Income Log
        sb.appendRow("PART 2: EXPENSE / INCOME LOG")
        sb.appendRow("Row#", "Type", "Description", "Amount (BDT)", "Notes")
        val nonEmptyRows = record.transactions.filter {
            it.description.isNotBlank() || it.amount != 0.0 || it.notes.isNotBlank()
        }
        if (nonEmptyRows.isEmpty()) {
            sb.appendRow("-", "None", "", "0.00", "")
        } else {
            nonEmptyRows.forEachIndexed { index, tx ->
                sb.appendRow(
                    (index + 1).toString(),
                    tx.type.label,
                    tx.description,
                    tx.amount.formatCsvAmount(),
                    tx.notes
                )
            }
        }
        sb.appendRow("TOTAL DAILY EXPENSES", "", "", record.totalDailyExpenses.formatCsvAmount(), "")
        sb.appendRow("TOTAL DAILY INCOME", "", "", record.totalDailyIncome.formatCsvAmount(), "")
        sb.appendLine()

        // Part 3: Reconciliation & MSA Summary
        sb.appendRow("PART 3: RECONCILIATION & MSA SUMMARY")
        sb.appendRow("Field", "Amount (BDT)")
        sb.appendRow("Available Balance", record.availableBalance.formatCsvAmount())
        sb.appendRow("Previous Balance", record.previousBalance.formatCsvAmount())
        sb.appendRow("MSA", record.msa.formatCsvAmount())
        sb.appendRow("Previous MSA", record.previousMsa.formatCsvAmount())

        return sb.toString()
    }

    /**
     * Exports the record to public external storage:
     * `Documents/MFS_Tracker/MFS_Tracker_YYYY-MM-DD_HHmm.csv`
     * Uses MediaStore on Android 10–15+ (API 29+) for zero-permission scoped storage compatibility.
     */
    suspend fun exportToPublicDocuments(
        context: Context,
        record: TrackerRecordEntity
    ): Result<ExportResult> = withContext(Dispatchers.IO) {
        runCatching {
            val date = NumberFormatUtils.parseDisplayDateOrDefault(
                record.dateString,
                record.timestampMillis
            )
            val time = NumberFormatUtils.parseDisplayTimeOrDefault(
                record.timeString,
                record.timestampMillis
            )
            val fileName = NumberFormatUtils.buildExportFileName(date, time, "csv")
            val csvBytes = buildCsvContent(record).toByteArray(Charsets.UTF_8)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val relativePath = "${Environment.DIRECTORY_DOCUMENTS}/$EXPORT_SUBFOLDER"
                val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

                // Delete any previous file owned by this app with the same name so re-exports overwrite cleanly
                runCatching {
                    val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND " +
                        "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
                    val selectionArgs = arrayOf(fileName, "$relativePath%")
                    resolver.delete(collection, selection, selectionArgs)
                }

                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, MIME_CSV)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "$relativePath/")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val itemUri = resolver.insert(collection, values)
                    ?: throw IOException("Unable to create file in MediaStore Documents/$EXPORT_SUBFOLDER")

                try {
                    resolver.openOutputStream(itemUri, "wt")?.use { out ->
                        out.write(UTF8_BOM)
                        out.write(csvBytes)
                        out.flush()
                    } ?: throw IOException("Unable to open output stream for $fileName")

                    val publishValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.IS_PENDING, 0)
                    }
                    resolver.update(itemUri, publishValues, null, null)
                } catch (e: Exception) {
                    runCatching { resolver.delete(itemUri, null, null) }
                    throw e
                }

                ExportResult(
                    fileName = fileName,
                    displayLocation = "Documents/$EXPORT_SUBFOLDER/$fileName",
                    uri = itemUri
                )
            } else {
                @Suppress("DEPRECATION")
                val docsRoot = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS
                )
                val targetDir = File(docsRoot, EXPORT_SUBFOLDER)
                if (!targetDir.exists() && !targetDir.mkdirs()) {
                    throw IOException("Cannot create directory ${targetDir.absolutePath}")
                }
                val outFile = File(targetDir, fileName)
                FileOutputStream(outFile).use { out ->
                    out.write(UTF8_BOM)
                    out.write(csvBytes)
                    out.flush()
                }
                ExportResult(
                    fileName = fileName,
                    displayLocation = "Documents/$EXPORT_SUBFOLDER/$fileName",
                    uri = Uri.fromFile(outFile)
                )
            }
        }
    }

    /**
     * Writes the CSV report to any user-selected Storage Access Framework (SAF) Document Uri.
     */
    suspend fun exportToSafUri(
        context: Context,
        uri: Uri,
        record: TrackerRecordEntity
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val csvBytes = buildCsvContent(record).toByteArray(Charsets.UTF_8)
            context.contentResolver.openOutputStream(uri, "wt")?.use { out ->
                out.write(UTF8_BOM)
                out.write(csvBytes)
                out.flush()
            } ?: throw IOException("Unable to write to selected storage location")
            uri.lastPathSegment ?: "custom file"
        }
    }

    private fun StringBuilder.appendRow(vararg cells: String) {
        cells.forEachIndexed { idx, cell ->
            if (idx > 0) append(',')
            append(escapeCsvCell(cell))
        }
        append('\n')
    }

    internal fun escapeCsvCell(value: String): String {
        // Formula-style phone string ="016..." preserves leading zero in spreadsheet apps
        if (value.startsWith("=\"") && value.endsWith("\"") && value.length > 3) {
            return value
        }
        val needsQuoting = value.contains(',') ||
            value.contains('"') ||
            value.contains('\n') ||
            value.contains('\r')
        return if (needsQuoting) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}

