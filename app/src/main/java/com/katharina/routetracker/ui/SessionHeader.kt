package com.katharina.routetracker.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
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
}

@Preview(showBackground = true)
@Composable
fun PreviewSessionHeader() {
    RouteTrackerTheme {
        SessionHeader(
            startedAt = System.currentTimeMillis() - 3600000,
            stoppedAt = System.currentTimeMillis(),
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSessionHeaderActive() {
    RouteTrackerTheme {
        SessionHeader(
            startedAt = System.currentTimeMillis(),
            stoppedAt = null,
            onBack = {}
        )
    }
}
