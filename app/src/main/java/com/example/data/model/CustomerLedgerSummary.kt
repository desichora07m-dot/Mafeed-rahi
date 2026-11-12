package com.example.data.model

data class CustomerLedgerSummary(
    val customer: Customer,
    val totalWorkMinutes: Int = 0,
    val totalBillAmount: Double = 0.0,
    val totalPaidAmount: Double = 0.0,
    val remainingBalance: Double = 0.0,
    val totalWorkEntries: Int = 0,
    val lastWorkDateMillis: Long? = null
)
