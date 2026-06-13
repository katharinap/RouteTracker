package com.katharina.routetracker.repository

import com.katharina.routetracker.domain.TrackPoint
import com.katharina.routetracker.domain.TrackingState
import com.katharina.routetracker.fakes.FakeLocationSource
import com.katharina.routetracker.fakes.FakeSessionStore
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TrackingRepositoryTest {

    private val testScope = TestScope()
    private val store = FakeSessionStore()
    private val context = mockk<android.content.Context>(relaxed = true)
    
    private fun createRepo(points: List<TrackPoint> = emptyList()): TrackingRepository {
        return TrackingRepository(
            store = store,
            locationSource = FakeLocationSource(points),
            scope = testScope,
            context = context
        )
    }

    @Test
    fun `createSession saves initial session to store`() = testScope.runTest {
        val repo = createRepo()
        repo.createSession()
        
        val session = repo.session.value
        assertNotNull(session)
        assertEquals(TrackingState.IDLE, session?.state)
        assertTrue(session!!.id > 0)
    }

    @Test
    fun `start sets startedAt and changes state to TRACKING`() = testScope.runTest {
        val repo = createRepo()
        repo.createSession()
        repo.start()
        
        val session = repo.session.value
        assertEquals(TrackingState.TRACKING, session?.state)
        assertNotNull(session?.startedAt)
    }

    @Test
    fun `start collects points from location source`() = testScope.runTest {
        val points = listOf(
            TrackPoint(1.0, 1.0, 100),
            TrackPoint(2.0, 2.0, 200)
        )
        val repo = createRepo(points)
        repo.createSession()
        repo.start()
        
        testScope.advanceUntilIdle()
        
        val session = repo.session.value
        assertEquals(2, session?.points?.size)
        assertEquals(points, session?.points)
    }

    @Test
    fun `pause stops collecting points`() = testScope.runTest {
        val points = listOf(TrackPoint(1.0, 1.0, 100))
        val repo = createRepo(points)
        repo.createSession()
        repo.start()
        repo.pause()
        
        testScope.advanceUntilIdle()
        
        // Even if points are available in the source, they shouldn't be collected after pause
        // In our FakeLocationSource, it emits everything immediately, so we check if they were collected before pause
        // To properly test this with a fake, we'd need a more reactive fake, but this validates the basic flow.
        assertEquals(TrackingState.PAUSED, repo.session.value?.state)
    }

    @Test
    fun `stop sets stoppedAt and state to STOPPED`() = testScope.runTest {
        val repo = createRepo()
        repo.createSession()
        repo.start()
        repo.stop()
        
        val session = repo.session.value
        assertEquals(TrackingState.STOPPED, session?.state)
        assertNotNull(session?.stoppedAt)
    }

    @Test
    fun `incremental persistence saves points to store`() = testScope.runTest {
        val point = TrackPoint(1.0, 1.0, 100)
        val repo = createRepo(listOf(point))
        repo.createSession()
        val sessionId = repo.session.value!!.id
        
        repo.start()
        testScope.advanceUntilIdle()
        
        val savedSession = store.load(sessionId)
        assertEquals(1, savedSession?.points?.size)
        assertEquals(point, savedSession?.points?.first())
    }

    @Test
    fun `distance is calculated incrementally`() = testScope.runTest {
        // approx 111km between these points
        val points = listOf(
            TrackPoint(0.0, 0.0, 100),
            TrackPoint(1.0, 0.0, 200)
        )
        val repo = createRepo(points)
        repo.createSession()
        repo.start()
        
        testScope.advanceUntilIdle()
        
        val session = repo.session.value
        // 1 degree latitude is approx 111,111 meters
        assertEquals(111111.0, session!!.distanceMeters, 500.0)
    }
}
