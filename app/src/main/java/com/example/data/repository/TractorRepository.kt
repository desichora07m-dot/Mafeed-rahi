package com.example.data.repository

import android.content.Context
import com.example.data.AppDatabase
import com.example.data.dao.CustomerDao
import com.example.data.dao.PaymentTransactionDao
import com.example.data.dao.WorkCategoryDao
import com.example.data.dao.WorkEntryDao
import com.example.data.model.Customer
import com.example.data.model.CustomerLedgerSummary
import com.example.data.model.DashboardSummary
import com.example.data.model.PaymentStatus
import com.example.data.model.PaymentTransaction
import com.example.data.model.RateUnit
import com.example.data.model.WorkCategory
import com.example.data.model.WorkEntry
import com.example.data.populateInitialData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class TractorRepository(
    private val database: AppDatabase,
    private val customerDao: CustomerDao = database.customerDao(),
    private val categoryDao: WorkCategoryDao = database.workCategoryDao(),
    private val workEntryDao: WorkEntryDao = database.workEntryDao(),
    private val paymentDao: PaymentTransactionDao = database.paymentTransactionDao()
) {
    init {
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            try {
                if (categoryDao.getCount() == 0) {
                    categoryDao.insertAll(AppDatabase.DEFAULT_CATEGORIES)
                }
            } catch (e: Exception) {
                android.util.Log.e("TractorRepository", "Error ensuring default categories", e)
            }
        }
    }

    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val allCategories: Flow<List<WorkCategory>> = categoryDao.getAllCategories()
    val allWorkEntries: Flow<List<WorkEntry>> = workEntryDao.getAllWorkEntries()
    val allPayments: Flow<List<PaymentTransaction>> = paymentDao.getAllPayments()

    // Combined Customer Ledgers with live balances
    val customerLedgers: Flow<List<CustomerLedgerSummary>> = combine(
        allCustomers,
        allWorkEntries,
        allPayments
    ) { customers, entries, payments ->
        customers.map { customer ->
            val customerEntries = entries.filter { it.customerId == customer.id }
            val customerPayments = payments.filter { it.customerId == customer.id }

            val totalMinutes = customerEntries.sumOf { it.durationMinutes }
            val totalBill = customerEntries.sumOf { it.totalAmount }
            val totalPaid = customerPayments.sumOf { it.amount }
            val balance = totalBill - totalPaid

            CustomerLedgerSummary(
                customer = customer,
                totalWorkMinutes = totalMinutes,
                totalBillAmount = totalBill,
                totalPaidAmount = totalPaid,
                remainingBalance = balance,
                totalWorkEntries = customerEntries.size,
                lastWorkDateMillis = customerEntries.maxOfOrNull { it.workDateMillis }
            )
        }
    }

    suspend fun findOrCreateCustomer(name: String, phone: String = "", village: String = ""): Customer = withContext(Dispatchers.IO) {
        val trimmed = name.trim()
        val existing = customerDao.findCustomerByName(trimmed)
        if (existing != null) {
            existing
        } else {
            val newCustomer = Customer(name = trimmed, phone = phone.trim(), village = village.trim())
            val id = customerDao.insert(newCustomer)
            newCustomer.copy(id = id)
        }
    }

    suspend fun saveWorkEntry(
        entry: WorkEntry,
        paymentMethod: String = "CASH"
    ): Long = withContext(Dispatchers.IO) {
        val entryId: Long
        if (entry.id == 0L) {
            entryId = workEntryDao.insert(entry)
            // If any payment was collected upon completing the work, log the transaction
            if (entry.amountPaid > 0) {
                paymentDao.insert(
                    PaymentTransaction(
                        customerId = entry.customerId,
                        customerName = entry.customerName,
                        workEntryId = entryId,
                        amount = entry.amountPaid,
                        paymentMethod = paymentMethod,
                        paymentDateMillis = entry.workDateMillis,
                        notes = "Payment received for ${entry.categoryName}"
                    )
                )
            }
        } else {
            entryId = entry.id
            workEntryDao.update(entry)
            // Synchronize existing payment linked to this work entry
            paymentDao.deleteByWorkEntryId(entry.id)
            if (entry.amountPaid > 0) {
                paymentDao.insert(
                    PaymentTransaction(
                        customerId = entry.customerId,
                        customerName = entry.customerName,
                        workEntryId = entry.id,
                        amount = entry.amountPaid,
                        paymentMethod = paymentMethod,
                        paymentDateMillis = entry.workDateMillis,
                        notes = "Payment updated for ${entry.categoryName}"
                    )
                )
            }
        }
        entryId
    }

    suspend fun deleteWorkEntry(entry: WorkEntry) = withContext(Dispatchers.IO) {
        paymentDao.deleteByWorkEntryId(entry.id)
        workEntryDao.delete(entry)
    }

    suspend fun addPayment(
        customerId: Long,
        customerName: String,
        amount: Double,
        method: String,
        dateMillis: Long,
        notes: String
    ): Long = withContext(Dispatchers.IO) {
        paymentDao.insert(
            PaymentTransaction(
                customerId = customerId,
                customerName = customerName,
                amount = amount,
                paymentMethod = method,
                paymentDateMillis = dateMillis,
                notes = notes
            )
        )
    }

    suspend fun deletePayment(payment: PaymentTransaction) = withContext(Dispatchers.IO) {
        paymentDao.delete(payment)
    }

    suspend fun insertCustomer(customer: Customer): Long = withContext(Dispatchers.IO) {
        customerDao.insert(customer)
    }

    suspend fun updateCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        customerDao.update(customer)
    }

    suspend fun deleteCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        workEntryDao.deleteByCustomerId(customer.id)
        paymentDao.deleteByCustomerId(customer.id)
        customerDao.delete(customer)
    }

    suspend fun saveCategory(category: WorkCategory): Long = withContext(Dispatchers.IO) {
        if (category.id == 0L) {
            categoryDao.insert(category)
        } else {
            categoryDao.update(category)
            category.id
        }
    }

    suspend fun deleteCategory(category: WorkCategory) = withContext(Dispatchers.IO) {
        if (category.isCustom) {
            categoryDao.delete(category)
        } else {
            categoryDao.softDelete(category.id)
        }
    }

    suspend fun resetDatabase() = withContext(Dispatchers.IO) {
        workEntryDao.deleteAll()
        paymentDao.deleteAll()
        customerDao.deleteAll()
        categoryDao.deleteAll()
        populateInitialData(database)
    }

    suspend fun exportDataAsJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "Tractor Hisab")
        root.put("exportTimestamp", System.currentTimeMillis())

        val customers = customerDao.getAllCustomers().first()
        val customersArr = JSONArray()
        for (c in customers) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("name", c.name)
            obj.put("phone", c.phone)
            obj.put("village", c.village)
            obj.put("notes", c.notes)
            obj.put("createdAt", c.createdAt)
            customersArr.put(obj)
        }
        root.put("customers", customersArr)

        val categories = categoryDao.getAllCategories().first()
        val catArr = JSONArray()
        for (c in categories) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("key", c.key)
            obj.put("nameEn", c.nameEn)
            obj.put("nameHi", c.nameHi)
            obj.put("drawableName", c.drawableName)
            obj.put("defaultRate", c.defaultRate)
            obj.put("defaultUnit", c.defaultUnit)
            obj.put("isCustom", c.isCustom)
            catArr.put(obj)
        }
        root.put("categories", catArr)

        val entries = workEntryDao.getAllWorkEntries().first()
        val entriesArr = JSONArray()
        for (e in entries) {
            val obj = JSONObject()
            obj.put("id", e.id)
            obj.put("customerId", e.customerId)
            obj.put("customerName", e.customerName)
            obj.put("categoryId", e.categoryId)
            obj.put("categoryKey", e.categoryKey)
            obj.put("categoryName", e.categoryName)
            obj.put("categoryDrawable", e.categoryDrawable)
            obj.put("durationMinutes", e.durationMinutes)
            obj.put("rate", e.rate)
            obj.put("rateUnit", e.rateUnit)
            obj.put("totalAmount", e.totalAmount)
            obj.put("amountPaid", e.amountPaid)
            obj.put("amountPending", e.amountPending)
            obj.put("paymentStatus", e.paymentStatus)
            obj.put("workDateMillis", e.workDateMillis)
            obj.put("notes", e.notes)
            entriesArr.put(obj)
        }
        root.put("workEntries", entriesArr)

        val payments = paymentDao.getAllPayments().first()
        val payArr = JSONArray()
        for (p in payments) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("customerId", p.customerId)
            obj.put("customerName", p.customerName)
            obj.put("workEntryId", p.workEntryId ?: JSONObject.NULL)
            obj.put("amount", p.amount)
            obj.put("paymentMethod", p.paymentMethod)
            obj.put("paymentDateMillis", p.paymentDateMillis)
            obj.put("notes", p.notes)
            payArr.put(obj)
        }
        root.put("payments", payArr)

        root.toString(2)
    }

    suspend fun importDataFromJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (root.has("customers")) {
                val customersArr = root.getJSONArray("customers")
                for (i in 0 until customersArr.length()) {
                    val obj = customersArr.getJSONObject(i)
                    customerDao.insert(
                        Customer(
                            name = obj.getString("name"),
                            phone = obj.optString("phone", ""),
                            village = obj.optString("village", ""),
                            notes = obj.optString("notes", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            if (root.has("workEntries")) {
                val entriesArr = root.getJSONArray("workEntries")
                for (i in 0 until entriesArr.length()) {
                    val obj = entriesArr.getJSONObject(i)
                    workEntryDao.insert(
                        WorkEntry(
                            customerId = obj.getLong("customerId"),
                            customerName = obj.getString("customerName"),
                            categoryId = obj.optLong("categoryId", 1),
                            categoryKey = obj.optString("categoryKey", "rotavator"),
                            categoryName = obj.getString("categoryName"),
                            categoryDrawable = obj.optString("categoryDrawable", "ic_work_rotavator"),
                            durationMinutes = obj.getInt("durationMinutes"),
                            rate = obj.getDouble("rate"),
                            rateUnit = obj.optString("rateUnit", RateUnit.PER_MINUTE.id),
                            totalAmount = obj.getDouble("totalAmount"),
                            amountPaid = obj.optDouble("amountPaid", 0.0),
                            amountPending = obj.optDouble("amountPending", 0.0),
                            paymentStatus = obj.optString("paymentStatus", PaymentStatus.UNPAID.id),
                            workDateMillis = obj.getLong("workDateMillis"),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
            }

            if (root.has("payments")) {
                val payArr = root.getJSONArray("payments")
                for (i in 0 until payArr.length()) {
                    val obj = payArr.getJSONObject(i)
                    paymentDao.insert(
                        PaymentTransaction(
                            customerId = obj.getLong("customerId"),
                            customerName = obj.getString("customerName"),
                            workEntryId = if (obj.isNull("workEntryId")) null else obj.optLong("workEntryId"),
                            amount = obj.getDouble("amount"),
                            paymentMethod = obj.optString("paymentMethod", "CASH"),
                            paymentDateMillis = obj.getLong("paymentDateMillis"),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    companion object {
        @Volatile
        private var REPO_INSTANCE: TractorRepository? = null

        fun getRepository(context: Context): TractorRepository {
            return REPO_INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getDatabase(context)
                val repo = TractorRepository(db)
                REPO_INSTANCE = repo
                repo
            }
        }
    }
}
