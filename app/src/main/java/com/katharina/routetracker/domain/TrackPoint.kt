package com.katharina.routetracker.domain

import kotlinx.serialization.Serializable
import kotlin.math.*

/** Immutable snapshot of a single GPS fix. */
@Serializable
data class TrackPoint(val lat: Double, val lon: Double, val timestampMs: Long)

/**
 * Calculates the distance to another point in meters using the Haversine formula.
 */
fun TrackPoint.distanceTo(other: TrackPoint): Double {
    val earthRadiusMeters = 6371000.0
    val dLat = Math.toRadians(other.lat - lat)
    val dLon = Math.toRadians(other.lon - lon)
    val a = (sin(dLat / 2).pow(2)) +
            (cos(Math.toRadians(lat)) * cos(Math.toRadians(other.lat)) *
                    sin(dLon / 2).pow(2))
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadiusMeters * c
}
