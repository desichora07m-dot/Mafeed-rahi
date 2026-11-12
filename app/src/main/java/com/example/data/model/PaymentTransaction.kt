package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payment_transactions",
    indices = [
        Index("customerId"),
        Index("paymentDateMillis")
    ]
)
data class PaymentTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val workEntryId: Long? = null,
    val amount: Double,
    val paymentMethod: String = "CASH", // CASH, UPI, BANK, OTHER
    val paymentDateMillis: Long,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
