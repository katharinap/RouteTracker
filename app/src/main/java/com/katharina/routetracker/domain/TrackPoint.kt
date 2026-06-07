package com.katharina.routetracker.domain

/** Immutable snapshot of a single GPS fix. */
data class TrackPoint(val lat: Double, val lon: Double, val timestampMs: Long)
