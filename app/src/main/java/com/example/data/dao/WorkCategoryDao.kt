package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WorkCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkCategoryDao {
    @Query("SELECT * FROM work_categories WHERE isDeleted = 0 ORDER BY id ASC")
    fun getAllCategories(): Flow<List<WorkCategory>>

    @Query("SELECT * FROM work_categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): WorkCategory?

    @Query("SELECT * FROM work_categories WHERE key = :key LIMIT 1")
    suspend fun getCategoryByKey(key: String): WorkCategory?

    @Query("SELECT COUNT(*) FROM work_categories")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: WorkCategory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<WorkCategory>)

    @Update
    suspend fun update(category: WorkCategory)

    @Delete
    suspend fun delete(category: WorkCategory)

    @Query("UPDATE work_categories SET isDeleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Long)

    @Query("DELETE FROM work_categories")
    suspend fun deleteAll()
}
