package com.katharina.routetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import com.katharina.routetracker.ui.RouteTrackerScreen
import com.katharina.routetracker.ui.SessionViewModel
import com.katharina.routetracker.ui.theme.RouteTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // osmdroid configuration
        Configuration.getInstance().userAgentValue = packageName

        enableEdgeToEdge()
        setContent {
            RouteTrackerTheme {
                val viewModel: SessionViewModel = hiltViewModel()
                RouteTrackerScreen(viewModel = viewModel)
            }
        }
    }
}
