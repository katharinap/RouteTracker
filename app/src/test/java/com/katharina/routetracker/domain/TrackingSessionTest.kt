package com.katharina.routetracker.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class TrackingSessionTest {

    @Test
    fun `IDLE can transition to TRACKING`() {
        val session = TrackingSession(state = TrackingState.IDLE)
        val updated = session.withState(TrackingState.TRACKING)
        assertEquals(TrackingState.TRACKING, updated.state)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `IDLE cannot transition to PAUSED`() {
        TrackingSession(state = TrackingState.IDLE).withState(TrackingState.PAUSED)
    }

    @Test
    fun `TRACKING can transition to PAUSED or STOPPED`() {
        val session = TrackingSession(state = TrackingState.TRACKING)
        assertEquals(TrackingState.PAUSED, session.withState(TrackingState.PAUSED).state)
        assertEquals(TrackingState.STOPPED, session.withState(TrackingState.STOPPED).state)
    }

    @Test
    fun `PAUSED can transition to TRACKING or STOPPED`() {
        val session = TrackingSession(state = TrackingState.PAUSED)
        assertEquals(TrackingState.TRACKING, session.withState(TrackingState.TRACKING).state)
        assertEquals(TrackingState.STOPPED, session.withState(TrackingState.STOPPED).state)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `STOPPED cannot transition anywhere`() {
        TrackingSession(state = TrackingState.STOPPED).withState(TrackingState.TRACKING)
    }
}
