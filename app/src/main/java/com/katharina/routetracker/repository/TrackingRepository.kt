package com.katharina.routetracker.repository

import com.katharina.routetracker.data.LocationSource
import com.katharina.routetracker.data.SessionStore
import com.katharina.routetracker.domain.TrackingSession
import com.katharina.routetracker.domain.TrackingState
import com.katharina.routetracker.domain.withState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrackingRepository(
    private val store: SessionStore,
    private val locationSource: LocationSource,
    private val scope: CoroutineScope,
) {
    private val _session = MutableStateFlow<TrackingSession?>(null)
    val session: StateFlow<TrackingSession?> = _session.asStateFlow()

    private var locationJob: Job? = null

    suspend fun createSession() {
        _session.value = store.save(TrackingSession())
    }

    suspend fun start() = transition(TrackingState.TRACKING) {
        _session.update { it?.copy(startedAt = System.currentTimeMillis()) }
        startCollecting()
    }

    suspend fun pause() = transition(TrackingState.PAUSED) { stopCollecting() }
    suspend fun resume() = transition(TrackingState.TRACKING) { startCollecting() }

    suspend fun stop() = transition(TrackingState.STOPPED) {
        _session.update { it?.copy(stoppedAt = System.currentTimeMillis()) }
        stopCollecting()
    }

    private suspend fun transition(
        next: TrackingState,
        sideEffect: suspend () -> Unit = {},
    ) {
        val updated = requireSession().withState(next)
        sideEffect()
        // The sideEffect might have updated the _session locally (like startedAt), 
        // so we merge the state change into the latest value.
        val final = _session.value?.copy(state = next) ?: updated
        _session.value = store.save(final)
    }

    private fun startCollecting() {
        if (locationJob != null) return
        locationJob = scope.launch {
            locationSource.locations.collect { point ->
                _session.update { it?.copy(points = it.points + point) }
                // persist incrementally so no points are lost on crash
                _session.value?.let { store.save(it) }
            }
        }
    }

    private fun stopCollecting() {
        locationJob?.cancel()
        locationJob = null
    }

    private fun requireSession() =
        _session.value ?: error("No active session")
}
