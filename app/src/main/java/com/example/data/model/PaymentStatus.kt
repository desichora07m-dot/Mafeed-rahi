package com.example.data.model

enum class PaymentStatus(val id: String, val labelEn: String, val labelHi: String) {
    PAID("paid", "Paid", "पूरा जमा"),
    UNPAID("unpaid", "Unpaid", "बकाया"),
    PARTIAL("partial", "Partially Paid", "आंशिक जमा");

    companion object {
        fun fromId(id: String?): PaymentStatus = entries.find { it.id.equals(id, ignoreCase = true) } ?: UNPAID
    }
}
