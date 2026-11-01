package com.mfstracker.app.util

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

object NumberFormatUtils {

    private val bdtSymbols = DecimalFormatSymbols(Locale.US)

    val displayDateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.US)

    val displayTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("hh:mm a", Locale.US)

    private val fileTimestampFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm", Locale.US)

    /**
     * Safely parses any nullable/empty/partial string into a finite non-NaN Double,
     * defaulting strictly to 0.0.
     */
    fun String?.toSafeDouble(): Double {
        if (this.isNullOrBlank()) return 0.0
        val cleaned = this.trim().replace(",", "")
        if (cleaned == "." || cleaned == "-" || cleaned == "-.") return 0.0
        val parsed = cleaned.toDoubleOrNull() ?: return 0.0
        return if (parsed.isFinite()) parsed else 0.0
    }

    /**
     * Filters keyboard input so only non-negative decimal numbers (at most one decimal point)
     * can be entered into numeric fields.
     */
    fun sanitizeDecimalInput(input: String): String {
        if (input.isEmpty()) return ""
        val sb = StringBuilder(input.length)
        var hasDot = false
        for (ch in input) {
            when {
                ch in '0'..'9' -> sb.append(ch)
                ch == '.' && !hasDot -> {
                    if (sb.isEmpty()) sb.append('0')
                    sb.append('.')
                    hasDot = true
                }
            }
        }
        return sb.toString()
    }

    /**
     * Formats a BDT amount for UI display with thousands separators and 2 decimal places.
     */
    fun Double.formatBdt(): String {
        val safe = if (this.isFinite()) this else 0.0
        return DecimalFormat("#,##0.00", bdtSymbols).format(safe)
    }

    /**
     * Formats a Double with 2 fixed decimal places and no grouping commas (ideal for CSV export).
     */
    fun Double.formatCsvAmount(): String {
        val safe = if (this.isFinite()) this else 0.0
        return String.format(Locale.US, "%.2f", safe)
    }

    /**
     * Converts a saved Double into a clean editable text field string without trailing ".0" noise.
     */
    fun Double.toEditableString(blankIfZero: Boolean = false): String {
        if (!this.isFinite() || this == 0.0) {
            return if (blankIfZero) "" else "0"
        }
        return BigDecimal.valueOf(this).stripTrailingZeros().toPlainString()
    }

    fun formatDate(date: LocalDate): String = date.format(displayDateFormatter)

    fun formatTime(time: LocalTime): String = time.format(displayTimeFormatter)

    fun buildExportFileName(
        date: LocalDate,
        time: LocalTime,
        extension: String = "csv"
    ): String {
        val stamp = LocalDateTime.of(date, time).format(fileTimestampFormatter)
        return "MFS_Tracker_${stamp}.${extension}"
    }

    fun toEpochMillis(
        date: LocalDate,
        time: LocalTime,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long {
        return LocalDateTime.of(date, time).atZone(zoneId).toInstant().toEpochMilli()
    }

    fun localDateToUtcPickerMillis(date: LocalDate): Long {
        return date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }

    fun utcPickerMillisToLocalDate(millis: Long): LocalDate {
        return Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
    }

    fun parseDisplayDateOrDefault(
        dateString: String,
        fallbackMillis: Long
    ): LocalDate {
        return runCatching { LocalDate.parse(dateString, displayDateFormatter) }
            .getOrElse {
                Instant.ofEpochMilli(fallbackMillis).atZone(ZoneId.systemDefault()).toLocalDate()
            }
    }

    fun parseDisplayTimeOrDefault(
        timeString: String,
        fallbackMillis: Long
    ): LocalTime {
        return runCatching { LocalTime.parse(timeString, displayTimeFormatter) }
            .getOrElse {
                Instant.ofEpochMilli(fallbackMillis).atZone(ZoneId.systemDefault()).toLocalTime()
            }
    }
}

