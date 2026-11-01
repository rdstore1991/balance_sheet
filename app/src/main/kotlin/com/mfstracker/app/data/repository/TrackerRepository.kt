package com.mfstracker.app.data.repository

import android.content.Context
import android.net.Uri
import com.mfstracker.app.data.local.TrackerDao
import com.mfstracker.app.data.local.TrackerRecordEntity
import com.mfstracker.app.data.preferences.CurrencyFormat
import com.mfstracker.app.data.preferences.ThemeMode
import com.mfstracker.app.data.preferences.ThemePreferences
import com.mfstracker.app.util.CsvExporter
import com.mfstracker.app.util.ExportResult
import kotlinx.coroutines.flow.Flow

interface TrackerRepository {
    val allRecordsFlow: Flow<List<TrackerRecordEntity>>
    val isDarkModeFlow: Flow<Boolean?>
    val themeModeFlow: Flow<ThemeMode>
    val currencyFormatFlow: Flow<CurrencyFormat>

    suspend fun getLatestRecord(): TrackerRecordEntity?
    suspend fun getRecordById(id: Long): TrackerRecordEntity?
    suspend fun saveRecord(record: TrackerRecordEntity): Long
    suspend fun deleteRecord(id: Long)
    suspend fun deleteAllRecords()

    suspend fun setDarkMode(enabled: Boolean)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setCurrencyFormat(format: CurrencyFormat)

    suspend fun exportRecordToDocuments(record: TrackerRecordEntity): Result<ExportResult>
    suspend fun exportRecordToSafUri(uri: Uri, record: TrackerRecordEntity): Result<String>
    suspend fun exportBatchRecordsToDocuments(
        records: List<TrackerRecordEntity>,
        rangeLabel: String
    ): Result<ExportResult>
    suspend fun exportBatchRecordsToSafUri(
        uri: Uri,
        records: List<TrackerRecordEntity>,
        rangeLabel: String
    ): Result<String>
}

class DefaultTrackerRepository(
    private val appContext: Context,
    private val trackerDao: TrackerDao,
    private val themePreferences: ThemePreferences
) : TrackerRepository {

    override val allRecordsFlow: Flow<List<TrackerRecordEntity>> =
        trackerDao.observeAllRecords()

    override val isDarkModeFlow: Flow<Boolean?> =
        themePreferences.isDarkModeFlow

    override val themeModeFlow: Flow<ThemeMode> =
        themePreferences.themeModeFlow

    override val currencyFormatFlow: Flow<CurrencyFormat> =
        themePreferences.currencyFormatFlow

    override suspend fun getLatestRecord(): TrackerRecordEntity? =
        trackerDao.getLatestRecord()

    override suspend fun getRecordById(id: Long): TrackerRecordEntity? =
        trackerDao.getRecordById(id)

    override suspend fun saveRecord(record: TrackerRecordEntity): Long {
        return if (record.id == 0L) {
            trackerDao.insertRecord(record)
        } else {
            trackerDao.updateRecord(record)
            record.id
        }
    }

    override suspend fun deleteRecord(id: Long) {
        trackerDao.deleteRecordById(id)
    }

    override suspend fun deleteAllRecords() {
        trackerDao.deleteAllRecords()
    }

    override suspend fun setDarkMode(enabled: Boolean) {
        themePreferences.setDarkMode(enabled)
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        themePreferences.setThemeMode(mode)
    }

    override suspend fun setCurrencyFormat(format: CurrencyFormat) {
        themePreferences.setCurrencyFormat(format)
    }

    override suspend fun exportRecordToDocuments(record: TrackerRecordEntity): Result<ExportResult> {
        return CsvExporter.exportToPublicDocuments(appContext, record)
    }

    override suspend fun exportRecordToSafUri(
        uri: Uri,
        record: TrackerRecordEntity
    ): Result<String> {
        return CsvExporter.exportToSafUri(appContext, uri, record)
    }

    override suspend fun exportBatchRecordsToDocuments(
        records: List<TrackerRecordEntity>,
        rangeLabel: String
    ): Result<ExportResult> {
        return CsvExporter.exportBatchToPublicDocuments(appContext, records, rangeLabel)
    }

    override suspend fun exportBatchRecordsToSafUri(
        uri: Uri,
        records: List<TrackerRecordEntity>,
        rangeLabel: String
    ): Result<String> {
        return CsvExporter.exportBatchToSafUri(appContext, uri, records, rangeLabel)
    }
}
