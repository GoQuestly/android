package com.goquestly.presentation.activeSession

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.goquestly.R
import com.goquestly.domain.model.QuestPoint
import com.goquestly.presentation.core.components.ConfirmationBottomSheet
import com.goquestly.presentation.core.components.button.PrimaryButton
import com.goquestly.util.DEFAULT_MAP_ZOOM_LEVEL
import com.goquestly.util.GOOGLE_MAPS_MAP_ID
import com.goquestly.util.MAP_ANIMATION_DURATION_MS
import com.goquestly.util.NAVIGATION_MAP_TILT_ANGLE
import com.goquestly.util.NAVIGATION_MAP_ZOOM_LEVEL
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ActiveSessionScreen(
    viewModel: ActiveSessionViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(state.isSessionCompleted) {
        if (state.isSessionCompleted) {
            onNavigateBack()
        }
    }

    if (!locationPermission.status.isGranted) {
        LocationPermissionRequired(
            onNavigateBack = onNavigateBack,
            onGrantPermission = {
                locationPermission.launchPermissionRequest()
            }
        )
    } else {
        LaunchedEffect(Unit) {
            viewModel.startLocationUpdates()
        }
        ActiveSessionContent(
            state = state,
            onRetry = viewModel::retry,
            onShowLeaveConfirmation = viewModel::showLeaveConfirmation,
            onDismissLeaveConfirmation = viewModel::dismissLeaveConfirmation,
            onLeaveSession = { viewModel.leaveSession(onNavigateBack) },
            onEnableCameraTracking = viewModel::enableCameraTracking,
            onDisableCameraTracking = viewModel::disableCameraTracking
        )
    }

    if (state.isUserRejected) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissRejectionDialog() },
            title = { Text(stringResource(R.string.participant_rejected_title)) },
            text = { Text(stringResource(R.string.participant_rejected_message)) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { viewModel.dismissRejectionDialog() }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationPermissionRequired(
    onNavigateBack: () -> Unit,
    onGrantPermission: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            modifier = Modifier.align(Alignment.TopCenter),
            title = {
                Text(
                    text = stringResource(R.string.location_permission_required),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.location_permission_required),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.location_permission_required_message),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))
            PrimaryButton(
                text = stringResource(R.string.grant_permission),
                onClick = onGrantPermission,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveSessionContent(
    state: ActiveSessionState,
    onRetry: () -> Unit,
    onShowLeaveConfirmation: () -> Unit,
    onDismissLeaveConfirmation: () -> Unit,
    onLeaveSession: () -> Unit,
    onEnableCameraTracking: () -> Unit,
    onDisableCameraTracking: () -> Unit
) {
    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = true
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState)
    val scope = rememberCoroutineScope()

    val initialLocation = remember(state.questPoints) {
        val currentCheckpoint = state.questPoints.firstOrNull { !it.isPassed }
        val targetCheckpoint = currentCheckpoint ?: state.questPoints.lastOrNull()
        targetCheckpoint?.let {
            if (it.latitude != null && it.longitude != null) {
                LatLng(it.latitude, it.longitude)
            } else null
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        initialLocation?.let {
            position = CameraPosition.fromLatLngZoom(it, DEFAULT_MAP_ZOOM_LEVEL)
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 145.dp,
        sheetContent = {
            if (state.session != null) {
                CheckpointsBottomSheet(
                    questPoints = state.questPoints,
                    elapsedTimeSeconds = state.elapsedTimeSeconds,
                    onMoveCamera = { latLng ->
                        onDisableCameraTracking()
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(
                                    latLng,
                                    DEFAULT_MAP_ZOOM_LEVEL
                                ),
                                MAP_ANIMATION_DURATION_MS
                            )
                        }
                    }
                )
            }
        },
        sheetContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.error_oops),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = state.error,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            PrimaryButton(
                                text = stringResource(R.string.try_again),
                                onClick = onRetry
                            )
                        }
                    }
                }

                else -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        MapView(
                            questPoints = state.questPoints,
                            cameraPositionState = cameraPositionState,
                            userLocation = state.userLocation,
                            userBearing = state.userBearing,
                            isCameraTrackingEnabled = state.isCameraTrackingEnabled,
                            onDisableCameraTracking = onDisableCameraTracking
                        )

                        CenterAlignedTopAppBar(
                            modifier = Modifier.align(Alignment.TopCenter),
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            ),
                            title = {
                                Text(
                                    text = state.session?.questTitle ?: "",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    softWrap = true
                                )
                            },
                            actions = {
                                IconButton(onClick = onShowLeaveConfirmation) {
                                    Icon(
                                        Icons.Filled.ExitToApp,
                                        contentDescription = stringResource(R.string.leave_active_session),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )

                        FloatingActionButton(
                            onClick = {
                                if (state.userLocation != null) {
                                    scope.launch {
                                        val cameraPosition = CameraPosition.Builder()
                                            .target(state.userLocation)
                                            .zoom(17f)
                                            .bearing(state.userBearing)
                                            .tilt(45f)
                                            .build()

                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newCameraPosition(cameraPosition),
                                            MAP_ANIMATION_DURATION_MS
                                        )
                                        onEnableCameraTracking()
                                    }
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(
                                    end = 16.dp,
                                    bottom = 161.dp
                                ),
                            shape = CircleShape,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = stringResource(R.string.my_location)
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.isLeaveConfirmationSheetOpen) {
        ConfirmationBottomSheet(
            title = stringResource(R.string.leave_active_session_confirmation_title),
            message = stringResource(R.string.leave_active_session_confirmation_message),
            confirmText = stringResource(R.string.leave),
            cancelText = stringResource(R.string.cancel),
            onConfirm = onLeaveSession,
            onDismiss = onDismissLeaveConfirmation
        )
    }
}

@OptIn(MapsComposeExperimentalApi::class)
@Composable
private fun MapView(
    questPoints: List<QuestPoint>,
    cameraPositionState: CameraPositionState,
    userLocation: LatLng?,
    userBearing: Float,
    isCameraTrackingEnabled: Boolean,
    onDisableCameraTracking: () -> Unit
) {
    var isFirstLocationUpdate by remember { mutableStateOf(true) }

    LaunchedEffect(userLocation, userBearing, isCameraTrackingEnabled) {
        if (userLocation != null && isCameraTrackingEnabled) {
            val cameraPosition = CameraPosition.Builder()
                .target(userLocation)
                .zoom(NAVIGATION_MAP_ZOOM_LEVEL)
                .bearing(userBearing)
                .tilt(NAVIGATION_MAP_TILT_ANGLE)
                .build()

            if (isFirstLocationUpdate) {
                cameraPositionState.move(CameraUpdateFactory.newCameraPosition(cameraPosition))
                isFirstLocationUpdate = false
            } else {
                cameraPositionState.animate(
                    CameraUpdateFactory.newCameraPosition(cameraPosition),
                    MAP_ANIMATION_DURATION_MS
                )
            }
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            mapType = MapType.NORMAL,
            isMyLocationEnabled = true,
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = false
        ),
        googleMapOptionsFactory = {
            GoogleMapOptions().mapId(GOOGLE_MAPS_MAP_ID)
        },
        mapColorScheme = ComposeMapColorScheme.FOLLOW_SYSTEM
    ) {
        MapEffect(Unit) { map ->
            map.setOnCameraMoveStartedListener { reason ->
                if (reason == com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    onDisableCameraTracking()
                }
            }
        }
        questPoints.forEach { point ->
            if (point.latitude != null && point.longitude != null) {
                Marker(
                    state = MarkerState(position = LatLng(point.latitude, point.longitude)),
                    title = point.name,
                    snippet = if (point.isPassed) stringResource(R.string.completed).lowercase() else null,
                    icon = BitmapDescriptorFactory.defaultMarker(
                        if (point.isPassed) BitmapDescriptorFactory.HUE_GREEN
                        else BitmapDescriptorFactory.HUE_ORANGE
                    )
                )
            }
        }
    }
}

@Composable
private fun CheckpointsBottomSheet(
    questPoints: List<QuestPoint>,
    elapsedTimeSeconds: Long,
    onMoveCamera: (LatLng) -> Unit
) {
    val completedCount = questPoints.count { it.isPassed }
    val totalCount = questPoints.size
    val progress = if (totalCount > 0) (completedCount.toFloat() / totalCount) else 0f
    val progressPercent = (progress * 100).toInt()

    val currentCheckpoint = questPoints.firstOrNull { !it.isPassed }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.percent_completed, progressPercent),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = stringResource(R.string.checkpoints_progress, completedCount, totalCount),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        val indicatorColor = if (progress > 0f) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = indicatorColor,
            trackColor = MaterialTheme.colorScheme.surfaceContainer,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formatTime(elapsedTimeSeconds),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.checkpoints),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.height(400.dp)
        ) {
            items(questPoints) { point ->
                val isCurrent = point == currentCheckpoint
                Column {
                    CheckpointItem(
                        point = point,
                        isCurrent = isCurrent,
                        onClick = {
                            if (point.latitude != null && point.longitude != null) {
                                onMoveCamera(LatLng(point.latitude, point.longitude))
                            }
                        }
                    )
                    if (isCurrent) {
                        Spacer(modifier = Modifier.height(12.dp))
                        PrimaryButton(
                            text = stringResource(R.string.open_task),
                            onClick = { },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckpointItem(
    point: QuestPoint,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        when {
                            point.isPassed -> MaterialTheme.colorScheme.primary
                            isCurrent -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        point.isPassed -> Icons.Default.Check
                        isCurrent -> Icons.Default.LockOpen
                        else -> Icons.Default.Lock
                    },
                    contentDescription = null,
                    tint = if (point.isPassed) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = point.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        point.isPassed -> stringResource(R.string.completed)
                        isCurrent -> stringResource(R.string.current_checkpoint)
                        else -> stringResource(R.string.locked_until_previous_task_completed)
                    },
                    fontSize = 14.sp,
                    color = if (point.isPassed) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

private fun formatTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%d:%02d", minutes, secs)
    }
}