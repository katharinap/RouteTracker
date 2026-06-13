package com.katharina.routetracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.katharina.routetracker.domain.TrackingSession
import java.util.Locale

fun Double.toDisplayDistance(): String =
    if (this >= 1000) {
        String.format(Locale.getDefault(), "%.2f km", this / 1000.0)
    } else {
        String.format(Locale.getDefault(), "%.0f m", this)
    }

@Composable
fun SessionList(
    sessions: List<TrackingSession>,
    onCreateSession: () -> Unit,
    onSelectSession: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to Route Tracker",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Button(onClick = onCreateSession) {
                    Text("New Session")
                }
            }
        }

        Text(
            text = "Past Sessions",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(sessions) { session ->
                SessionItem(
                    session = session,
                    onClick = { onSelectSession(session.id) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
fun SessionItem(
    session: TrackingSession,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Session #${session.id}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Started: ${session.startedAt.toDisplayTime()}",
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${session.points.size} points recorded",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = session.distanceMeters.toDisplayDistance(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
