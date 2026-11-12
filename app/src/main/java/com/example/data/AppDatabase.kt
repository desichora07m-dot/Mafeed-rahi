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
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val DEFAULT_CATEGORIES = listOf(
            WorkCategory(
                key = "cultivator",
                nameEn = "Cultivator",
                nameHi = "कल्टीवेटर",
                drawableName = "ic_work_cultivator",
                defaultRate = 12.0,
                defaultUnit = RateUnit.PER_MINUTE.id
            ),
            WorkCategory(
                key = "kartal",
                nameEn = "Kartal",
                nameHi = "करताल / डिस्क",
                drawableName = "ic_work_kartal",
                defaultRate = 15.0,
                defaultUnit = RateUnit.PER_MINUTE.id
            ),
            WorkCategory(
                key = "rotavator",
                nameEn = "Rotavator",
                nameHi = "रोटावेटर",
                drawableName = "ic_work_rotavator",
                defaultRate = 18.0,
                defaultUnit = RateUnit.PER_MINUTE.id
            ),
            WorkCategory(
                key = "land_leveler",
                nameEn = "Land Leveler",
                nameHi = "लैंड लेवलर / पाटा",
                drawableName = "ic_work_land_leveler",
                defaultRate = 850.0,
                defaultUnit = RateUnit.PER_HOUR.id
            ),
            WorkCategory(
                key = "paddy_thresher",
                nameEn = "Paddy Thresher",
                nameHi = "धान थ्रेशर",
                drawableName = "ic_work_paddy_thresher",
                defaultRate = 20.0,
                defaultUnit = RateUnit.PER_MINUTE.id
            ),
            WorkCategory(
                key = "maize_thresher",
                nameEn = "Maize Thresher",
                nameHi = "मक्का थ्रेशर",
                drawableName = "ic_work_maize_thresher",
                defaultRate = 22.0,
                defaultUnit = RateUnit.PER_MINUTE.id
            ),
            WorkCategory(
                key = "trolley",
                nameEn = "Tractor Trolley",
                nameHi = "ट्रैक्टर ट्रॉली",
                drawableName = "ic_work_trolley",
                defaultRate = 600.0,
                defaultUnit = RateUnit.FIXED.id
            ),
            WorkCategory(
                key = "mitti_karna",
                nameEn = "Mitti Karna",
                nameHi = "मिट्टी काटना",
                drawableName = "ic_work_mitti_karna",
                defaultRate = 950.0,
                defaultUnit = RateUnit.PER_HOUR.id
            )
        )
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(database)
                }
            }
        }
    }
}

suspend fun populateInitialData(database: AppDatabase) {
    val categoryDao = database.workCategoryDao()
    val customerDao = database.customerDao()
    val workEntryDao = database.workEntryDao()
    val paymentDao = database.paymentTransactionDao()

    if (categoryDao.getCount() == 0) {
        categoryDao.insertAll(AppDatabase.DEFAULT_CATEGORIES)
    }

    // Seed helpful initial customers for immediate usability & demo
    val c1Id = customerDao.insert(Customer(name = "Babulal Patel (बाबूलाल)", phone = "9876543210", village = "Kheda"))
    val c2Id = customerDao.insert(Customer(name = "Rameshwar Sharma (रामेश्वर)", phone = "9823456789", village = "Rampur"))
    val c3Id = customerDao.insert(Customer(name = "Suresh Yadav (सुरेश)", phone = "9898765432", village = "Shivgarh"))

    val now = System.currentTimeMillis()
    val oneHourAgo = now - 3600000L
    val yesterday = now - 86400000L

    // Entry 1: Rotavator 20 min @ ₹15/min = ₹300 (Paid ₹300)
    val entry1Id = workEntryDao.insert(
        WorkEntry(
            customerId = c1Id,
            customerName = "Babulal Patel (बाबूलाल)",
            categoryId = 3,
            categoryKey = "rotavator",
            categoryName = "Rotavator (रोटावेटर)",
            categoryDrawable = "ic_work_rotavator",
            durationMinutes = 20,
            rate = 15.0,
            rateUnit = RateUnit.PER_MINUTE.id,
            totalAmount = 300.0,
            amountPaid = 300.0,
            amountPending = 0.0,
            paymentStatus = PaymentStatus.PAID.id,
            workDateMillis = now,
            notes = "Khet no. 4 rotavator work done"
        )
    )
    paymentDao.insert(
        PaymentTransaction(
            customerId = c1Id,
            customerName = "Babulal Patel (बाबूलाल)",
            workEntryId = entry1Id,
            amount = 300.0,
            paymentMethod = "CASH",
            paymentDateMillis = now,
            notes = "Cash received for rotavator"
        )
    )

    // Entry 2: Cultivator 1 hour 30 min @ ₹12/min = 90 * 12 = ₹1080 (Partial paid ₹500, pending ₹580)
    val entry2Id = workEntryDao.insert(
        WorkEntry(
            customerId = c2Id,
            customerName = "Rameshwar Sharma (रामेश्वर)",
            categoryId = 1,
            categoryKey = "cultivator",
            categoryName = "Cultivator (कल्टीवेटर)",
            categoryDrawable = "ic_work_cultivator",
            durationMinutes = 90,
            rate = 12.0,
            rateUnit = RateUnit.PER_MINUTE.id,
            totalAmount = 1080.0,
            amountPaid = 500.0,
            amountPending = 580.0,
            paymentStatus = PaymentStatus.PARTIAL.id,
            workDateMillis = oneHourAgo,
            notes = "East boundary field tilled"
        )
    )
    paymentDao.insert(
        PaymentTransaction(
            customerId = c2Id,
            customerName = "Rameshwar Sharma (रामेश्वर)",
            workEntryId = entry2Id,
            amount = 500.0,
            paymentMethod = "UPI",
            paymentDateMillis = oneHourAgo,
            notes = "UPI payment via PhonePe"
        )
    )

    // Entry 3: Trolley 2 trips @ ₹600 = ₹1200 (Unpaid, pending ₹1200)
    workEntryDao.insert(
        WorkEntry(
            customerId = c3Id,
            customerName = "Suresh Yadav (सुरेश)",
            categoryId = 7,
            categoryKey = "trolley",
            categoryName = "Tractor Trolley (ट्रॉली)",
            categoryDrawable = "ic_work_trolley",
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
}
