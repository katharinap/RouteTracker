package com.katharina.routetracker.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.katharina.routetracker.MainActivity
import com.katharina.routetracker.R
import com.katharina.routetracker.domain.TrackingState
import com.katharina.routetracker.repository.TrackingRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class TrackingService : Service() {

    @Inject
    lateinit var repo: TrackingRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val notificationId = 1
    private val channelId = "tracking_channel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        
        repo.session
            .onEach { session ->
                if (session == null || (session.state == TrackingState.STOPPED)) {
                    stopSelf()
                } else {
                    updateNotification(session.state)
                }
            }
            .launchIn(serviceScope)
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification(TrackingState.TRACKING)
        
        startForeground(
            notificationId, 
            notification, 
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION,
        )
        
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            "Route Tracking",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(state: TrackingState): android.app.Notification {
        val contentText = if (state == TrackingState.TRACKING) "Tracking your route..." else "Tracking paused"
        
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Route Tracker")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_tracking)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(state: TrackingState) {
        val notification = createNotification(state)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }
}
