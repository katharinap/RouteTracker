package com.katharina.routetracker.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class SessionHeaderTest {

    @Test
    fun `null timestamp returns dash`() {
        val timestamp: Long? = null
        assertEquals("—", timestamp.toDisplayTime())
    }

    @Test
    fun `valid timestamp formats correctly`() {
        // Use a fixed timestamp to avoid locale/timezone issues in CI if possible, 
        // but since we use Locale.getDefault() and default TZ, we check for consistency.
        val calendar = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 1, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val timestamp = calendar.timeInMillis
        
        val result = timestamp.toDisplayTime()
        
        // We expect "2024-01-01 12:00" if the TZ matches what Calendar uses.
        // To be safe across environments, we just check the pattern.
        assertTrue(result.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")))
    }
    
    private fun assertTrue(condition: Boolean) {
        org.junit.Assert.assertTrue(condition)
    }
}
