package com.katharina.routetracker.ui

import com.katharina.routetracker.domain.TrackPoint
import com.katharina.routetracker.domain.TrackingState
import com.katharina.routetracker.fakes.FakeLocationSource
import com.katharina.routetracker.fakes.FakeSessionStore
import com.katharina.routetracker.repository.TrackingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val store = FakeSessionStore()
    private val locationSource = FakeLocationSource(emptyList())
    
    private lateinit var repo: TrackingRepository
    private lateinit var viewModel: SessionViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = TrackingRepository(store, locationSource, testScope)
        viewModel = SessionViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty`() = testScope.runTest {
        val state = viewModel.uiState.value
        assertEquals(TrackingState.IDLE, state.trackingState)
        assertFalse(state.hasActiveSession)
        assertNull(state.error)
    }

    @Test
    fun `createSession updates hasActiveSession`() = testScope.runTest {
        viewModel.createSession()
        testScope.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state.hasActiveSession)
        assertEquals(TrackingState.IDLE, state.trackingState)
    }

    @Test
    fun `start updates tracking state`() = testScope.runTest {
        viewModel.createSession()
        testScope.advanceUntilIdle()
        
        viewModel.start()
        testScope.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(TrackingState.TRACKING, state.trackingState)
    }

    @Test
    fun `error in transition is caught and exposed`() = testScope.runTest {
        // IDLE -> PAUSED is invalid
        viewModel.createSession()
        testScope.advanceUntilIdle()
        
        viewModel.pause()
        testScope.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Invalid transition"))
    }

    @Test
    fun `clearError resets error state`() = testScope.runTest {
        viewModel.createSession()
        testScope.advanceUntilIdle()
        viewModel.pause() // causes error
        testScope.advanceUntilIdle()
        
        viewModel.clearError()
        testScope.advanceUntilIdle()
        
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `points are mapped to UI state`() = testScope.runTest {
        val points = listOf(TrackPoint(1.0, 1.0, 100))
        val reactiveLocationSource = FakeLocationSource(points)
        repo = TrackingRepository(store, reactiveLocationSource, testScope)
        viewModel = SessionViewModel(repo)
        
        viewModel.createSession()
        testScope.advanceUntilIdle()
        viewModel.start()
        testScope.advanceUntilIdle()
        
        assertEquals(1, viewModel.uiState.value.points.size)
    }
}
