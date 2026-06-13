package com.katharina.routetracker.repository

import android.content.Context
import android.content.Intent
import com.katharina.routetracker.data.LocationSource
import com.katharina.routetracker.data.SessionStore
import com.katharina.routetracker.domain.TrackingSession
import com.katharina.routetracker.domain.TrackingState
import com.katharina.routetracker.domain.distanceTo
import com.katharina.routetracker.domain.withState
import com.katharina.routetracker.service.TrackingService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class TrackingRepository @Inject constructor(
    private val store: SessionStore,
    private val locationSource: LocationSource,
    private val scope: CoroutineScope,
    @param:ApplicationContext private val context: Context,
) {
    private val _session = MutableStateFlow<TrackingSession?>(null)
    val session: StateFlow<TrackingSession?> = _session.asStateFlow()

    private var locationJob: Job? = null

    suspend fun createSession() {
        _session.value = store.save(TrackingSession())
    }

    suspend fun loadSession(id: Long) {
        _session.value = store.load(id)
    }

    fun observeAll() = store.observeAll()

    fun closeSession() {
        _session.value = null
    }

    suspend fun start() = transition(TrackingState.TRACKING) {
        _session.update { it?.copy(startedAt = System.currentTimeMillis()) }
        startService()
        startCollecting()
    }

    suspend fun pause() = transition(TrackingState.PAUSED) { stopCollecting() }
    suspend fun resume() = transition(TrackingState.TRACKING) { startCollecting() }

    suspend fun stop() = transition(TrackingState.STOPPED) {
        _session.update { it?.copy(stoppedAt = System.currentTimeMillis()) }
        stopCollecting()
        stopService()
    }

    private fun startService() {
        val intent = Intent(context, TrackingService::class.java)
        context.startForegroundService(intent)
    }

    private fun stopService() {
        val intent = Intent(context, TrackingService::class.java)
        context.stopService(intent)
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
                _session.update { session ->
                    session?.let {
                        val lastPoint = it.points.lastOrNull()
                        val newDistance = if (lastPoint != null) {
                            it.distanceMeters + lastPoint.distanceTo(point)
                        } else {
                            it.distanceMeters
                        }
                        it.copy(
                            points = it.points + point,
                            distanceMeters = newDistance
                        )
                    }
                }
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
