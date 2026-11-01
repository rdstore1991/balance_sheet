package com.mfstracker.app.data.repository

import android.content.Context
import android.net.Uri
import com.mfstracker.app.data.local.TrackerDao
import com.mfstracker.app.data.local.TrackerRecordEntity
import com.mfstracker.app.data.preferences.ThemePreferences
import com.mfstracker.app.util.CsvExporter
import com.mfstracker.app.util.ExportResult
import kotlinx.coroutines.flow.Flow

interface TrackerRepository {
    val allRecordsFlow: Flow<List<TrackerRecordEntity>>
    val isDarkModeFlow: Flow<Boolean?>

    suspend fun getLatestRecord(): TrackerRecordEntity?
    suspend fun getRecordById(id: Long): TrackerRecordEntity?
    suspend fun saveRecord(record: TrackerRecordEntity): Long
    suspend fun deleteRecord(id: Long)
    suspend fun setDarkMode(enabled: Boolean)
    suspend fun exportRecordToDocuments(record: TrackerRecordEntity): Result<ExportResult>
    suspend fun exportRecordToSafUri(uri: Uri, record: TrackerRecordEntity): Result<String>
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

    override suspend fun setDarkMode(enabled: Boolean) {
        themePreferences.setDarkMode(enabled)
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
}

