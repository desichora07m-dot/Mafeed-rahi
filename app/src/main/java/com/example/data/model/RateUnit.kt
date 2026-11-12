package com.example.data.model

enum class RateUnit(val id: String, val labelEn: String, val labelHi: String, val shortUnitEn: String, val shortUnitHi: String) {
    PER_MINUTE("per_minute", "Per Minute", "प्रति मिनट", "min", "मिनट"),
    PER_HOUR("per_hour", "Per Hour", "प्रति घंटा", "hr", "घंटा"),
    FIXED("fixed", "Fixed Rate", "फिक्स दर", "fixed", "फिक्स");

    companion object {
        fun fromId(id: String?): RateUnit = entries.find { it.id.equals(id, ignoreCase = true) } ?: PER_MINUTE
    }
}
