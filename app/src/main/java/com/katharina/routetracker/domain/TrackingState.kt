package com.katharina.routetracker.domain

/** All states a session can be in. */
enum class TrackingState { IDLE, TRACKING, PAUSED, STOPPED }

/** Computed visibility for UI buttons based on current state. */
data class ControlsState(
    val showStart: Boolean,
    val showPause: Boolean,
    val showResume: Boolean,
    val showStop: Boolean,
)

/**
 * Maps the internal tracking state to UI control visibility.
 * This is the single source of truth for button logic.
 */
fun TrackingState.toControlsState() = ControlsState(
    showStart = this == TrackingState.IDLE,
    showPause = this == TrackingState.TRACKING,
    showResume = this == TrackingState.PAUSED,
    showStop = (this == TrackingState.TRACKING || this == TrackingState.PAUSED),
)
