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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.goquestly.R
import com.goquestly.data.local.ActiveSessionManager
import com.goquestly.data.remote.websocket.ActiveSessionSocketService
import com.goquestly.domain.mapper.toDomainModel
import com.goquestly.domain.model.ParticipationBlockReason
import com.goquestly.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationTrackingService : Service() {

    @Inject
    lateinit var activeSessionSocketService: ActiveSessionSocketService

    @Inject
    lateinit var activeSessionManager: ActiveSessionManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var sessionId: Int? = null
    private var lastKnownLocation: LatLng? = null
    private var periodicUpdateJob: Job? = null
    private var lastUpdateTimeMs: Long = 0

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
                activeSessionSocketService.connect()
                activeSessionSocketService.joinSession(sessionId)

                launch {
                    activeSessionSocketService.observeParticipantRejected().collect {
                        activeSessionManager.emitParticipationBlock(ParticipationBlockReason.TOO_FAR_FROM_START)
                        stopTracking()
                    }
                }

                launch {
                    activeSessionSocketService.observeParticipantDisqualified().collect {
                        activeSessionManager.emitParticipationBlock(ParticipationBlockReason.REQUIRED_TASK_NOT_COMPLETED)
                        stopTracking()
                    }
                }

                launch {
                    activeSessionSocketService.observePointPassed().collect { event ->
                        activeSessionManager.emitPointPassed(
                            questPointId = event.questPointId,
                            pointName = event.pointName,
                            orderNumber = event.orderNumber
                        )
                        showPointPassedNotification(event.questPointId, event.pointName)
                    }
                }

                launch {
                    activeSessionSocketService.observeSessionCancelled().collect {
                        activeSessionManager.emitSessionCancelled()
                        stopTracking()
                    }
                }

                launch {
                    activeSessionSocketService.observeSessionEnded().collect {
                        activeSessionManager.emitSessionEnded()
                        stopTracking()
                    }
                }

                launch {
                    activeSessionSocketService.observePhotoModerated().collect { event ->
                        activeSessionManager.emitPhotoModerated(event.toDomainModel())
                    }
                }
            } catch (_: Exception) {
            }
        }

        startLocationUpdates()
        startPeriodicLocationUpdates()
        sendInitialLocation()
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
            setWaitForAccurateLocation(false)
            setMaxUpdateDelayMillis(UPDATE_INTERVAL_MS * 2)
            setMinUpdateDistanceMeters(MIN_UPDATE_DISTANCE_METERS)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    if (location.hasAccuracy() && location.accuracy > MAX_LOCATION_ACCURACY_METERS) {
                        Log.w(TAG, "Skipping inaccurate location. Accuracy: ${location.accuracy}m")
                        return
                    }

                    val locationAge = System.currentTimeMillis() - location.time
                    if (locationAge > LOCATION_STALENESS_THRESHOLD_MS) {
                        Log.w(TAG, "Skipping stale location. Age: ${locationAge}ms")
                        return
                    }

                    val latLng = LatLng(
                        location.latitude,
                        location.longitude
                    )
                    val bearing = if (location.hasBearing()) location.bearing else 0f

                    lastKnownLocation = latLng
                    lastUpdateTimeMs = System.currentTimeMillis()

                    serviceScope.launch {
                        activeSessionManager.updateLocation(latLng, bearing)
                    }

                    sessionId?.let {
                        serviceScope.launch {
                            try {
                                activeSessionSocketService.updateLocation(
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
        } catch (_: SecurityException) {
            stopSelf()
        }
    }

    private fun startPeriodicLocationUpdates() {
        periodicUpdateJob?.cancel()
        periodicUpdateJob = serviceScope.launch {
            while (true) {
                delay(3000L)

                val timeSinceLastUpdate = System.currentTimeMillis() - lastUpdateTimeMs

                if (timeSinceLastUpdate >= PERIODIC_UPDATE_INTERVAL_MS) {
                    lastKnownLocation?.let { location ->
                        sessionId?.let { id ->
                            try {
                                activeSessionSocketService.updateLocation(
                                    sessionId = id,
                                    latitude = location.latitude,
                                    longitude = location.longitude
                                )
                                lastUpdateTimeMs = System.currentTimeMillis()
                                Log.d(
                                    TAG,
                                    "Periodic location update sent (no real update for ${timeSinceLastUpdate}ms)"
                                )
                            } catch (_: Exception) {
                                Log.w(TAG, "Failed to send periodic location update")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun sendInitialLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        serviceScope.launch {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        if (it.hasAccuracy() && it.accuracy <= MAX_LOCATION_ACCURACY_METERS) {
                            val latLng = LatLng(it.latitude, it.longitude)
                            val bearing = if (it.hasBearing()) it.bearing else 0f

                            lastKnownLocation = latLng
                            lastUpdateTimeMs = System.currentTimeMillis()

                            serviceScope.launch {
                                activeSessionManager.updateLocation(latLng, bearing)
                            }

                            sessionId?.let { id ->
                                serviceScope.launch {
                                    try {
                                        activeSessionSocketService.updateLocation(
                                            sessionId = id,
                                            latitude = it.latitude,
                                            longitude = it.longitude
                                        )
                                        Log.d(TAG, "Initial location sent successfully")
                                    } catch (e: Exception) {
                                        Log.w(TAG, "Failed to send initial location: ${e.message}")
                                    }
                                }
                            }
                        } else {
                            Log.w(TAG, "Initial location too inaccurate: ${it.accuracy}m")
                        }
                    } ?: run {
                        Log.w(TAG, "No initial location available")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to get initial location: ${e.message}")
            }
        }
    }

    private fun stopTracking() {
        periodicUpdateJob?.cancel()
        periodicUpdateJob = null
        lastKnownLocation = null
        lastUpdateTimeMs = 0

        sessionId?.let {
            serviceScope.launch {
                try {
                    activeSessionSocketService.leaveSession(it)
                    activeSessionSocketService.disconnect()
                } catch (_: Exception) {
                }
            }
        }

        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
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

        val pointPassedChannel = NotificationChannel(
            POINT_PASSED_CHANNEL_ID,
            getString(R.string.point_passed_notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.point_passed_notification_channel_description)
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        notificationManager.createNotificationChannel(pointPassedChannel)
    }

    private fun showPointPassedNotification(pointId: Int, pointName: String) {
        val notificationId = POINT_PASSED_NOTIFICATION_BASE_ID + pointId

        val notification = NotificationCompat.Builder(this, POINT_PASSED_CHANNEL_ID)
            .setContentTitle(getString(R.string.checkpoint_passed))
            .setContentText(getString(R.string.you_have_reached_checkpoint, pointName))
            .setSmallIcon(R.drawable.ic_logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(createPendingIntent())
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)

        activeSessionManager.addPointPassedNotification(notificationId)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "LocationTrackingService"
        private const val CHANNEL_ID = "location_tracking_channel"
        private const val POINT_PASSED_CHANNEL_ID = "point_passed_channel"
        private const val NOTIFICATION_ID = 1001
        private const val POINT_PASSED_NOTIFICATION_BASE_ID = 2000
        private const val UPDATE_INTERVAL_MS = 3000L
        private const val FASTEST_UPDATE_INTERVAL_MS = 1000L
        private const val LOCATION_STALENESS_THRESHOLD_MS = 30000L
        private const val MAX_LOCATION_ACCURACY_METERS = 100f
        private const val MIN_UPDATE_DISTANCE_METERS = 1.5f
        private const val PERIODIC_UPDATE_INTERVAL_MS = 8000L

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
