package com.katharina.routetracker.domain

/** Full session; the single source of truth for a tracking activity. */
data class TrackingSession(
    val id: Long = 0,
    val startedAt: Long? = null,   // null until start() is called
    val stoppedAt: Long? = null,   // null until stop() is called
    val state: TrackingState = TrackingState.IDLE,
    val points: List<TrackPoint> = emptyList(),
    val distanceMeters: Double = 0.0,
)

/**
 * Validates and returns a copy of the session with the new state.
 * This is the only place transition legality is checked.
 */
fun TrackingSession.withState(next: TrackingState): TrackingSession {
    val allowed = mapOf(
        TrackingState.IDLE     to setOf(TrackingState.TRACKING),
        TrackingState.TRACKING to setOf(TrackingState.PAUSED, TrackingState.STOPPED),
        TrackingState.PAUSED   to setOf(TrackingState.TRACKING, TrackingState.STOPPED),
        TrackingState.STOPPED  to emptySet<TrackingState>(),
    )
    
    val possibleTransitions = allowed[state] ?: emptySet()
    require(next in possibleTransitions) {
        "Invalid transition: $state → $next"
    }
    
    return copy(state = next)
}
