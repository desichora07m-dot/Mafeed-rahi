package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.CustomerDao
import com.example.data.dao.PaymentTransactionDao
import com.example.data.dao.WorkCategoryDao
import com.example.data.dao.WorkEntryDao
import com.example.data.model.Customer
import com.example.data.model.PaymentStatus
import com.example.data.model.PaymentTransaction
import com.example.data.model.RateUnit
import com.example.data.model.WorkCategory
import com.example.data.model.WorkEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Customer::class,
        WorkCategory::class,
        WorkEntry::class,
        PaymentTransaction::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun workCategoryDao(): WorkCategoryDao
    abstract fun workEntryDao(): WorkEntryDao
    abstract fun paymentTransactionDao(): PaymentTransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tractor_hisab.db"
                )
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val DEFAULT_CATEGORIES = listOf(
            WorkCategory(
                key = "cultivator",
                nameEn = "Cultivator",
                nameHi = "Cultivator",
                drawableName = "img_cultivator",
                defaultRate = 15.0,
                defaultUnit = RateUnit.PER_MINUTE.id
            ),
            WorkCategory(
                key = "kartal",
                nameEn = "Kartal",
                nameHi = "Kartal",
                drawableName = "img_kartal",
                defaultRate = 15.0,
                defaultUnit = RateUnit.PER_MINUTE.id
            ),
            WorkCategory(
                key = "rotavator",
                nameEn = "Rotavator",
                nameHi = "Rotavator",
                drawableName = "img_rotavator",
                defaultRate = 15.0,
                defaultUnit = RateUnit.PER_MINUTE.id
            ),
            WorkCategory(
                key = "trolley",
                nameEn = "Tractor Trolley",
                nameHi = "Tractor Trolley",
                drawableName = "img_trolley",
                defaultRate = 600.0,
                defaultUnit = RateUnit.FIXED.id
            ),
            WorkCategory(
                key = "mitti_cutting",
                nameEn = "Mitti Cutting",
                nameHi = "Mitti Cutting",
                drawableName = "img_mitti_cutting",
                defaultRate = 900.0,
                defaultUnit = RateUnit.PER_HOUR.id
            )
        )
    }

    private class DatabaseCallback(private val appContext: Context) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = getDatabase(appContext)
                    populateInitialData(database)
                } catch (e: Exception) {
                    android.util.Log.e("AppDatabase", "Error initializing database data", e)
                }
            }
        }
    }
}

suspend fun populateInitialData(database: AppDatabase) {
    try {
        val categoryDao = database.workCategoryDao()
        val customerDao = database.customerDao()
        val workEntryDao = database.workEntryDao()
        val paymentDao = database.paymentTransactionDao()

        if (categoryDao.getCount() == 0) {
            categoryDao.insertAll(AppDatabase.DEFAULT_CATEGORIES)
        }

    // Seed initial customers for immediate usability
    val c1Id = customerDao.insert(Customer(name = "Babulal Patel", phone = "9876543210", village = "Kheda"))
    val c2Id = customerDao.insert(Customer(name = "Rameshwar Sharma", phone = "9823456789", village = "Rampur"))
    val c3Id = customerDao.insert(Customer(name = "Suresh Yadav", phone = "9898765432", village = "Shivgarh"))

    val now = System.currentTimeMillis()
    val oneHourAgo = now - 3600000L
    val yesterday = now - 86400000L

    // Entry 1: Rotavator 20 min @ ₹15/min = ₹300 (Paid ₹300)
    val entry1Id = workEntryDao.insert(
        WorkEntry(
            customerId = c1Id,
            customerName = "Babulal Patel",
            categoryId = 3,
            categoryKey = "rotavator",
            categoryName = "Rotavator",
            categoryDrawable = "img_rotavator",
            durationMinutes = 20,
            rate = 15.0,
            rateUnit = RateUnit.PER_MINUTE.id,
            totalAmount = 300.0,
            amountPaid = 300.0,
            amountPending = 0.0,
            paymentStatus = PaymentStatus.PAID.id,
            workDateMillis = now,
            notes = "East field rotavator work"
        )
    )
    paymentDao.insert(
        PaymentTransaction(
            customerId = c1Id,
            customerName = "Babulal Patel",
            workEntryId = entry1Id,
            amount = 300.0,
            paymentMethod = "CASH",
            paymentDateMillis = now,
            notes = "Cash received for rotavator"
        )
    )

    // Entry 2: Cultivator 1 hour 30 min @ ₹15/min = 90 * 15 = ₹1350 (Partial paid ₹500, pending ₹850)
    val entry2Id = workEntryDao.insert(
        WorkEntry(
            customerId = c2Id,
            customerName = "Rameshwar Sharma",
            categoryId = 1,
            categoryKey = "cultivator",
            categoryName = "Cultivator",
            categoryDrawable = "img_cultivator",
            durationMinutes = 90,
            rate = 15.0,
            rateUnit = RateUnit.PER_MINUTE.id,
            totalAmount = 1350.0,
            amountPaid = 500.0,
            amountPending = 850.0,
            paymentStatus = PaymentStatus.PARTIAL.id,
            workDateMillis = oneHourAgo,
            notes = "East boundary field tilled"
        )
    )
    paymentDao.insert(
        PaymentTransaction(
            customerId = c2Id,
            customerName = "Rameshwar Sharma",
            workEntryId = entry2Id,
            amount = 500.0,
            paymentMethod = "UPI",
            paymentDateMillis = oneHourAgo,
            notes = "UPI payment"
        )
    )

    // Entry 3: Trolley 2 trips @ ₹600 = ₹1200 (Unpaid, pending ₹1200)
    workEntryDao.insert(
        WorkEntry(
            customerId = c3Id,
            customerName = "Suresh Yadav",
            categoryId = 4,
            categoryKey = "trolley",
            categoryName = "Tractor Trolley",
            categoryDrawable = "img_trolley",
            durationMinutes = 60,
            rate = 1200.0,
            rateUnit = RateUnit.FIXED.id,
            totalAmount = 1200.0,
            amountPaid = 0.0,
            amountPending = 1200.0,
            paymentStatus = PaymentStatus.UNPAID.id,
            workDateMillis = yesterday,
            notes = "Sand delivery to new house site"
        )
    )
    } catch (e: Exception) {
        android.util.Log.e("AppDatabase", "Error in populateInitialData", e)
    }
}
