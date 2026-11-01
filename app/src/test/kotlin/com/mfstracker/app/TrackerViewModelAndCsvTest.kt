package com.mfstracker.app

import android.net.Uri
import com.mfstracker.app.data.local.TrackerRecordEntity
import com.mfstracker.app.data.local.TrackerTypeConverters
import com.mfstracker.app.data.model.TransactionType
import com.mfstracker.app.data.repository.TrackerRepository
import com.mfstracker.app.ui.TrackerViewModel
import com.mfstracker.app.util.CsvExporter
import com.mfstracker.app.util.ExportResult
import com.mfstracker.app.util.NumberFormatUtils
import com.mfstracker.app.util.NumberFormatUtils.toSafeDouble
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class TrackerViewModelAndCsvTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepository: FakeTrackerRepository
    private lateinit var viewModel: TrackerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeTrackerRepository()
        viewModel = TrackerViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has 7 fixed MFS rows and 5 empty Expense rows`() {
        val state = viewModel.uiState.value
        assertEquals(7, state.mfsInputs.size)
        assertEquals("01605001886", state.mfsInputs[0].accountNumber)
        assertEquals("Bkash", state.mfsInputs[0].provider)
        assertEquals("Nagad", state.mfsInputs[1].provider)
        assertEquals("01965972859", state.mfsInputs[2].accountNumber)
        assertEquals("01842662901", state.mfsInputs[4].accountNumber)
        assertEquals("01715385025", state.mfsInputs[6].accountNumber)
        assertEquals("Bkash Agent", state.mfsInputs[6].provider)

        assertEquals(5, state.logRows.size)
        assertTrue(state.logRows.all { it.type == TransactionType.EXPENSE })
        assertEquals(0.0, state.totalMfsBalance, 0.001)
        assertEquals(0.0, state.availableBalance, 0.001)
    }

    @Test
    fun `empty null and malformed decimal inputs safely parse to 0_0`() {
        val nullStr: String? = null
        assertEquals(0.0, nullStr.toSafeDouble(), 0.001)
        assertEquals(0.0, "".toSafeDouble(), 0.001)
        assertEquals(0.0, "   ".toSafeDouble(), 0.001)
        assertEquals(0.0, ".".toSafeDouble(), 0.001)
        assertEquals(0.0, "abc".toSafeDouble(), 0.001)
        assertEquals("1234.56", NumberFormatUtils.sanitizeDecimalInput("12a34.5.6"))
        assertEquals("0.5", NumberFormatUtils.sanitizeDecimalInput(".5"))
    }

    @Test
    fun `Part 1 total MFS balance reactively mirrors Part 3 Available Balance`() {
        viewModel.updateMfsBalance("1A", "15000.50")
        viewModel.updateMfsBalance("1B", "4999.50")
        viewModel.updateMfsBalance("4", "30000")

        val state = viewModel.uiState.value
        assertEquals(50000.0, state.totalMfsBalance, 0.001)
        assertEquals(50000.0, state.availableBalance, 0.001)
    }

    @Test
    fun `Part 2 computes Expense and Income totals and supports dynamic rows`() {
        val initialIds = viewModel.uiState.value.logRows.map { it.id }

        viewModel.updateLogRowDescription(initialIds[0], "Office Rent")
        viewModel.updateLogRowAmount(initialIds[0], "1200.50")

        viewModel.updateLogRowType(initialIds[1], TransactionType.INCOME)
        viewModel.updateLogRowDescription(initialIds[1], "Commission")
        viewModel.updateLogRowAmount(initialIds[1], "3500.25")

        viewModel.addLogRow()
        assertEquals(6, viewModel.uiState.value.logRows.size)

        val addedId = viewModel.uiState.value.logRows.last().id
        viewModel.updateLogRowAmount(addedId, "299.50")

        assertEquals(1500.0, viewModel.uiState.value.totalDailyExpenses, 0.001)
        assertEquals(3500.25, viewModel.uiState.value.totalDailyIncome, 0.001)

        viewModel.deleteLogRow(addedId)
        assertEquals(5, viewModel.uiState.value.logRows.size)
        assertEquals(1200.50, viewModel.uiState.value.totalDailyExpenses, 0.001)
    }

    @Test
    fun `saving record and starting new sheet auto-populates Previous Balance and Previous MSA`() = runTest {
        viewModel.updateMfsBalance("1A", "25000")
        viewModel.updateMfsBalance("2A", "15000")
        viewModel.updateMsa("18500.75")
        viewModel.updateSelectedDate(LocalDate.of(2026, 11, 1))
        viewModel.updateSelectedTime(14, 30)

        viewModel.saveAndExportToStorage()
        assertEquals(1, fakeRepository.exportedCount)

        // Start a brand new sheet: Previous Balance & Previous MSA should auto-populate from saved record
        viewModel.startNewSheet()
        val nextSheet = viewModel.uiState.value
        assertEquals("40000", nextSheet.previousBalanceInput)
        assertEquals("18500.75", nextSheet.previousMsaInput)

        // Manual override remains supported
        viewModel.updatePreviousBalance("39500")
        assertEquals("39500", viewModel.uiState.value.previousBalanceInput)
    }

    @Test
    fun `CSV export filename and content format match specification`() = runTest {
        val date = LocalDate.of(2026, 11, 1)
        val time = LocalTime.of(9, 5)
        val fileName = NumberFormatUtils.buildExportFileName(date, time, "csv")
        assertEquals("MFS_Tracker_2026-11-01_0905.csv", fileName)

        viewModel.updateSelectedDate(date)
        viewModel.updateSelectedTime(9, 5)
        viewModel.updateMfsBalance("1A", "10000")
        viewModel.updateMfsBalance("4", "20500.5")
        val firstRowId = viewModel.uiState.value.logRows.first().id
        viewModel.updateLogRowDescription(firstRowId, "Tea, Snacks")
        viewModel.updateLogRowAmount(firstRowId, "150")
        viewModel.updateMsa("5000")
        viewModel.saveRecord()

        val saved = fakeRepository.getLatestRecord()!!
        val csv = CsvExporter.buildCsvContent(saved)
        assertTrue(csv.contains("MFS FUND & EXPENSE TRACKER"))
        assertTrue(csv.contains("=\"01605001886\""))
        assertTrue(csv.contains("TOTAL MFS BALANCES,,,30500.50"))
        assertTrue(csv.contains("\"Tea, Snacks\""))
        assertTrue(csv.contains("Available Balance,30500.50"))
        assertTrue(csv.contains("MSA,5000.00"))

        // Verify Room TypeConverters round-trip
        val converters = TrackerTypeConverters()
        val mfsJson = converters.fromMfsBalances(saved.mfsBalances)
        assertEquals(saved.mfsBalances, converters.toMfsBalances(mfsJson))
        val txJson = converters.fromTransactions(saved.transactions)
        assertEquals(saved.transactions, converters.toTransactions(txJson))
    }

    private class FakeTrackerRepository : TrackerRepository {
        private val recordsState = MutableStateFlow<List<TrackerRecordEntity>>(emptyList())
        private val darkState = MutableStateFlow<Boolean?>(null)
        private var nextId = 1L
        var exportedCount = 0

        override val allRecordsFlow: Flow<List<TrackerRecordEntity>> = recordsState
        override val isDarkModeFlow: Flow<Boolean?> = darkState

        override suspend fun getLatestRecord(): TrackerRecordEntity? =
            recordsState.value.firstOrNull()

        override suspend fun getRecordById(id: Long): TrackerRecordEntity? =
            recordsState.value.firstOrNull { it.id == id }

        override suspend fun saveRecord(record: TrackerRecordEntity): Long {
            val assignedId = if (record.id == 0L) nextId++ else record.id
            val entity = record.copy(id = assignedId)
            val updated = (recordsState.value.filterNot { it.id == assignedId } + entity)
                .sortedWith(compareByDescending<TrackerRecordEntity> { it.timestampMillis }.thenByDescending { it.id })
            recordsState.value = updated
            return assignedId
        }

        override suspend fun deleteRecord(id: Long) {
            recordsState.value = recordsState.value.filterNot { it.id == id }
        }

        override suspend fun setDarkMode(enabled: Boolean) {
            darkState.value = enabled
        }

        override suspend fun exportRecordToDocuments(record: TrackerRecordEntity): Result<ExportResult> {
            exportedCount++
            return Result.success(
                ExportResult(
                    fileName = "MFS_Tracker_test.csv",
                    displayLocation = "Documents/MFS_Tracker/MFS_Tracker_test.csv"
                )
            )
        }

        override suspend fun exportRecordToSafUri(
            uri: Uri,
            record: TrackerRecordEntity
        ): Result<String> = Result.success("custom.csv")
    }
}

