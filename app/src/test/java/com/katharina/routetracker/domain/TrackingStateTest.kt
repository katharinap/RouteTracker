package com.katharina.routetracker.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackingStateTest {

    @Test
    fun `IDLE shows only start button`() {
        val controls = TrackingState.IDLE.toControlsState()
        assertTrue(controls.showStart)
        assertFalse(controls.showPause)
        assertFalse(controls.showResume)
        assertFalse(controls.showStop)
    }

    @Test
    fun `TRACKING shows pause and stop buttons`() {
        val controls = TrackingState.TRACKING.toControlsState()
        assertFalse(controls.showStart)
        assertTrue(controls.showPause)
        assertFalse(controls.showResume)
        assertTrue(controls.showStop)
    }

    @Test
    fun `PAUSED shows resume and stop buttons`() {
        val controls = TrackingState.PAUSED.toControlsState()
        assertFalse(controls.showStart)
        assertFalse(controls.showPause)
        assertTrue(controls.showResume)
        assertTrue(controls.showStop)
    }

    @Test
    fun `STOPPED shows no buttons`() {
        val controls = TrackingState.STOPPED.toControlsState()
        assertFalse(controls.showStart)
        assertFalse(controls.showPause)
        assertFalse(controls.showResume)
        assertFalse(controls.showStop)
    }
}
