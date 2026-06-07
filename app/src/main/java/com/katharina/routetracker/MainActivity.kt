package com.katharina.routetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.katharina.routetracker.data.FusedLocationSource
import com.katharina.routetracker.data.room.AppDatabase
import com.katharina.routetracker.data.room.RoomSessionStore
import com.katharina.routetracker.data.room.TrackPointConverter
import com.katharina.routetracker.repository.TrackingRepository
import com.katharina.routetracker.ui.RouteTrackerScreen
import com.katharina.routetracker.ui.SessionViewModel
import com.katharina.routetracker.ui.theme.RouteTrackerTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // osmdroid configuration
        Configuration.getInstance().userAgentValue = packageName

        // Manual DI for now
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "route_tracker.db"
        ).build()
        
        val store = RoomSessionStore(db.sessionDao(), TrackPointConverter())
        val locationSource = FusedLocationSource(applicationContext)
        val repo = TrackingRepository(store, locationSource, lifecycleScope)
        val viewModel = SessionViewModel(repo)

        enableEdgeToEdge()
        setContent {
            RouteTrackerTheme {
                RouteTrackerScreen(viewModel = viewModel)
            }
        }
    }
}
