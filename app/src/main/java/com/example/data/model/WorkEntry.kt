package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "work_entries",
    indices = [
        Index("customerId"),
        Index("workDateMillis")
    ]
)
data class WorkEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val categoryId: Long,
    val categoryKey: String,
    val categoryName: String,
    val categoryDrawable: String,
    val durationMinutes: Int, // Total duration stored accurately in minutes
    val rate: Double,
    val rateUnit: String = RateUnit.PER_MINUTE.id,
    val totalAmount: Double,
    val amountPaid: Double = 0.0,
    val amountPending: Double = 0.0,
    val paymentStatus: String = PaymentStatus.UNPAID.id,
    val workDateMillis: Long,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getParsedUnit(): RateUnit = RateUnit.fromId(rateUnit)
    fun getParsedStatus(): PaymentStatus = PaymentStatus.fromId(paymentStatus)

    val hours: Int get() = durationMinutes / 60
    val minutes: Int get() = durationMinutes % 60
}
