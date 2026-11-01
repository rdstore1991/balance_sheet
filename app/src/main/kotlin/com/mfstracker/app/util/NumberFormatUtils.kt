package com.mfstracker.app.util

import com.mfstracker.app.data.preferences.CurrencyFormat
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

object NumberFormatUtils {

    private val bdtSymbols = DecimalFormatSymbols(Locale.US)

    val displayDateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.US)

    val displayDatePrettyFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)

    val displayTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("hh:mm a", Locale.US)

    val monthYearFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMM yyyy", Locale.US)

    private val fileTimestampFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm", Locale.US)

    /**
     * Filters keyboard input so non-negative numbers, decimal points, and math operators (+, -, *, /)
     * can be entered for inline calculator expressions.
     */
    fun sanitizeMathExpressionInput(input: String): String {
        if (input.isEmpty()) return ""
        val allowedChars = "0123456789.+-*/()"
        return input.filter { it in allowedChars }
    }

    /**
     * Safely parses any nullable/empty/partial string into a finite non-NaN Double,
     * defaulting strictly to 0.0. Supports inline arithmetic expressions.
     */
    fun String?.toSafeDouble(): Double {
        if (this.isNullOrBlank()) return 0.0
        val cleaned = this.trim().replace(",", "")
        if (cleaned == "." || cleaned == "-" || cleaned == "-." || cleaned == "+") return 0.0
        val evaluated = MathExpressionEvaluator.evaluateMathExpression(cleaned)
        if (evaluated != null) return evaluated
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
     * Formats an amount using the active user currency preference (e.g. "৳ 1,250.00" or "BDT 1,250.00").
     */
    fun Double.formatCurrency(format: CurrencyFormat = CurrencyFormat.TAKA_SYMBOL): String {
        return "${format.prefix} ${this.formatBdt()}"
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

    /**
     * Formats an 11-digit Bangladesh phone number into `01XXX-XXXXXX` for easy scanning without truncation.
     */
    fun formatPhoneNumber(raw: String): String {
        val cleaned = raw.filter { it.isDigit() }
        return if (cleaned.length == 11) {
            "${cleaned.substring(0, 5)}-${cleaned.substring(5)}"
        } else {
            raw
        }
    }

    fun formatDate(date: LocalDate): String = date.format(displayDateFormatter)

    fun formatDatePretty(date: LocalDate): String = date.format(displayDatePrettyFormatter)

    fun formatTime(time: LocalTime): String = time.format(displayTimeFormatter)

    fun formatMonthYear(yearMonth: YearMonth): String = yearMonth.format(monthYearFormatter)

    fun buildExportFileName(
        date: LocalDate,
        time: LocalTime,
        extension: String = "csv"
    ): String {
        val stamp = LocalDateTime.of(date, time).format(fileTimestampFormatter)
        return "MFS_Tracker_${stamp}.${extension}"
    }

    fun buildBatchExportFileName(
        rangeLabel: String,
        date: LocalDate,
        time: LocalTime,
        extension: String = "csv"
    ): String {
        val safeScope = rangeLabel
            .trim()
            .replace(Regex("[^A-Za-z0-9]+"), "_")
            .trim('_')
            .ifBlank { "Export" }
        val stamp = LocalDateTime.of(date, time).format(fileTimestampFormatter)
        return "MFS_Tracker_${safeScope}_${stamp}.${extension}"
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
