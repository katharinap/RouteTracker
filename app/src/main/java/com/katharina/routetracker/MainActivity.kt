package com.katharina.routetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.katharina.routetracker.domain.TrackPoint
import com.katharina.routetracker.ui.OsmMapView
import com.katharina.routetracker.ui.theme.RouteTrackerTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // osmdroid configuration
        Configuration.getInstance().userAgentValue = packageName

        enableEdgeToEdge()
        setContent {
            RouteTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val dummyPoints = listOf(
                        TrackPoint(52.5200, 13.4050, 0), // Berlin
                        TrackPoint(52.5210, 13.4060, 0),
                        TrackPoint(52.5220, 13.4070, 0)
                    )
                    OsmMapView(
                        points = dummyPoints,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}
