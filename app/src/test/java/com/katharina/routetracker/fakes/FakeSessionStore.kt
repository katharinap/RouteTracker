package com.katharina.routetracker.fakes

import com.katharina.routetracker.data.SessionStore
import com.katharina.routetracker.domain.TrackingSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSessionStore : SessionStore {
    private var nextId = 1L
    private val db = mutableMapOf<Long, TrackingSession>()
    private val _sessions = MutableStateFlow<Map<Long, TrackingSession>>(emptyMap())

    override suspend fun save(session: TrackingSession): TrackingSession {
        val updated = if (session.id == 0L) {
            session.copy(id = nextId++)
        } else {
            session
        }
        db[updated.id] = updated
        _sessions.value = db.toMap()
        return updated
    }

    override suspend fun load(id: Long): TrackingSession? = db[id]

    override fun observeAll(): Flow<List<TrackingSession>> = 
        _sessions.map { it.values.toList().sortedByDescending { s -> s.startedAt } }
}
