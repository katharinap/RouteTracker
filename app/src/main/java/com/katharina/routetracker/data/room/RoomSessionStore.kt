package com.katharina.routetracker.data.room

import com.katharina.routetracker.data.SessionStore
import com.katharina.routetracker.domain.TrackingSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomSessionStore(
    private val dao: SessionDao,
    private val converter: TrackPointConverter,
) : SessionStore {

    override suspend fun save(session: TrackingSession): TrackingSession {
        val pointsJson = converter.fromTrackPointList(session.points)
        val entity = session.toEntity(pointsJson)
        val id = dao.save(entity)
        return session.copy(id = id)
    }

    override suspend fun load(id: Long): TrackingSession? {
        val entity = dao.load(id) ?: return null
        val points = converter.toTrackPointList(entity.pointsJson)
        return entity.toDomain(points)
    }

    override fun observeAll(): Flow<List<TrackingSession>> {
        return dao.observeAll().map { entities ->
            entities.map { entity ->
                val points = converter.toTrackPointList(entity.pointsJson)
                entity.toDomain(points)
            }
        }
    }
}
