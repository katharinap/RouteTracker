package com.katharina.routetracker.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.katharina.routetracker.domain.TrackingSession
import com.katharina.routetracker.domain.TrackingState

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long?,
    val stoppedAt: Long?,
    val state: String,
    val pointsJson: String,
)

fun SessionEntity.toDomain(points: List<com.katharina.routetracker.domain.TrackPoint>): TrackingSession =
    TrackingSession(
        id = id,
        startedAt = startedAt,
        stoppedAt = stoppedAt,
        state = TrackingState.valueOf(state),
        points = points
    )

fun TrackingSession.toEntity(pointsJson: String): SessionEntity =
    SessionEntity(
        id = id,
        startedAt = startedAt,
        stoppedAt = stoppedAt,
        state = state.name,
        pointsJson = pointsJson
    )
