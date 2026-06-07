package com.katharina.routetracker.data

import com.katharina.routetracker.domain.TrackPoint
import kotlinx.coroutines.flow.Flow

/** Abstracts location hardware. */
interface LocationSource {
    /** Emits fixes only while collected; caller controls lifecycle. */
    val locations: Flow<TrackPoint>
}
