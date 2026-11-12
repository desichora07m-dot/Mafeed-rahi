package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WorkEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkEntryDao {
    @Query("SELECT * FROM work_entries ORDER BY workDateMillis DESC, createdAt DESC")
    fun getAllWorkEntries(): Flow<List<WorkEntry>>

    @Query("SELECT * FROM work_entries WHERE customerId = :customerId ORDER BY workDateMillis DESC")
    fun getWorkEntriesByCustomer(customerId: Long): Flow<List<WorkEntry>>

    @Query("SELECT * FROM work_entries WHERE workDateMillis >= :startMillis AND workDateMillis <= :endMillis ORDER BY workDateMillis DESC")
    fun getWorkEntriesByDateRange(startMillis: Long, endMillis: Long): Flow<List<WorkEntry>>

    @Query("SELECT * FROM work_entries WHERE id = :id")
    suspend fun getWorkEntryById(id: Long): WorkEntry?

    @Query("SELECT * FROM work_entries WHERE id = :id")
    fun observeWorkEntryById(id: Long): Flow<WorkEntry?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WorkEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<WorkEntry>)

    @Update
    suspend fun update(entry: WorkEntry)

    @Delete
    suspend fun delete(entry: WorkEntry)

    @Query("DELETE FROM work_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM work_entries WHERE customerId = :customerId")
    suspend fun deleteByCustomerId(customerId: Long)

    @Query("DELETE FROM work_entries")
    suspend fun deleteAll()
}
