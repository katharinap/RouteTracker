package com.katharina.routetracker.ui

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.katharina.routetracker.domain.TrackingState
import com.katharina.routetracker.domain.toControlsState

@Composable
fun RouteTrackerScreen(viewModel: SessionViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                viewModel.start()
            }
        }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            if (!uiState.hasActiveSession) {
                SessionList(
                    sessions = allSessions,
                    onCreateSession = viewModel::createSession,
                    onSelectSession = viewModel::selectSession,
                )
            } else {
                BackHandler {
                    viewModel.closeSession()
                }
                Column(modifier = Modifier.fillMaxSize()) {
                    SessionHeader(
                        startedAt = uiState.startedAt,
                        stoppedAt = uiState.stoppedAt,
                        distanceMeters = uiState.distanceMeters,
                        onBack = viewModel::closeSession,
                    )
                    OsmMapView(
                        points = uiState.points,
                        modifier = Modifier.weight(1f),
                        showLatestPointMarker = uiState.trackingState != TrackingState.STOPPED,
                    )
                    TrackingControlsBar(
                        controls = uiState.trackingState.toControlsState(),
                        onStart = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.POST_NOTIFICATIONS,
                                ),
                            )
                        },
                        onPause = viewModel::pause,
                        onResume = viewModel::resume,
                        onStop = viewModel::stop,
                    )
                }
            }
        }
    }
}
