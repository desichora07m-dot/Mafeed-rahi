package com.example

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.model.Customer
import com.example.data.model.PaymentStatus
import com.example.data.model.RateUnit
import com.example.data.model.WorkCategory
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

        // Test customer creation with ONLY name (Phase 4)
        val customerId = repo.insertCustomer(Customer(name = "Kisan Singh"))
        assertTrue(customerId > 0)

        // Test flexible calculations:
        // Example 1 — Per Minute: 47 min @ ₹15/min = ₹705
        val totalPerMin = FormatUtils.calculateTotalAmount(47, 15.0, RateUnit.PER_MINUTE)
        assertEquals(705.0, totalPerMin, 0.001)

        // Example 2 — Per Hour: 2 hours (120 min) @ ₹800/hr = ₹1,600
        val totalPerHour = FormatUtils.calculateTotalAmount(120, 800.0, RateUnit.PER_HOUR)
        assertEquals(1600.0, totalPerHour, 0.001)

        // Example 3 — Fixed Amount: ₹1,500
        val totalFixed = FormatUtils.calculateTotalAmount(60, 1500.0, RateUnit.FIXED)
        assertEquals(1500.0, totalFixed, 0.001)

        val workEntry = WorkEntry(
            customerId = customerId,
            customerName = "Kisan Singh",
            categoryId = 1,
            categoryKey = "cultivator",
            categoryName = "Cultivator",
            categoryDrawable = "img_cultivator",
            durationMinutes = 47,
            rate = 15.0,
            rateUnit = RateUnit.PER_MINUTE.id,
            totalAmount = totalPerMin,
            amountPaid = 300.0,
            amountPending = 405.0,
            paymentStatus = PaymentStatus.PARTIAL.id,
            workDateMillis = System.currentTimeMillis(),
            notes = "East field tested"
        )
        val entryId = repo.saveWorkEntry(workEntry, "CASH")
        assertTrue(entryId > 0)

        // Verify customer ledger calculates balance accurately (705 - 300 = 405 pending)
        val ledgers = repo.customerLedgers.first()
        val customerLedger = ledgers.find { it.customer.id == customerId }
        assertNotNull(customerLedger)
        assertEquals(405.0, customerLedger!!.remainingBalance, 0.001)
        assertEquals(705.0, customerLedger.totalBillAmount, 0.001)
        assertEquals(300.0, customerLedger.totalPaidAmount, 0.001)

        // Test CSV generation
        val csv = repo.exportWorkEntriesCsv()
        assertTrue(csv.contains("Kisan Singh"))
        assertTrue(csv.contains("Cultivator"))

        // Test visible categories (only the 5 default categories + any custom, excluding removed ones)
        val visibleCats = repo.visibleCategories.first()
        val keys = visibleCats.map { it.key }
        assertTrue(keys.contains("cultivator"))
        assertTrue(keys.contains("kartal"))
        assertTrue(keys.contains("rotavator"))
        assertTrue(keys.contains("trolley"))
        assertTrue(keys.contains("mitti_cutting"))
        assertTrue(!keys.contains("land_leveler"))
        assertTrue(!keys.contains("paddy_thresher"))
        assertTrue(!keys.contains("maize_thresher"))
    }

    @Test
    fun testCustomCategoryCreationAndPersistence() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = TractorRepository.getRepository(context)

        val customCategory = WorkCategory(
            key = "custom_deep_plough",
            nameEn = "Deep Ploughing",
            nameHi = "Deep Ploughing",
            drawableName = "ic_work_custom",
            defaultRate = 25.0,
            defaultUnit = RateUnit.PER_MINUTE.id,
            isCustom = true
        )
        val id = repo.saveCategory(customCategory)
        assertTrue(id > 0)

        val visibleCats = repo.visibleCategories.first()
        assertTrue(visibleCats.any { it.key == "custom_deep_plough" })
    }
}
