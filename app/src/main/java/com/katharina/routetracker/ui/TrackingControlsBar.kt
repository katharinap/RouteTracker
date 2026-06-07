package com.katharina.routetracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.katharina.routetracker.domain.ControlsState
import com.katharina.routetracker.domain.TrackingState
import com.katharina.routetracker.domain.toControlsState
import com.katharina.routetracker.ui.theme.RouteTrackerTheme

@Composable
fun TrackingControlsBar(
    controls: ControlsState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        if (controls.showStart) {
            Button(onClick = onStart) {
                Text("Start Tracking")
            }
        }

        if (controls.showPause) {
            OutlinedButton(onClick = onPause) {
                Text("Pause")
            }
        }

        if (controls.showResume) {
            Button(onClick = onResume) {
                Text("Resume")
            }
        }

        if (controls.showStop) {
            Button(
                onClick = onStop,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Stop")
            }
        }
    }
}

@Preview(showBackground = true, name = "Idle State")
@Composable
fun PreviewControlsIdle() {
    RouteTrackerTheme {
        TrackingControlsBar(
            controls = TrackingState.IDLE.toControlsState(),
            onStart = {}, onPause = {}, onResume = {}, onStop = {}
        )
    }
}

@Preview(showBackground = true, name = "Tracking State")
@Composable
fun PreviewControlsTracking() {
    RouteTrackerTheme {
        TrackingControlsBar(
            controls = TrackingState.TRACKING.toControlsState(),
            onStart = {}, onPause = {}, onResume = {}, onStop = {}
        )
    }
}

@Preview(showBackground = true, name = "Paused State")
@Composable
fun PreviewControlsPaused() {
    RouteTrackerTheme {
        TrackingControlsBar(
            controls = TrackingState.PAUSED.toControlsState(),
            onStart = {}, onPause = {}, onResume = {}, onStop = {}
        )
    }
}
