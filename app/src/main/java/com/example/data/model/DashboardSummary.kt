package com.example.data.model

data class DashboardSummary(
    val totalWorkingMinutes: Int = 0,
    val totalEarnings: Double = 0.0,
    val totalCustomersServed: Int = 0,
    val totalPaymentsReceived: Double = 0.0,
    val totalPendingPayments: Double = 0.0,
    val totalWorkEntriesCount: Int = 0,
    val weeklyWorkingMinutes: Int = 0,
    val monthlyWorkingMinutes: Int = 0,
    val weeklyEarnings: Double = 0.0,
    val monthlyEarnings: Double = 0.0
)
