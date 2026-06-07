package com.katharina.routetracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.katharina.routetracker.domain.TrackPoint
import com.katharina.routetracker.domain.TrackingSession
import com.katharina.routetracker.domain.TrackingState
import com.katharina.routetracker.repository.TrackingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** UI-focused snapshot of the current session state. */
data class SessionUiState(
    val startedAt: Long? = null,
    val stoppedAt: Long? = null,
    val trackingState: TrackingState = TrackingState.IDLE,
    val points: List<TrackPoint> = emptyList(),
    val error: String? = null,
    val hasActiveSession: Boolean = false,
)

@HiltViewModel
class SessionViewModel @Inject constructor(private val repo: TrackingRepository) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SessionUiState> = combine(
        repo.session,
        _error
    ) { session, error ->
        session.toUiState().copy(error = error)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SessionUiState()
    )

    fun createSession() = safeLaunch { repo.createSession() }
    fun start() = safeLaunch { repo.start() }
    fun pause() = safeLaunch { repo.pause() }
    fun resume() = safeLaunch { repo.resume() }
    fun stop() = safeLaunch { repo.stop() }

    fun clearError() {
        _error.value = null
    }

    private fun safeLaunch(block: suspend () -> Unit) = viewModelScope.launch {
        try {
            _error.value = null
            block()
        } catch (e: Exception) {
            _error.value = e.message ?: "An unexpected error occurred"
        }
    }

    private fun TrackingSession?.toUiState() = if (this == null) {
        SessionUiState()
    } else {
        SessionUiState(
            startedAt = startedAt,
            stoppedAt = stoppedAt,
            trackingState = state,
            points = points,
            hasActiveSession = true
        )
    }
}
