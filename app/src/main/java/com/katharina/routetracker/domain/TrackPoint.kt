package com.katharina.routetracker.domain

import kotlinx.serialization.Serializable

/** Immutable snapshot of a single GPS fix. */
@Serializable
data class TrackPoint(val lat: Double, val lon: Double, val timestampMs: Long)
