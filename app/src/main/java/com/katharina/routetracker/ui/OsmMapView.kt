package com.katharina.routetracker.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.katharina.routetracker.R
import com.katharina.routetracker.domain.TrackPoint
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun OsmMapView(
    points: List<TrackPoint>,
    modifier: Modifier = Modifier,
    showLatestPointMarker: Boolean = false,
) {
    val context = LocalContext.current
    var isInitialized by remember { mutableStateOf(false) }

    AndroidView(
        factory = {
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
            }
        },
        update = { map ->
            map.overlays.removeAll { it is Polyline || it is Marker }
            if (points.isNotEmpty()) {
                val lastPoint = GeoPoint(points.last().lat, points.last().lon)
                
                val line = Polyline().apply {
                    setPoints(points.map { GeoPoint(it.lat, it.lon) })
                }
                map.overlays.add(line)

                if (showLatestPointMarker) {
                    val marker = Marker(map).apply {
                        position = lastPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        icon = ContextCompat.getDrawable(context, R.drawable.ic_location_dot)
                        title = "Current Position"
                    }
                    map.overlays.add(marker)
                }
                
                // Only set the initial zoom and center once per session view
                if (!isInitialized) {
                    map.controller.setZoom(16.0)
                    map.controller.setCenter(lastPoint)
                    isInitialized = true
                } else {
                    // For subsequent updates, we follow the user but keep their zoom level
                    map.controller.setCenter(lastPoint)
                }
            }
            map.invalidate()
        },
        modifier = modifier,
    )
}
