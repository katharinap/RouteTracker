package com.katharina.routetracker.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.katharina.routetracker.ui.theme.RouteTrackerTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val timestampFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

/**
 * Formats a timestamp for display.
 * Returns "—" if the timestamp is null.
 */
fun Long?.toDisplayTime(): String =
    this?.let { timestampFormatter.format(Date(it)) } ?: "—"

@Composable
fun SessionHeader(
    startedAt: Long?,
    stoppedAt: Long?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Started: ${startedAt.toDisplayTime()}",
            style = MaterialTheme.typography.bodyLarge
        )
        if (stoppedAt != null) {
            Text(
                text = "Stopped: ${stoppedAt.toDisplayTime()}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSessionHeader() {
    RouteTrackerTheme {
        SessionHeader(
            startedAt = System.currentTimeMillis() - 3600000,
            stoppedAt = System.currentTimeMillis()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSessionHeaderActive() {
    RouteTrackerTheme {
        SessionHeader(
            startedAt = System.currentTimeMillis(),
            stoppedAt = null
        )
    }
}
