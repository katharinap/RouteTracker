package com.katharina.routetracker.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class TrackPointTest {

    @Test
    fun `distance between same points is zero`() {
        val p = TrackPoint(52.52, 13.405, 0)
        assertEquals(0.0, p.distanceTo(p), 0.001)
    }

    @Test
    fun `distance between known points matches expected value`() {
        // Berlin to Potsdam approx 27km
        val berlin = TrackPoint(52.5200, 13.4050, 0)
        val potsdam = TrackPoint(52.3906, 13.0645, 0)
        
        val distance = berlin.distanceTo(potsdam)
        
        // Expected value from reliable online calculator: ~27160 meters
        assertEquals(27160.0, distance, 50.0)
    }
}
