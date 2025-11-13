package com.goquestly.data.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.goquestly.R
import com.goquestly.data.local.ActiveSessionManager
import com.goquestly.data.remote.websocket.LocationSocketService
import com.goquestly.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationTrackingService : Service() {

    @Inject
    lateinit var locationSocketService: LocationSocketService

    @Inject
    lateinit var activeSessionManager: ActiveSessionManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var sessionId: Int? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> {
                sessionId = intent.getIntExtra(EXTRA_SESSION_ID, -1).takeIf { it != -1 }
                sessionId?.let { startTracking(it) }
            }

            ACTION_STOP_TRACKING -> {
                stopTracking()
            }
        }
        return START_NOT_STICKY
    }

    private fun startTracking(sessionId: Int) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.active_session_notification_title))
            .setContentText(getString(R.string.active_session_notification_message))
            .setSmallIcon(R.drawable.ic_logo)
            .setOngoing(true)
            .setContentIntent(createPendingIntent())
            .build()

        startForeground(NOTIFICATION_ID, notification)

        serviceScope.launch {
            try {
                locationSocketService.connect()
                locationSocketService.joinSession(sessionId)

                launch {
                    locationSocketService.observeParticipantRejected().collect {
                        handleRejection()
                    }
                }
            } catch (e: Exception) {
            }
        }

        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            stopSelf()
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            UPDATE_INTERVAL_MS
        ).apply {
            setMinUpdateIntervalMillis(FASTEST_UPDATE_INTERVAL_MS)
            setWaitForAccurateLocation(true)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val latLng = com.google.android.gms.maps.model.LatLng(
                        location.latitude,
                        location.longitude
                    )
                    val bearing = if (location.hasBearing()) location.bearing else 0f

                    serviceScope.launch {
                        activeSessionManager.updateLocation(latLng, bearing)
                    }

                    sessionId?.let {
                        serviceScope.launch {
                            try {
                                locationSocketService.updateLocation(
                                    sessionId = it,
                                    latitude = location.latitude,
                                    longitude = location.longitude
                                )
                            } catch (_: Exception) {

                            }
                        }
                    }
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            stopSelf()
        }
    }

    private fun handleRejection() {
        serviceScope.launch {
            activeSessionManager.emitRejection()
            stopTracking()
        }
    }

    private fun stopTracking() {
        sessionId?.let {
            serviceScope.launch {
                try {
                    locationSocketService.leaveSession(it)
                    locationSocketService.disconnect()
                } catch (_: Exception) {
                }
            }
        }

        fusedLocationClient.removeLocationUpdates(locationCallback)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.active_session_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.active_session_notification_channel_description)
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "location_tracking_channel"
        private const val NOTIFICATION_ID = 1001
        private const val UPDATE_INTERVAL_MS = 5000L
        private const val FASTEST_UPDATE_INTERVAL_MS = 2000L

        const val ACTION_START_TRACKING = "com.goquestly.action.START_TRACKING"
        const val ACTION_STOP_TRACKING = "com.goquestly.action.STOP_TRACKING"
        const val EXTRA_SESSION_ID = "session_id"

        var isRunning = false
            private set

        fun startService(context: Context, sessionId: Int) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_START_TRACKING
                putExtra(EXTRA_SESSION_ID, sessionId)
            }
            context.startForegroundService(intent)
            isRunning = true
        }

        fun stopService(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_STOP_TRACKING
            }
            context.startService(intent)
            isRunning = false
        }
    }
}
