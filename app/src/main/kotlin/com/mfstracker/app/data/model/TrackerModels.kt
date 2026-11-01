package com.mfstracker.app.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class TransactionType(val label: String) {
    EXPENSE("Expense"),
    INCOME("Income")
}

@Serializable
data class MfsAccountBalance(
    val key: String,
    val slNo: Int,
    val accountNumber: String,
    val provider: String,
    val balance: Double = 0.0
)

@Serializable
data class ExpenseIncomeItem(
    val id: String,
    val type: TransactionType = TransactionType.EXPENSE,
    val description: String = "",
    val amount: Double = 0.0,
    val notes: String = ""
)

data class FixedMfsSpec(
    val key: String,
    val slNo: Int,
    val accountNumber: String,
    val provider: String
)

object FixedMfsAccounts {
    /**
     * Exact fixed 7 MFS account rows across 4 SL# groups as specified:
     * - SL# 1: 01605001886 -> Row 1A: Bkash | Row 1B: Nagad
     * - SL# 2: 01965972859 -> Row 2A: Bkash | Row 2B: Nagad
     * - SL# 3: 01842662901 -> Row 3A: Bkash | Row 3B: Nagad
     * - SL# 4: 01715385025 -> Row 4: Bkash Agent
     */
    val specs: List<FixedMfsSpec> = listOf(
        FixedMfsSpec(key = "1A", slNo = 1, accountNumber = "01605001886", provider = "Bkash"),
        FixedMfsSpec(key = "1B", slNo = 1, accountNumber = "01605001886", provider = "Nagad"),
        FixedMfsSpec(key = "2A", slNo = 2, accountNumber = "01965972859", provider = "Bkash"),
        FixedMfsSpec(key = "2B", slNo = 2, accountNumber = "01965972859", provider = "Nagad"),
        FixedMfsSpec(key = "3A", slNo = 3, accountNumber = "01842662901", provider = "Bkash"),
        FixedMfsSpec(key = "3B", slNo = 3, accountNumber = "01842662901", provider = "Nagad"),
        FixedMfsSpec(key = "4", slNo = 4, accountNumber = "01715385025", provider = "Bkash Agent")
    )

    const val DEFAULT_LOG_ROW_COUNT = 5
}

