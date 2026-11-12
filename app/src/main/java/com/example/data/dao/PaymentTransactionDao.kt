package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PaymentTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentTransactionDao {
    @Query("SELECT * FROM payment_transactions ORDER BY paymentDateMillis DESC, createdAt DESC")
    fun getAllPayments(): Flow<List<PaymentTransaction>>

    @Query("SELECT * FROM payment_transactions WHERE customerId = :customerId ORDER BY paymentDateMillis DESC")
    fun getPaymentsByCustomer(customerId: Long): Flow<List<PaymentTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: PaymentTransaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(payments: List<PaymentTransaction>)

    @Update
    suspend fun update(payment: PaymentTransaction)

    @Delete
    suspend fun delete(payment: PaymentTransaction)

    @Query("DELETE FROM payment_transactions WHERE workEntryId = :workEntryId")
    suspend fun deleteByWorkEntryId(workEntryId: Long)

    @Query("DELETE FROM payment_transactions WHERE customerId = :customerId")
    suspend fun deleteByCustomerId(customerId: Long)

    @Query("DELETE FROM payment_transactions")
    suspend fun deleteAll()
}
