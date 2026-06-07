package com.katharina.routetracker.data

import com.katharina.routetracker.domain.TrackingSession
import kotlinx.coroutines.flow.Flow

/** Abstracts persistence. */
interface SessionStore {
    /** Saves or updates a session. Returns the session with assigned ID. */
    suspend fun save(session: TrackingSession): TrackingSession

    /** Loads a specific session by ID. */
    suspend fun load(id: Long): TrackingSession?

    /** Observes all sessions stored in the database. */
    fun observeAll(): Flow<List<TrackingSession>>
}
