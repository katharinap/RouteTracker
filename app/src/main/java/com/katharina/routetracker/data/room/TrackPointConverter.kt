package com.katharina.routetracker.data.room

import androidx.room.TypeConverter
import com.katharina.routetracker.domain.TrackPoint
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TrackPointConverter {
    @TypeConverter
    fun fromTrackPointList(value: List<TrackPoint>): String = Json.encodeToString(value)

    @TypeConverter
    fun toTrackPointList(value: String): List<TrackPoint> = try {
        Json.decodeFromString(value)
    } catch (e: Exception) {
        emptyList()
    }
}
