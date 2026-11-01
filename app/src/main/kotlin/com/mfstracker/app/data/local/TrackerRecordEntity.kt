package com.mfstracker.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.mfstracker.app.data.model.ExpenseIncomeItem
import com.mfstracker.app.data.model.MfsAccountBalance
import com.mfstracker.app.data.model.TransactionType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "tracker_records")
data class TrackerRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestampMillis: Long,
    val dateString: String,
    val timeString: String,
    val mfsBalances: List<MfsAccountBalance>,
    val transactions: List<ExpenseIncomeItem>,
    val availableBalance: Double,
    val previousBalance: Double,
    val msa: Double,
    val previousMsa: Double,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    val totalMfsBalance: Double
        get() = mfsBalances.sumOf { it.balance }

    val totalDailyExpenses: Double
        get() = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

    val totalDailyIncome: Double
        get() = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
}

class TrackerTypeConverters {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @TypeConverter
    fun fromMfsBalances(value: List<MfsAccountBalance>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toMfsBalances(raw: String): List<MfsAccountBalance> {
        if (raw.isBlank()) return emptyList()
        return runCatching { json.decodeFromString<List<MfsAccountBalance>>(raw) }
            .getOrDefault(emptyList())
    }

    @TypeConverter
    fun fromTransactions(value: List<ExpenseIncomeItem>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toTransactions(raw: String): List<ExpenseIncomeItem> {
        if (raw.isBlank()) return emptyList()
        return runCatching { json.decodeFromString<List<ExpenseIncomeItem>>(raw) }
            .getOrDefault(emptyList())
    }
}

