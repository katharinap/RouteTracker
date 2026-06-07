package com.katharina.routetracker.fakes

import com.katharina.routetracker.data.LocationSource
import com.katharina.routetracker.domain.TrackPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

class FakeLocationSource(private val points: List<TrackPoint>) : LocationSource {
    override val locations: Flow<TrackPoint> = points.asFlow()
}
