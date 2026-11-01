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
import java.time.LocalDate
import java.time.LocalTime

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
     * Parses an RFC-4180 CSV backup file (single-snapshot or consolidated multi-snapshot)
     * and reconstructs TrackerRecordEntity models for database restore.
     */
    fun parseCsvContent(csvText: String): List<TrackerRecordEntity> {
        val lines = csvText.lines().map { it.trim().trim('\uFEFF') }
        if (lines.isEmpty()) return emptyList()

        val parsedRecords = mutableListOf<TrackerRecordEntity>()
        var i = 0

        while (i < lines.size) {
            val line = lines[i]
            if (line.contains("MFS FUND & EXPENSE TRACKER") && !line.contains("CONSOLIDATED LEDGER EXPORT")) {
                val record = parseSingleRecordBlock(lines, i)
                if (record != null) {
                    parsedRecords.add(record)
                }
            }
            i++
        }

        return parsedRecords
    }

    private fun parseSingleRecordBlock(lines: List<String>, startIndex: Int): TrackerRecordEntity? {
        var dateStr = ""
        var timeStr = ""
        var prevBal = 0.0
        var curMsa = 0.0
        var prevMsa = 0.0
        var currentSection = 0 // 1: Part 1 MFS, 2: Part 2 Tx, 3: Part 3 Reconciliation
        val mfsList = mutableListOf<com.mfstracker.app.data.model.MfsAccountBalance>()
        val txList = mutableListOf<com.mfstracker.app.data.model.ExpenseIncomeItem>()

        var idx = startIndex
        while (idx < lines.size) {
            val rawLine = lines[idx]
            val cells = parseCsvRow(rawLine)

            if (idx > startIndex && rawLine.contains("MFS FUND & EXPENSE TRACKER")) {
                break
            }

            if (rawLine.contains("PART 1:", ignoreCase = true)) {
                currentSection = 1
            } else if (rawLine.contains("PART 2:", ignoreCase = true)) {
                currentSection = 2
            } else if (rawLine.contains("PART 3:", ignoreCase = true)) {
                currentSection = 3
            }

            if (cells.size >= 4 && cells[0].equals("Date", ignoreCase = true) && cells[2].equals("Time", ignoreCase = true)) {
                dateStr = cells[1].trim()
                timeStr = cells[3].trim()
            }

            // MFS account rows: e.g. ["1", "=\"01605001886\"", "Bkash", "15000.00"]
            val slNoParsed = cells.getOrNull(0)?.toIntOrNull()
            if (currentSection == 1 && slNoParsed != null && cells.size >= 4) {
                val rawAcc = cells[1].trim().removePrefix("=\"").removePrefix("=").removeSuffix("\"")
                val provider = cells[2].trim()
                val balance = cells[3].toDoubleOrNull() ?: 0.0
                val key = when {
                    slNoParsed == 1 && provider.contains("bkash", true) -> "1A"
                    slNoParsed == 1 && provider.contains("nagad", true) -> "1B"
                    slNoParsed == 2 && provider.contains("bkash", true) -> "2A"
                    slNoParsed == 2 && provider.contains("nagad", true) -> "2B"
                    slNoParsed == 3 && provider.contains("bkash", true) -> "3A"
                    slNoParsed == 3 && provider.contains("nagad", true) -> "3B"
                    slNoParsed == 4 -> "4"
                    else -> "${slNoParsed}_${provider}"
                }
                mfsList.add(
                    com.mfstracker.app.data.model.MfsAccountBalance(
                        key = key,
                        slNo = slNoParsed,
                        accountNumber = rawAcc,
                        provider = provider,
                        balance = balance
                    )
                )
            }

            // Transaction rows: e.g. ["1", "Expense", "Lunch", "150.00", "Notes"]
            val rowNumParsed = cells.getOrNull(0)?.toIntOrNull()
            if (currentSection == 2 && rowNumParsed != null && cells.size >= 4 && (cells[1].equals("Expense", true) || cells[1].equals("Income", true))) {
                val type = if (cells[1].equals("Income", true)) com.mfstracker.app.data.model.TransactionType.INCOME else com.mfstracker.app.data.model.TransactionType.EXPENSE
                val desc = cells[2].trim()
                val amount = cells[3].toDoubleOrNull() ?: 0.0
                val notes = cells.getOrElse(4) { "" }.trim()
                txList.add(
                    com.mfstracker.app.data.model.ExpenseIncomeItem(
                        id = java.util.UUID.randomUUID().toString(),
                        type = type,
                        description = desc,
                        amount = amount,
                        notes = notes
                    )
                )
            }

            // Part 3 Reconciliation fields
            if (currentSection == 3 && cells.isNotEmpty()) {
                val label = cells[0].trim()
                val amountVal = cells.getOrNull(1)?.toDoubleOrNull() ?: 0.0
                when {
                    label.equals("Previous Balance", ignoreCase = true) -> prevBal = amountVal
                    label.equals("MSA", ignoreCase = true) -> curMsa = amountVal
                    label.equals("Previous MSA", ignoreCase = true) -> prevMsa = amountVal
                }
            }

            idx++
        }

        if (dateStr.isBlank()) {
            dateStr = NumberFormatUtils.formatDate(java.time.LocalDate.now())
        }
        if (timeStr.isBlank()) {
            timeStr = NumberFormatUtils.formatTime(java.time.LocalTime.now())
        }

        val totalMfs = mfsList.sumOf { it.balance }
        val dateObj = NumberFormatUtils.parseDisplayDateOrDefault(dateStr, System.currentTimeMillis())
        val timeObj = NumberFormatUtils.parseDisplayTimeOrDefault(timeStr, System.currentTimeMillis())
        val timestampMillis = NumberFormatUtils.toEpochMillis(dateObj, timeObj)

        return TrackerRecordEntity(
            id = 0L,
            timestampMillis = timestampMillis,
            dateString = dateStr,
            timeString = timeStr,
            mfsBalances = mfsList,
            transactions = txList,
            availableBalance = totalMfs,
            previousBalance = prevBal,
            msa = curMsa,
            previousMsa = prevMsa,
            updatedAtMillis = System.currentTimeMillis()
        )
    }

    private fun parseCsvRow(row: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < row.length) {
            val c = row[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < row.length && row[i + 1] == '\"') {
                    current.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim())
                current = StringBuilder()
            } else {
                current.append(c)
            }
            i++
        }
        result.add(current.toString().trim())
        return result
    }

    /**
     * Reads a CSV backup file from a SAF Uri, parses its records, and returns them.
     */
    suspend fun importCsvFromSafUri(
        context: Context,
        uri: Uri
    ): Result<List<TrackerRecordEntity>> = withContext(Dispatchers.IO) {
        runCatching {
            val content = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).readText()
            } ?: throw IOException("Could not open input stream for selected backup file")

            val records = parseCsvContent(content)
            if (records.isEmpty()) {
                throw IllegalArgumentException("No valid MFS Tracker records found in selected CSV file")
            }
            records
        }
    }

    /**
     * Generates a structured RFC-4180 CSV report covering Part 1 (MFS Balances),
     * Part 2 (Expense & Income Log), and Part 3 (Reconciliation & MSA Summary).
     */
    fun buildCsvContent(record: TrackerRecordEntity): String {
        val sb = StringBuilder()
        appendSingleRecordBlock(sb, record)
        return sb.toString()
    }

    /**
     * Generates a consolidated RFC-4180 CSV report for a list of snapshots (date range or full DB),
     * starting with a summary overview table followed by complete per-snapshot breakdowns.
     */
    fun buildBatchCsvContent(
        records: List<TrackerRecordEntity>,
        rangeLabel: String = "All Records"
    ): String {
        val sb = StringBuilder()

        sb.appendRow("MFS FUND & EXPENSE TRACKER - CONSOLIDATED LEDGER EXPORT")
        sb.appendRow(
            "Export Scope",
            rangeLabel,
            "Total Snapshots",
            records.size.toString()
        )
        sb.appendLine()

        // Consolidated Summary Table
        sb.appendRow("SUMMARY LEDGER OVERVIEW")
        sb.appendRow(
            "Snapshot #",
            "Date",
            "Time",
            "Total MFS / Available Balance (BDT)",
            "Previous Balance (BDT)",
            "MSA (BDT)",
            "Previous MSA (BDT)",
            "Total Daily Expenses (BDT)",
            "Total Daily Income (BDT)"
        )

        if (records.isEmpty()) {
            sb.appendRow("-", "No records in range", "", "0.00", "0.00", "0.00", "0.00", "0.00", "0.00")
        } else {
            records.forEachIndexed { idx, rec ->
                sb.appendRow(
                    (idx + 1).toString(),
                    rec.dateString,
                    rec.timeString,
                    rec.availableBalance.formatCsvAmount(),
                    rec.previousBalance.formatCsvAmount(),
                    rec.msa.formatCsvAmount(),
                    rec.previousMsa.formatCsvAmount(),
                    rec.totalDailyExpenses.formatCsvAmount(),
                    rec.totalDailyIncome.formatCsvAmount()
                )
            }
            val sumExpenses = records.sumOf { it.totalDailyExpenses }
            val sumIncome = records.sumOf { it.totalDailyIncome }
            val latestAvail = records.first().availableBalance
            sb.appendRow(
                "SUMMARY TOTALS",
                "Latest Available ->",
                "",
                latestAvail.formatCsvAmount(),
                "",
                "",
                "",
                sumExpenses.formatCsvAmount(),
                sumIncome.formatCsvAmount()
            )
        }

        // Individual Detailed Sheets
        records.forEachIndexed { index, record ->
            sb.appendLine()
            sb.appendRow("=== SNAPSHOT ${index + 1} OF ${records.size} ===")
            appendSingleRecordBlock(sb, record)
        }

        return sb.toString()
    }

    private fun appendSingleRecordBlock(sb: StringBuilder, record: TrackerRecordEntity) {
        // Header block
        sb.appendRow("MFS FUND & EXPENSE TRACKER")
        sb.appendRow("Date", record.dateString, "Time", record.timeString)
        sb.appendLine()

        // Part 1: MFS Account Balances
        sb.appendRow("PART 1: I. MFS ACCOUNT BALANCES")
        sb.appendRow("SL#", "Account Number", "MFS Provider", "Current Balance (BDT)")
        for (item in record.mfsBalances) {
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
    }

    /**
     * Exports a single record to public external storage:
     * `Documents/MFS_Tracker/MFS_Tracker_YYYY-MM-DD_HHmm.csv`
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
            writeCsvBytesToPublicDocuments(context, fileName, csvBytes)
        }
    }

    /**
     * Exports a batch of records (date range or full database) directly into
     * `Documents/MFS_Tracker/` via MediaStore / Scoped Storage.
     */
    suspend fun exportBatchToPublicDocuments(
        context: Context,
        records: List<TrackerRecordEntity>,
        rangeLabel: String
    ): Result<ExportResult> = withContext(Dispatchers.IO) {
        runCatching {
            val fileName = NumberFormatUtils.buildBatchExportFileName(
                rangeLabel = rangeLabel,
                date = LocalDate.now(),
                time = LocalTime.now(),
                extension = "csv"
            )
            val csvBytes = buildBatchCsvContent(records, rangeLabel).toByteArray(Charsets.UTF_8)
            writeCsvBytesToPublicDocuments(context, fileName, csvBytes)
        }
    }

    private fun writeCsvBytesToPublicDocuments(
        context: Context,
        fileName: String,
        csvBytes: ByteArray
    ): ExportResult {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val relativePath = "${Environment.DIRECTORY_DOCUMENTS}/$EXPORT_SUBFOLDER"
            val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

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

    /**
     * Writes a single-record CSV report to any user-selected Storage Access Framework (SAF) Document Uri.
     */
    suspend fun exportToSafUri(
        context: Context,
        uri: Uri,
        record: TrackerRecordEntity
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val csvBytes = buildCsvContent(record).toByteArray(Charsets.UTF_8)
            writeCsvBytesToSafUri(context, uri, csvBytes)
        }
    }

    /**
     * Writes a consolidated multi-record CSV report to any user-selected SAF Document Uri.
     */
    suspend fun exportBatchToSafUri(
        context: Context,
        uri: Uri,
        records: List<TrackerRecordEntity>,
        rangeLabel: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val csvBytes = buildBatchCsvContent(records, rangeLabel).toByteArray(Charsets.UTF_8)
            writeCsvBytesToSafUri(context, uri, csvBytes)
        }
    }

    private fun writeCsvBytesToSafUri(
        context: Context,
        uri: Uri,
        csvBytes: ByteArray
    ): String {
        context.contentResolver.openOutputStream(uri, "wt")?.use { out ->
            out.write(UTF8_BOM)
            out.write(csvBytes)
            out.flush()
        } ?: throw IOException("Unable to write to selected storage location")
        return uri.lastPathSegment ?: "custom file"
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
