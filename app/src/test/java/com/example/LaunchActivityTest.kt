package com.example

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.model.Customer
import com.example.data.model.PaymentStatus
import com.example.data.model.RateUnit
import com.example.data.model.WorkEntry
import com.example.data.repository.TractorRepository
import com.example.util.FormatUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LaunchActivityTest {

    @Test
    fun testLaunchMainActivity() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertNotNull(activity)
                assertEquals("Tractor Hisab", activity.getString(R.string.app_name))
            }
        }
    }

    @Test
    fun testOfflineDatabaseAndCalculations() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = TractorRepository.getRepository(context)

        // Verify default categories seeded
        val categories = repo.allCategories.first()
        assertTrue(categories.isNotEmpty())

        // Test customer creation
        val customerId = repo.insertCustomer(Customer(name = "Kisan Singh", phone = "9876500000", village = "Khedi"))
        assertTrue(customerId > 0)

        // Test WorkEntry creation with live calculation: 45 min @ ₹15/min = ₹675
        val durationMins = 45
        val rate = 15.0
        val total = FormatUtils.calculateTotalAmount(durationMins, rate, RateUnit.PER_MINUTE)
        assertEquals(675.0, total, 0.001)

        val workEntry = WorkEntry(
            customerId = customerId,
            customerName = "Kisan Singh",
            categoryId = 1,
            categoryKey = "cultivator",
            categoryName = "Cultivator",
            categoryDrawable = "ic_work_cultivator",
            durationMinutes = durationMins,
            rate = rate,
            rateUnit = RateUnit.PER_MINUTE.id,
            totalAmount = total,
            amountPaid = 300.0,
            amountPending = 375.0,
            paymentStatus = PaymentStatus.PARTIAL.id,
            workDateMillis = System.currentTimeMillis(),
            notes = "East field tested"
        )
        val entryId = repo.saveWorkEntry(workEntry, "CASH")
        assertTrue(entryId > 0)

        // Verify customer ledger calculates balance accurately (675 - 300 = 375 pending)
        val ledgers = repo.customerLedgers.first()
        val customerLedger = ledgers.find { it.customer.id == customerId }
        assertNotNull(customerLedger)
        assertEquals(375.0, customerLedger!!.remainingBalance, 0.001)
        assertEquals(675.0, customerLedger.totalBillAmount, 0.001)
        assertEquals(300.0, customerLedger.totalPaidAmount, 0.001)
    }
}
