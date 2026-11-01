package com.mfstracker.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerDao {

    @Query("SELECT * FROM tracker_records ORDER BY timestampMillis DESC, id DESC")
    fun observeAllRecords(): Flow<List<TrackerRecordEntity>>

    @Query("SELECT * FROM tracker_records ORDER BY timestampMillis DESC, id DESC LIMIT 1")
    suspend fun getLatestRecord(): TrackerRecordEntity?

    @Query("SELECT * FROM tracker_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Long): TrackerRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: TrackerRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<TrackerRecordEntity>): List<Long>

    @Update
    suspend fun updateRecord(record: TrackerRecordEntity)

    @Query("DELETE FROM tracker_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM tracker_records")
    suspend fun deleteAllRecords()
}

