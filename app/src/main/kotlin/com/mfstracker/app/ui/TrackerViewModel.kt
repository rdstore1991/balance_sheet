package com.mfstracker.app.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mfstracker.app.MfsTrackerApp
import com.mfstracker.app.data.local.TrackerRecordEntity
import com.mfstracker.app.data.model.ExpenseIncomeItem
import com.mfstracker.app.data.model.FixedMfsAccounts
import com.mfstracker.app.data.model.MfsAccountBalance
import com.mfstracker.app.data.model.TransactionType
import com.mfstracker.app.data.preferences.CurrencyFormat
import com.mfstracker.app.data.preferences.ThemeMode
import com.mfstracker.app.data.repository.TrackerRepository
import com.mfstracker.app.util.NumberFormatUtils
import com.mfstracker.app.util.NumberFormatUtils.formatBdt
import com.mfstracker.app.util.NumberFormatUtils.sanitizeDecimalInput
import com.mfstracker.app.util.NumberFormatUtils.toEditableString
import com.mfstracker.app.util.NumberFormatUtils.toSafeDouble
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.util.UUID

enum class ExportRangePreset(val label: String) {
    ALL_RECORDS("All Records"),
    THIS_MONTH("This Month"),
    LAST_7_DAYS("Last 7 Days"),
    CUSTOM_RANGE("Custom Dates")
}

data class MfsBalanceInputUi(
    val key: String,
    val slNo: Int,
    val accountNumber: String,
    val provider: String,
    val rawBalance: String = ""
) {
    val numericBalance: Double
        get() = rawBalance.toSafeDouble()
}

data class LogRowUi(
    val id: String = UUID.randomUUID().toString(),
    val type: TransactionType = TransactionType.EXPENSE,
    val description: String = "",
    val rawAmount: String = "",
    val notes: String = ""
) {
    val numericAmount: Double
        get() = rawAmount.toSafeDouble()
}

data class TrackerUiState(
    val darkModePreference: Boolean? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val currencyFormat: CurrencyFormat = CurrencyFormat.TAKA_SYMBOL,
    val editingRecordId: Long? = null,
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedTime: LocalTime = LocalTime.now().withSecond(0).withNano(0),
    val mfsInputs: List<MfsBalanceInputUi> = createEmptyMfsInputs(),
    val logRows: List<LogRowUi> = createDefaultLogRows(),
    val previousBalanceInput: String = "",
    val msaInput: String = "",
    val previousMsaInput: String = "",
    val isPreviousBalanceManuallyEdited: Boolean = false,
    val isPreviousMsaManuallyEdited: Boolean = false,
    val historyRecords: List<TrackerRecordEntity> = emptyList(),
    val isHistoryVisible: Boolean = false,
    val historySearchQuery: String = "",
    val historyFilterStartDate: LocalDate? = null,
    val historyFilterEndDate: LocalDate? = null,
    val historyTypeFilter: TransactionType? = null,
    val exportPreset: ExportRangePreset = ExportRangePreset.ALL_RECORDS,
    val exportCustomStartDate: LocalDate = LocalDate.now().minusDays(30),
    val exportCustomEndDate: LocalDate = LocalDate.now(),
    val lastExportConfirmation: String? = null,
    val isSaving: Boolean = false,
    val isExporting: Boolean = false,
    val statusMessage: String? = null
) {
    val formattedDate: String
        get() = NumberFormatUtils.formatDate(selectedDate)

    val formattedTime: String
        get() = NumberFormatUtils.formatTime(selectedTime)

    val totalMfsBalance: Double
        get() = mfsInputs.sumOf { it.numericBalance }

    /**
     * Part 3 Available Balance strictly mirrors Part 1 TOTAL MFS Balances in real time.
     */
    val availableBalance: Double
        get() = totalMfsBalance

    val totalDailyExpenses: Double
        get() = logRows
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.numericAmount }

    val totalDailyIncome: Double
        get() = logRows
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.numericAmount }

    // --- Dashboard Homepage KPIs (derived from saved snapshots) ---

    val latestSnapshot: TrackerRecordEntity?
        get() = historyRecords.firstOrNull()

    val latestAvailableBalance: Double
        get() = latestSnapshot?.availableBalance ?: 0.0

    val kpiReferenceMonth: YearMonth
        get() = latestSnapshot?.let { snapshot ->
            YearMonth.from(
                NumberFormatUtils.parseDisplayDateOrDefault(
                    snapshot.dateString,
                    snapshot.timestampMillis
                )
            )
        } ?: YearMonth.now()

    val kpiMonthLabel: String
        get() = NumberFormatUtils.formatMonthYear(kpiReferenceMonth)

    val totalMonthlyExpenses: Double
        get() = historyRecords
            .filter { record ->
                val recordMonth = YearMonth.from(
                    NumberFormatUtils.parseDisplayDateOrDefault(
                        record.dateString,
                        record.timestampMillis
                    )
                )
                recordMonth == kpiReferenceMonth
            }
            .sumOf { it.totalDailyExpenses }

    val activeAccountCount: Int
        get() = latestSnapshot?.mfsBalances?.count { it.balance > 0.0 } ?: 0

    val totalConfiguredAccounts: Int
        get() = FixedMfsAccounts.specs.size

    // --- Filtered History & Ledger Feed ---

    val filteredHistoryRecords: List<TrackerRecordEntity>
        get() {
            val query = historySearchQuery.trim().lowercase()
            return historyRecords.filter { record ->
                val recDate = NumberFormatUtils.parseDisplayDateOrDefault(
                    record.dateString,
                    record.timestampMillis
                )

                val withinStart = historyFilterStartDate?.let { !recDate.isBefore(it) } ?: true
                val withinEnd = historyFilterEndDate?.let { !recDate.isAfter(it) } ?: true
                if (!withinStart || !withinEnd) return@filter false

                val matchesType = when (historyTypeFilter) {
                    null -> true
                    TransactionType.EXPENSE -> record.transactions.any {
                        it.type == TransactionType.EXPENSE && (it.amount > 0.0 || it.description.isNotBlank())
                    }
                    TransactionType.INCOME -> record.transactions.any {
                        it.type == TransactionType.INCOME && (it.amount > 0.0 || it.description.isNotBlank())
                    }
                }
                if (!matchesType) return@filter false

                if (query.isEmpty()) return@filter true

                val prettyDate = NumberFormatUtils.formatDatePretty(recDate).lowercase()
                val matchesHeader = record.dateString.lowercase().contains(query) ||
                    prettyDate.contains(query) ||
                    record.timeString.lowercase().contains(query) ||
                    record.availableBalance.formatBdt().contains(query) ||
                    record.msa.formatBdt().contains(query)

                val matchesTransactions = record.transactions.any { tx ->
                    tx.description.lowercase().contains(query) ||
                        tx.notes.lowercase().contains(query) ||
                        tx.type.label.lowercase().contains(query) ||
                        tx.amount.formatBdt().contains(query)
                }

                val matchesAccounts = record.mfsBalances.any { acc ->
                    acc.accountNumber.contains(query) ||
                        acc.provider.lowercase().contains(query)
                }

                matchesHeader || matchesTransactions || matchesAccounts
            }
        }

    // --- Export CSV Selection ---

    val selectedExportRangeLabel: String
        get() = when (exportPreset) {
            ExportRangePreset.ALL_RECORDS -> "All Records"
            ExportRangePreset.THIS_MONTH -> "Month ${NumberFormatUtils.formatMonthYear(YearMonth.now())}"
            ExportRangePreset.LAST_7_DAYS -> "Last 7 Days"
            ExportRangePreset.CUSTOM_RANGE -> {
                val start = NumberFormatUtils.formatDate(exportCustomStartDate)
                val end = NumberFormatUtils.formatDate(exportCustomEndDate)
                "$start to $end"
            }
        }

    val recordsForSelectedExportRange: List<TrackerRecordEntity>
        get() {
            val today = LocalDate.now()
            val currentMonth = YearMonth.from(today)
            val sevenDaysAgo = today.minusDays(6)

            return historyRecords.filter { record ->
                val recDate = NumberFormatUtils.parseDisplayDateOrDefault(
                    record.dateString,
                    record.timestampMillis
                )
                when (exportPreset) {
                    ExportRangePreset.ALL_RECORDS -> true
                    ExportRangePreset.THIS_MONTH -> YearMonth.from(recDate) == currentMonth
                    ExportRangePreset.LAST_7_DAYS -> !recDate.isBefore(sevenDaysAgo) && !recDate.isAfter(today)
                    ExportRangePreset.CUSTOM_RANGE -> {
                        val effectiveStart = minOf(exportCustomStartDate, exportCustomEndDate)
                        val effectiveEnd = maxOf(exportCustomStartDate, exportCustomEndDate)
                        !recDate.isBefore(effectiveStart) && !recDate.isAfter(effectiveEnd)
                    }
                }
            }
        }

    companion object {
        fun createEmptyMfsInputs(): List<MfsBalanceInputUi> {
            return FixedMfsAccounts.specs.map { spec ->
                MfsBalanceInputUi(
                    key = spec.key,
                    slNo = spec.slNo,
                    accountNumber = spec.accountNumber,
                    provider = spec.provider,
                    rawBalance = ""
                )
            }
        }

        fun createDefaultLogRows(
            count: Int = FixedMfsAccounts.DEFAULT_LOG_ROW_COUNT
        ): List<LogRowUi> {
            return List(count) {
                LogRowUi(type = TransactionType.EXPENSE)
            }
        }
    }
}

class TrackerViewModel(
    private val repository: TrackerRepository
) : ViewModel() {

    private sealed interface PendingSafPayload {
        data class Single(val record: TrackerRecordEntity) : PendingSafPayload
        data class Batch(
            val records: List<TrackerRecordEntity>,
            val rangeLabel: String
        ) : PendingSafPayload
    }

    private val _uiState = MutableStateFlow(TrackerUiState())
    val uiState: StateFlow<TrackerUiState> = _uiState.asStateFlow()

    private var pendingSafPayload: PendingSafPayload? = null

    init {
        viewModelScope.launch {
            repository.isDarkModeFlow.collect { pref ->
                val resolvedMode = when (pref) {
                    true -> ThemeMode.DARK
                    false -> ThemeMode.LIGHT
                    null -> ThemeMode.SYSTEM
                }
                _uiState.update {
                    it.copy(
                        darkModePreference = pref,
                        themeMode = resolvedMode
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.themeModeFlow.collect { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
        }

        viewModelScope.launch {
            repository.currencyFormatFlow.collect { format ->
                _uiState.update { it.copy(currencyFormat = format) }
            }
        }

        viewModelScope.launch {
            repository.allRecordsFlow.collect { records ->
                val latest = records.firstOrNull()
                _uiState.update { current ->
                    val shouldAutoFillPrevBalance =
                        current.editingRecordId == null && !current.isPreviousBalanceManuallyEdited
                    val shouldAutoFillPrevMsa =
                        current.editingRecordId == null && !current.isPreviousMsaManuallyEdited

                    current.copy(
                        historyRecords = records,
                        previousBalanceInput = if (shouldAutoFillPrevBalance) {
                            latest?.availableBalance?.toEditableString() ?: ""
                        } else {
                            current.previousBalanceInput
                        },
                        previousMsaInput = if (shouldAutoFillPrevMsa) {
                            latest?.msa?.toEditableString() ?: ""
                        } else {
                            current.previousMsaInput
                        }
                    )
                }
            }
        }
    }

    fun toggleTheme(currentlyDark: Boolean) {
        viewModelScope.launch {
            repository.setDarkMode(!currentlyDark)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun setCurrencyFormat(format: CurrencyFormat) {
        viewModelScope.launch {
            repository.setCurrencyFormat(format)
        }
    }

    fun updateSelectedDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun updateSelectedTime(hour: Int, minute: Int) {
        _uiState.update {
            it.copy(selectedTime = LocalTime.of(hour, minute))
        }
    }

    fun updateMfsBalance(key: String, rawInput: String) {
        val cleaned = sanitizeDecimalInput(rawInput)
        _uiState.update { state ->
            state.copy(
                mfsInputs = state.mfsInputs.map { row ->
                    if (row.key == key) row.copy(rawBalance = cleaned) else row
                }
            )
        }
    }

    fun addLogRow() {
        _uiState.update { state ->
            state.copy(logRows = state.logRows + LogRowUi(type = TransactionType.EXPENSE))
        }
    }

    fun deleteLogRow(rowId: String) {
        _uiState.update { state ->
            state.copy(logRows = state.logRows.filterNot { it.id == rowId })
        }
    }

    fun updateLogRowType(rowId: String, type: TransactionType) {
        _uiState.update { state ->
            state.copy(
                logRows = state.logRows.map { row ->
                    if (row.id == rowId) row.copy(type = type) else row
                }
            )
        }
    }

    fun updateLogRowDescription(rowId: String, description: String) {
        _uiState.update { state ->
            state.copy(
                logRows = state.logRows.map { row ->
                    if (row.id == rowId) row.copy(description = description) else row
                }
            )
        }
    }

    fun updateLogRowAmount(rowId: String, rawAmount: String) {
        val cleaned = sanitizeDecimalInput(rawAmount)
        _uiState.update { state ->
            state.copy(
                logRows = state.logRows.map { row ->
                    if (row.id == rowId) row.copy(rawAmount = cleaned) else row
                }
            )
        }
    }

    fun updateLogRowNotes(rowId: String, notes: String) {
        _uiState.update { state ->
            state.copy(
                logRows = state.logRows.map { row ->
                    if (row.id == rowId) row.copy(notes = notes) else row
                }
            )
        }
    }

    fun updatePreviousBalance(rawInput: String) {
        val cleaned = sanitizeDecimalInput(rawInput)
        _uiState.update {
            it.copy(
                previousBalanceInput = cleaned,
                isPreviousBalanceManuallyEdited = true
            )
        }
    }

    fun updateMsa(rawInput: String) {
        val cleaned = sanitizeDecimalInput(rawInput)
        _uiState.update { it.copy(msaInput = cleaned) }
    }

    fun updatePreviousMsa(rawInput: String) {
        val cleaned = sanitizeDecimalInput(rawInput)
        _uiState.update {
            it.copy(
                previousMsaInput = cleaned,
                isPreviousMsaManuallyEdited = true
            )
        }
    }

    // --- History Filtering ---

    fun updateHistorySearchQuery(query: String) {
        _uiState.update { it.copy(historySearchQuery = query) }
    }

    fun updateHistoryFilterStartDate(date: LocalDate?) {
        _uiState.update { it.copy(historyFilterStartDate = date) }
    }

    fun updateHistoryFilterEndDate(date: LocalDate?) {
        _uiState.update { it.copy(historyFilterEndDate = date) }
    }

    fun updateHistoryTypeFilter(type: TransactionType?) {
        _uiState.update { it.copy(historyTypeFilter = type) }
    }

    fun clearHistoryFilters() {
        _uiState.update {
            it.copy(
                historySearchQuery = "",
                historyFilterStartDate = null,
                historyFilterEndDate = null,
                historyTypeFilter = null
            )
        }
    }

    // --- Export Tab Controls ---

    fun selectExportPreset(preset: ExportRangePreset) {
        _uiState.update { it.copy(exportPreset = preset) }
    }

    fun updateExportCustomStartDate(date: LocalDate) {
        _uiState.update { it.copy(exportCustomStartDate = date) }
    }

    fun updateExportCustomEndDate(date: LocalDate) {
        _uiState.update { it.copy(exportCustomEndDate = date) }
    }

    fun clearExportConfirmation() {
        _uiState.update { it.copy(lastExportConfirmation = null) }
    }

    fun setHistoryVisible(visible: Boolean) {
        _uiState.update { it.copy(isHistoryVisible = visible) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    /**
     * Clones closing balances from the most recent saved snapshot into Part 1,
     * carries forward Available Balance -> Previous Balance and MSA -> Previous MSA,
     * while keeping the current Date/Time and fresh Part 2 transaction log.
     */
    fun carryForwardLastRecord() {
        viewModelScope.launch {
            val latest = repository.getLatestRecord()
            if (latest == null) {
                _uiState.update {
                    it.copy(statusMessage = "No saved records found to carry forward")
                }
                return@launch
            }

            val mappedMfs = FixedMfsAccounts.specs.map { spec ->
                val savedMatch = latest.mfsBalances.firstOrNull { it.key == spec.key }
                    ?: latest.mfsBalances.firstOrNull {
                        it.slNo == spec.slNo && it.provider.equals(spec.provider, ignoreCase = true)
                    }
                MfsBalanceInputUi(
                    key = spec.key,
                    slNo = spec.slNo,
                    accountNumber = spec.accountNumber,
                    provider = spec.provider,
                    rawBalance = savedMatch?.balance?.toEditableString(blankIfZero = true) ?: ""
                )
            }

            _uiState.update { current ->
                current.copy(
                    mfsInputs = mappedMfs,
                    previousBalanceInput = latest.availableBalance.toEditableString(),
                    previousMsaInput = latest.msa.toEditableString(),
                    isPreviousBalanceManuallyEdited = false,
                    isPreviousMsaManuallyEdited = false,
                    statusMessage = "Carried forward balances from ${latest.dateString}"
                )
            }
        }
    }

    /**
     * Imports records from a selected CSV file Uri and updates local Room DB.
     */
    fun restoreRecordsFromCsv(uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val outcome = repository.importRecordsFromSafUri(uri)
            val msg = outcome.fold(
                onSuccess = { count -> "Successfully restored $count record(s) from backup" },
                onFailure = { err -> "CSV restore failed: ${err.message ?: "Invalid file"}" }
            )
            _uiState.update {
                it.copy(
                    isSaving = false,
                    statusMessage = msg
                )
            }
        }
    }

    /**
     * Resets the editor to a fresh daily sheet, automatically carrying forward
     * Available Balance -> Previous Balance and MSA -> Previous MSA from the most recent saved record.
     */
    fun startNewSheet(showMessage: Boolean = true) {
        viewModelScope.launch {
            val latest = repository.getLatestRecord()
            _uiState.update { current ->
                current.copy(
                    editingRecordId = null,
                    selectedDate = LocalDate.now(),
                    selectedTime = LocalTime.now().withSecond(0).withNano(0),
                    mfsInputs = TrackerUiState.createEmptyMfsInputs(),
                    logRows = TrackerUiState.createDefaultLogRows(),
                    previousBalanceInput = latest?.availableBalance?.toEditableString() ?: "",
                    msaInput = "",
                    previousMsaInput = latest?.msa?.toEditableString() ?: "",
                    isPreviousBalanceManuallyEdited = false,
                    isPreviousMsaManuallyEdited = false,
                    isHistoryVisible = false,
                    statusMessage = if (showMessage) "New sheet ready" else null
                )
            }
        }
    }

    fun saveRecord() {
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val snapshot = buildEntityFromState(_uiState.value)
                val savedId = repository.saveRecord(snapshot)
                _uiState.update {
                    it.copy(
                        editingRecordId = savedId,
                        isSaving = false,
                        statusMessage = "Saved record (${snapshot.dateString} ${snapshot.timeString})"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        statusMessage = "Failed to save: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun saveAndExportToStorage() {
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val snapshot = buildEntityFromState(_uiState.value)
                val savedId = repository.saveRecord(snapshot)
                val savedEntity = snapshot.copy(id = savedId)

                val exportOutcome = repository.exportRecordToDocuments(savedEntity)
                val msg = exportOutcome.fold(
                    onSuccess = { res -> "Saved & exported to ${res.displayLocation}" },
                    onFailure = { err -> "Saved locally; CSV export failed: ${err.message}" }
                )

                _uiState.update {
                    it.copy(
                        editingRecordId = savedId,
                        isSaving = false,
                        statusMessage = msg
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        statusMessage = "Error saving record: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun loadRecordFromHistory(record: TrackerRecordEntity, showMessage: Boolean = true) {
        val loadedDate = NumberFormatUtils.parseDisplayDateOrDefault(
            record.dateString,
            record.timestampMillis
        )
        val loadedTime = NumberFormatUtils.parseDisplayTimeOrDefault(
            record.timeString,
            record.timestampMillis
        )

        val mappedMfs = FixedMfsAccounts.specs.map { spec ->
            val savedMatch = record.mfsBalances.firstOrNull { it.key == spec.key }
                ?: record.mfsBalances.firstOrNull {
                    it.slNo == spec.slNo && it.provider.equals(spec.provider, ignoreCase = true)
                }
            MfsBalanceInputUi(
                key = spec.key,
                slNo = spec.slNo,
                accountNumber = spec.accountNumber,
                provider = spec.provider,
                rawBalance = savedMatch?.balance?.toEditableString(blankIfZero = true) ?: ""
            )
        }

        val mappedLogs = if (record.transactions.isEmpty()) {
            TrackerUiState.createDefaultLogRows()
        } else {
            record.transactions.map { tx ->
                LogRowUi(
                    id = tx.id.ifBlank { UUID.randomUUID().toString() },
                    type = tx.type,
                    description = tx.description,
                    rawAmount = tx.amount.toEditableString(blankIfZero = true),
                    notes = tx.notes
                )
            }
        }

        _uiState.update { current ->
            current.copy(
                editingRecordId = record.id,
                selectedDate = loadedDate,
                selectedTime = loadedTime,
                mfsInputs = mappedMfs,
                logRows = mappedLogs,
                previousBalanceInput = record.previousBalance.toEditableString(),
                msaInput = record.msa.toEditableString(),
                previousMsaInput = record.previousMsa.toEditableString(),
                isPreviousBalanceManuallyEdited = true,
                isPreviousMsaManuallyEdited = true,
                isHistoryVisible = false,
                statusMessage = if (showMessage) "Loaded entry: ${record.dateString} ${record.timeString}" else null
            )
        }
    }

    fun exportHistoryRecordToDocuments(record: TrackerRecordEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val outcome = repository.exportRecordToDocuments(record)
            val (msg, confirmBanner) = outcome.fold(
                onSuccess = { res ->
                    val text = "Exported to ${res.displayLocation}"
                    text to text
                },
                onFailure = { err ->
                    "Export failed: ${err.message}" to null
                }
            )
            _uiState.update {
                it.copy(
                    isExporting = false,
                    statusMessage = msg,
                    lastExportConfirmation = confirmBanner ?: it.lastExportConfirmation
                )
            }
        }
    }

    fun exportSelectedRangeToDocuments() {
        val currentState = _uiState.value
        if (currentState.isExporting) return
        val targetRecords = currentState.recordsForSelectedExportRange
        val rangeLabel = currentState.selectedExportRangeLabel

        if (targetRecords.isEmpty()) {
            _uiState.update {
                it.copy(statusMessage = "No saved records match '$rangeLabel'")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val outcome = repository.exportBatchRecordsToDocuments(targetRecords, rangeLabel)
            val (msg, confirmBanner) = outcome.fold(
                onSuccess = { res ->
                    val text = "Exported ${targetRecords.size} record(s) to ${res.displayLocation}"
                    text to text
                },
                onFailure = { err ->
                    "CSV export failed: ${err.message}" to null
                }
            )
            _uiState.update {
                it.copy(
                    isExporting = false,
                    statusMessage = msg,
                    lastExportConfirmation = confirmBanner ?: it.lastExportConfirmation
                )
            }
        }
    }

    fun exportFullDatabaseBackupToDocuments() {
        val allRecords = _uiState.value.historyRecords
        if (_uiState.value.isExporting) return
        if (allRecords.isEmpty()) {
            _uiState.update {
                it.copy(statusMessage = "No saved records in database to export")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val outcome = repository.exportBatchRecordsToDocuments(allRecords, "Full Database Backup")
            val (msg, confirmBanner) = outcome.fold(
                onSuccess = { res ->
                    val text = "Full backup (${allRecords.size} records) exported to ${res.displayLocation}"
                    text to text
                },
                onFailure = { err ->
                    "Backup export failed: ${err.message}" to null
                }
            )
            _uiState.update {
                it.copy(
                    isExporting = false,
                    statusMessage = msg,
                    lastExportConfirmation = confirmBanner ?: it.lastExportConfirmation
                )
            }
        }
    }

    fun prepareSafExportForRecord(record: TrackerRecordEntity): String {
        pendingSafPayload = PendingSafPayload.Single(record)
        val date = NumberFormatUtils.parseDisplayDateOrDefault(
            record.dateString,
            record.timestampMillis
        )
        val time = NumberFormatUtils.parseDisplayTimeOrDefault(
            record.timeString,
            record.timestampMillis
        )
        return NumberFormatUtils.buildExportFileName(date, time, "csv")
    }

    fun prepareSafBatchExport(): String? {
        val currentState = _uiState.value
        val targetRecords = currentState.recordsForSelectedExportRange
        val rangeLabel = currentState.selectedExportRangeLabel
        if (targetRecords.isEmpty()) {
            _uiState.update {
                it.copy(statusMessage = "No saved records match '$rangeLabel'")
            }
            return null
        }
        pendingSafPayload = PendingSafPayload.Batch(targetRecords, rangeLabel)
        return NumberFormatUtils.buildBatchExportFileName(
            rangeLabel = rangeLabel,
            date = LocalDate.now(),
            time = LocalTime.now(),
            extension = "csv"
        )
    }

    fun completeSafExport(uri: Uri?) {
        val payload = pendingSafPayload
        pendingSafPayload = null
        if (uri == null || payload == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val outcome = when (payload) {
                is PendingSafPayload.Single -> repository.exportRecordToSafUri(uri, payload.record)
                is PendingSafPayload.Batch -> repository.exportBatchRecordsToSafUri(
                    uri = uri,
                    records = payload.records,
                    rangeLabel = payload.rangeLabel
                )
            }
            val (msg, confirmBanner) = outcome.fold(
                onSuccess = { location ->
                    val text = "Exported CSV successfully ($location)"
                    text to text
                },
                onFailure = { err ->
                    "SAF export failed: ${err.message}" to null
                }
            )
            _uiState.update {
                it.copy(
                    isExporting = false,
                    statusMessage = msg,
                    lastExportConfirmation = confirmBanner ?: it.lastExportConfirmation
                )
            }
        }
    }

    fun deleteHistoryRecord(recordId: Long) {
        viewModelScope.launch {
            repository.deleteRecord(recordId)
            _uiState.update { current ->
                val detachedId = if (current.editingRecordId == recordId) null else current.editingRecordId
                current.copy(
                    editingRecordId = detachedId,
                    statusMessage = "Deleted saved entry"
                )
            }
        }
    }

    fun clearAllRecords() {
        viewModelScope.launch {
            repository.deleteAllRecords()
            _uiState.update { current ->
                current.copy(
                    editingRecordId = null,
                    previousBalanceInput = "",
                    previousMsaInput = "",
                    isPreviousBalanceManuallyEdited = false,
                    isPreviousMsaManuallyEdited = false,
                    lastExportConfirmation = null,
                    statusMessage = "All saved records cleared from local database"
                )
            }
        }
    }

    fun buildEntityFromState(state: TrackerUiState): TrackerRecordEntity {
        val mfsList = state.mfsInputs.map { ui ->
            MfsAccountBalance(
                key = ui.key,
                slNo = ui.slNo,
                accountNumber = ui.accountNumber,
                provider = ui.provider,
                balance = ui.numericBalance
            )
        }

        val txList = state.logRows.map { row ->
            ExpenseIncomeItem(
                id = row.id,
                type = row.type,
                description = row.description.trim(),
                amount = row.numericAmount,
                notes = row.notes.trim()
            )
        }

        val totalMfs = mfsList.sumOf { it.balance }
        val epochMillis = NumberFormatUtils.toEpochMillis(state.selectedDate, state.selectedTime)

        return TrackerRecordEntity(
            id = state.editingRecordId ?: 0L,
            timestampMillis = epochMillis,
            dateString = state.formattedDate,
            timeString = state.formattedTime,
            mfsBalances = mfsList,
            transactions = txList,
            availableBalance = totalMfs,
            previousBalance = state.previousBalanceInput.toSafeDouble(),
            msa = state.msaInput.toSafeDouble(),
            previousMsa = state.previousMsaInput.toSafeDouble(),
            updatedAtMillis = System.currentTimeMillis()
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MfsTrackerApp)
                TrackerViewModel(app.repository)
            }
        }
    }
}
