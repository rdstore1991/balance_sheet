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
import com.mfstracker.app.data.repository.TrackerRepository
import com.mfstracker.app.util.NumberFormatUtils
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
import java.util.UUID

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
    val isSaving: Boolean = false,
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

    private val _uiState = MutableStateFlow(TrackerUiState())
    val uiState: StateFlow<TrackerUiState> = _uiState.asStateFlow()

    private var pendingSafRecord: TrackerRecordEntity? = null

    init {
        viewModelScope.launch {
            repository.isDarkModeFlow.collect { pref ->
                _uiState.update { it.copy(darkModePreference = pref) }
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

    fun setHistoryVisible(visible: Boolean) {
        _uiState.update { it.copy(isHistoryVisible = visible) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    /**
     * Resets the dashboard to a fresh daily sheet, automatically carrying forward
     * Available Balance -> Previous Balance and MSA -> Previous MSA from the most recent saved record.
     */
    fun startNewSheet() {
        viewModelScope.launch {
            val latest = repository.getLatestRecord()
            _uiState.update { current ->
                TrackerUiState(
                    darkModePreference = current.darkModePreference,
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
                    historyRecords = current.historyRecords,
                    isHistoryVisible = false,
                    statusMessage = "New sheet ready"
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

    fun loadRecordFromHistory(record: TrackerRecordEntity) {
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
                statusMessage = "Loaded entry: ${record.dateString} ${record.timeString}"
            )
        }
    }

    fun exportHistoryRecordToDocuments(record: TrackerRecordEntity) {
        viewModelScope.launch {
            val outcome = repository.exportRecordToDocuments(record)
            val msg = outcome.fold(
                onSuccess = { res -> "Exported to ${res.displayLocation}" },
                onFailure = { err -> "Export failed: ${err.message}" }
            )
            _uiState.update { it.copy(statusMessage = msg) }
        }
    }

    fun prepareSafExportForRecord(record: TrackerRecordEntity): String {
        pendingSafRecord = record
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

    fun completeSafExport(uri: Uri?) {
        val targetRecord = pendingSafRecord
        pendingSafRecord = null
        if (uri == null || targetRecord == null) return

        viewModelScope.launch {
            val outcome = repository.exportRecordToSafUri(uri, targetRecord)
            val msg = outcome.fold(
                onSuccess = { "Exported CSV to selected folder" },
                onFailure = { err -> "SAF export failed: ${err.message}" }
            )
            _uiState.update { it.copy(statusMessage = msg) }
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

    private fun buildEntityFromState(state: TrackerUiState): TrackerRecordEntity {
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

