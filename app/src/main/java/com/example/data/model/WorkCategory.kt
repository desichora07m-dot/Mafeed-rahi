package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_categories")
data class WorkCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val key: String,
    val nameEn: String,
    val nameHi: String,
    val drawableName: String,
    val defaultRate: Double = 15.0,
    val defaultUnit: String = RateUnit.PER_MINUTE.id,
    val isCustom: Boolean = false,
    val isDeleted: Boolean = false
) {
    fun getParsedUnit(): RateUnit = RateUnit.fromId(defaultUnit)
}
