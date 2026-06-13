package com.katharina.routetracker.data

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.katharina.routetracker.domain.TrackPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FusedLocationSource(private val context: Context) : LocationSource {

    @SuppressLint("MissingPermission")
    override val locations: Flow<TrackPoint> = callbackFlow {
        val client = LocationServices.getFusedLocationProviderClient(context)
        
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(
                        TrackPoint(
                            lat = location.latitude,
                            lon = location.longitude,
                            timestampMs = location.time,
                        )
                    )
                }
            }
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateDistanceMeters(3f)
            .setMinUpdateIntervalMillis(2000L)
            .build()
        
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())

        awaitClose {
            client.removeLocationUpdates(callback)
        }
    }
}
