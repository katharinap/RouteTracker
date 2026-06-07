package com.katharina.routetracker.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.katharina.routetracker.domain.TrackPoint
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline

@Composable
fun OsmMapView(points: List<TrackPoint>, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    AndroidView(
        factory = {
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
            }
        },
        update = { map ->
            map.overlays.removeAll { it is Polyline }
            if (points.isNotEmpty()) {
                val line = Polyline().apply {
                    setPoints(points.map { GeoPoint(it.lat, it.lon) })
                }
                map.overlays.add(line)
                map.controller.setCenter(GeoPoint(points.last().lat, points.last().lon))
                map.controller.setZoom(16.0)
            }
            map.invalidate()
        },
        modifier = modifier,
    )
}
