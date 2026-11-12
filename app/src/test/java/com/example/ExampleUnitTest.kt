package com.example

import com.example.data.model.RateUnit
import com.example.util.FormatUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testDurationAndPerMinuteRateCalculation() {
        // Example from prompt: 20 minutes @ ₹15/min = ₹300
        val amount = FormatUtils.calculateTotalAmount(
            durationMinutes = 20,
            rate = 15.0,
            rateUnit = RateUnit.PER_MINUTE
        )
        assertEquals(300.0, amount, 0.001)
    }

    @Test
    fun testPerHourRateCalculation() {
        // 1 hour 30 mins (90 mins) @ ₹800/hr = 1.5 * 800 = ₹1200
        val amount = FormatUtils.calculateTotalAmount(
            durationMinutes = 90,
            rate = 800.0,
            rateUnit = RateUnit.PER_HOUR
        )
        assertEquals(1200.0, amount, 0.001)
    }

    @Test
    fun testFixedRateCalculation() {
        val amount = FormatUtils.calculateTotalAmount(
            durationMinutes = 45,
            rate = 500.0,
            rateUnit = RateUnit.FIXED
        )
        assertEquals(500.0, amount, 0.001)
    }

    @Test
    fun testFormatDuration() {
        val formattedEn = FormatUtils.formatDuration(90, isHindi = false)
        assertEquals("1h 30m", formattedEn)

        val formattedHi = FormatUtils.formatDuration(90, isHindi = true)
        assertEquals("1 घंटा 30 मिनट", formattedHi)
    }
}
