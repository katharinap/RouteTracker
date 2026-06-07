package com.katharina.routetracker.fakes

import com.katharina.routetracker.data.SessionStore
import com.katharina.routetracker.domain.TrackingSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeSessionStore : SessionStore {
    private var nextId = 1L
    private val db = mutableMapOf<Long, TrackingSession>()

    override suspend fun save(session: TrackingSession): TrackingSession {
        val updated = if (session.id == 0L) {
            session.copy(id = nextId++)
        } else {
            session
        }
        db[updated.id] = updated
        return updated
    }

    override suspend fun load(id: Long): TrackingSession? = db[id]

    override fun observeAll(): Flow<List<TrackingSession>> = flowOf(db.values.toList())
}
