package com.goquestly.presentation.activeSession

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
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
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.BitmapDescriptor
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
import com.goquestly.domain.model.ParticipationBlockReason
import com.goquestly.domain.model.QuestPoint
import com.goquestly.domain.model.TaskStatus
import com.goquestly.presentation.core.components.ConfirmationBottomSheet
import com.goquestly.presentation.core.components.button.PrimaryButton
import com.goquestly.util.DEFAULT_MAP_ZOOM_LEVEL
import com.goquestly.util.GOOGLE_MAPS_MAP_ID
import com.goquestly.util.MAP_ANIMATION_DURATION_MS
import com.goquestly.util.NAVIGATION_MAP_TILT_ANGLE
import com.goquestly.util.NAVIGATION_MAP_ZOOM_LEVEL
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ActiveSessionScreen(
    viewModel: ActiveSessionViewModel = hiltViewModel(),
    onLeaveSession: () -> Unit,
    onNavigateToTask: ((sessionId: Int, pointId: Int, pointName: String) -> Unit)? = null
) {
    val state by viewModel.state.collectAsState()
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    var isLocationEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(state.isSessionCompleted) {
        if (state.isSessionCompleted) {
            onLeaveSession()
        }
    }

    LaunchedEffect(state.activeTask) {
        val activeTask = state.activeTask
        if (activeTask != null && !state.isLoading) {
            val pointName =
                state.questPoints.find { it.pointId == activeTask.questPointId }?.name ?: ""
            onNavigateToTask?.invoke(
                state.session?.id ?: return@LaunchedEffect,
                activeTask.questPointId,
                pointName
            )
        }
    }

    LaunchedEffect(Unit) {
        isLocationEnabled = viewModel.isLocationEnabled()
    }

    if (!locationPermission.status.isGranted) {
        LocationPermissionRequired(
            onNavigateBack = onLeaveSession,
            onGrantPermission = {
                locationPermission.launchPermissionRequest()
            }
        )
    } else if (!isLocationEnabled) {
        LocationDisabledScreen(
            onNavigateBack = onLeaveSession,
            onRetry = {
                isLocationEnabled = viewModel.isLocationEnabled()
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
            onLeaveSession = { viewModel.leaveSession(onLeaveSession) },
            onEnableCameraTracking = viewModel::enableCameraTracking,
            onDisableCameraTracking = viewModel::disableCameraTracking,
            onNavigateToTask = onNavigateToTask
        )
    }

    if (state.isParticipationBlocked) {
        val blockMessage = when (state.blockReason) {
            ParticipationBlockReason.NO_LOCATION -> stringResource(R.string.rejection_reason_no_location)
            ParticipationBlockReason.TOO_FAR_FROM_START -> stringResource(R.string.rejection_reason_too_far_from_start)
            ParticipationBlockReason.REQUIRED_TASK_NOT_COMPLETED -> stringResource(R.string.rejection_reason_required_task_not_completed)
            null -> stringResource(R.string.participant_rejected_message)
        }

        AlertDialog(
            onDismissRequest = { viewModel.dismissBlockDialog() },
            title = { Text(stringResource(R.string.participant_rejected_title)) },
            text = { Text(blockMessage) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { viewModel.dismissBlockDialog() }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    state.pointPassedEvent?.let { event ->
        PointPassedDialog(
            pointName = event.pointName,
            hasTask = event.hasTask,
            taskStatus = event.taskStatus,
            onOpenTask = {
                val sessionId = state.session?.id ?: 0
                onNavigateToTask?.invoke(sessionId, event.questPointId, event.pointName)
                viewModel.dismissPointPassedDialog()
            },
            onDismiss = { viewModel.dismissPointPassedDialog() }
        )
    }

    state.photoModeratedEvent?.let { event ->
        PhotoModeratedDialog(
            event = event,
            onDismiss = { viewModel.dismissPhotoModeratedDialog() }
        )
    }
}

@Composable
private fun PointPassedDialog(
    pointName: String,
    hasTask: Boolean,
    taskStatus: TaskStatus?,
    onOpenTask: () -> Unit,
    onDismiss: () -> Unit
) {
    val shouldShowTaskButton =
        hasTask && taskStatus in setOf(TaskStatus.NOT_STARTED, TaskStatus.IN_PROGRESS)
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = stringResource(R.string.checkpoint_passed),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.you_have_reached_checkpoint, pointName),
                    textAlign = TextAlign.Center
                )
                if (shouldShowTaskButton) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.task_available_at_this_checkpoint),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            if (shouldShowTaskButton) {
                PrimaryButton(
                    text = stringResource(R.string.open_task),
                    onClick = onOpenTask
                )
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(stringResource(if (shouldShowTaskButton) R.string.continue_quest else R.string.ok))
            }
        }
    )
}

@Composable
private fun PhotoModeratedDialog(
    event: com.goquestly.domain.model.PhotoModeratedEvent,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = if (event.approved) Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = null,
                tint = if (event.approved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = stringResource(
                    if (event.approved) R.string.photo_approved else R.string.photo_rejected
                ),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = event.pointName,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.taskDescription,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!event.approved && event.rejectionReason != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.rejection_reason_label),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Text(
                        text = event.rejectionReason,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (event.scoreAdjustment != 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.points_earned, event.scoreAdjustment),
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            PrimaryButton(
                text = stringResource(R.string.ok),
                onClick = onDismiss
            )
        }
    )
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
private fun LocationDisabledScreen(
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            modifier = Modifier.align(Alignment.TopCenter),
            title = {
                Text(
                    text = stringResource(R.string.location_disabled),
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
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.location_disabled),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.location_disabled_message),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))
            PrimaryButton(
                text = stringResource(R.string.try_again),
                onClick = onRetry,
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
    onDisableCameraTracking: () -> Unit,
    onNavigateToTask: ((sessionId: Int, pointId: Int, pointName: String) -> Unit)?
) {
    BackHandler(enabled = true) {
        onShowLeaveConfirmation()
    }

    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = true
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState)
    val scope = rememberCoroutineScope()

    val defaultBottomSheetPeekHeight = 150.dp

    val density = LocalDensity.current
    var bottomSheetPeekHeightPx by remember {
        mutableFloatStateOf(with(density) { defaultBottomSheetPeekHeight.toPx() })
    }
    var bottomSheetFullHeightPx by remember {
        mutableFloatStateOf(with(density) { 400.dp.toPx() })
    }

    val bottomSheetPeekHeight = with(density) {
        if (bottomSheetPeekHeightPx > 0) bottomSheetPeekHeightPx.toDp() else defaultBottomSheetPeekHeight
    }

    val mapPaddingPx = when (bottomSheetState.currentValue) {
        SheetValue.PartiallyExpanded -> bottomSheetPeekHeightPx
        SheetValue.Expanded -> bottomSheetFullHeightPx
        else -> bottomSheetPeekHeightPx
    }

    val initialCameraPosition = remember(state.questPoints, state.userLocation) {
        if (state.userLocation != null) {
            CameraPosition(
                state.userLocation,
                NAVIGATION_MAP_ZOOM_LEVEL,
                NAVIGATION_MAP_TILT_ANGLE,
                state.userBearing
            )
        } else if (state.questPoints.isNotEmpty()) {
            val currentCheckpoint = state.questPoints.firstOrNull { !it.isPassed }
            val targetCheckpoint = currentCheckpoint ?: state.questPoints.lastOrNull()

            if (targetCheckpoint?.latitude != null && targetCheckpoint.longitude != null) {
                CameraPosition(
                    LatLng(targetCheckpoint.latitude, targetCheckpoint.longitude),
                    DEFAULT_MAP_ZOOM_LEVEL,
                    0f,
                    0f
                )
            } else {
                CameraPosition(LatLng(0.0, 0.0), DEFAULT_MAP_ZOOM_LEVEL, 0f, 0f)
            }
        } else {
            CameraPosition(LatLng(0.0, 0.0), DEFAULT_MAP_ZOOM_LEVEL, 0f, 0f)
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = initialCameraPosition
    }

    var targetCameraLocation by remember { mutableStateOf<LatLng?>(null) }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = bottomSheetPeekHeight,
        sheetContent = {
            if (state.session != null) {
                CheckpointsBottomSheet(
                    questPoints = state.questPoints,
                    elapsedTimeSeconds = state.elapsedTimeSeconds,
                    sessionId = state.session.id,
                    userLocation = state.userLocation,
                    onMoveCamera = { latLng ->
                        onDisableCameraTracking()
                        targetCameraLocation = latLng
                    },
                    onNavigateToTask = onNavigateToTask,
                    onPeekHeightMeasured = { heightPx ->
                        val contentHeightWithPadding =
                            heightPx + with(density) { (24.dp + 16.dp).toPx() }
                        val minHeightPx = with(density) { defaultBottomSheetPeekHeight.toPx() }
                        val targetHeight = maxOf(contentHeightWithPadding, minHeightPx)

                        if (abs(targetHeight - bottomSheetPeekHeightPx) > 1f) {
                            bottomSheetPeekHeightPx = targetHeight
                        }
                    },
                    onFullHeightMeasured = { heightPx ->
                        val totalHeight = heightPx + with(density) { 24.dp.toPx() }
                        if (abs(totalHeight - bottomSheetFullHeightPx) > 1f) {
                            bottomSheetFullHeightPx = totalHeight
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
                            onDisableCameraTracking = onDisableCameraTracking,
                            bottomSheetPeekHeightPx = mapPaddingPx,
                            targetLocation = targetCameraLocation,
                            onCameraAnimationComplete = {
                                targetCameraLocation = null
                            }
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
                                        Icons.AutoMirrored.Filled.ExitToApp,
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
    onDisableCameraTracking: () -> Unit,
    bottomSheetPeekHeightPx: Float,
    targetLocation: LatLng? = null,
    onCameraAnimationComplete: () -> Unit = {}
) {
    var isFirstLocationUpdate by remember { mutableStateOf(true) }
    var isMapReady by remember { mutableStateOf(false) }
    var shouldRenderMap by remember { mutableStateOf(false) }

    var greenMarker by remember { mutableStateOf<BitmapDescriptor?>(null) }
    var orangeMarker by remember { mutableStateOf<BitmapDescriptor?>(null) }

    var mapInstance by remember { mutableStateOf<GoogleMap?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(userLocation, questPoints) {
        if (shouldRenderMap) return@LaunchedEffect

        val hasValidLocation = userLocation != null
        val hasValidQuestPoints = questPoints.any { it.latitude != null && it.longitude != null }

        if (hasValidLocation || hasValidQuestPoints) {
            delay(100)
            shouldRenderMap = true
        }
    }

    LaunchedEffect(userLocation, userBearing, isCameraTrackingEnabled, isMapReady) {
        if (isMapReady && userLocation != null && isCameraTrackingEnabled) {
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

    LaunchedEffect(targetLocation, mapInstance, isMapReady) {
        val map = mapInstance
        if (targetLocation != null && map != null && isMapReady) {
            delay(50)

            scope.launch {
                try {
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(targetLocation, DEFAULT_MAP_ZOOM_LEVEL),
                        MAP_ANIMATION_DURATION_MS,
                        object : GoogleMap.CancelableCallback {
                            override fun onFinish() {
                                onCameraAnimationComplete()
                            }

                            override fun onCancel() {
                                onCameraAnimationComplete()
                            }
                        }
                    )
                } catch (_: Exception) {
                    onCameraAnimationComplete()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (shouldRenderMap) {
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
                    mapInstance = map

                    if (greenMarker == null) {
                        greenMarker =
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    }
                    if (orangeMarker == null) {
                        orangeMarker =
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                    }

                    map.setOnCameraMoveStartedListener { reason ->
                        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                            onDisableCameraTracking()
                        }
                    }
                    map.setOnMapLoadedCallback {
                        isMapReady = true
                    }
                }

                MapEffect(bottomSheetPeekHeightPx) { map ->
                    map.setPadding(0, 0, 0, bottomSheetPeekHeightPx.toInt())
                }
                if (greenMarker != null && orangeMarker != null) {
                    questPoints.forEach { point ->
                        if (point.latitude != null && point.longitude != null) {
                            Marker(
                                state = MarkerState(
                                    position = LatLng(
                                        point.latitude,
                                        point.longitude
                                    )
                                ),
                                title = point.name,
                                snippet = if (point.isPassed) stringResource(R.string.completed).lowercase() else null,
                                icon = if (point.isPassed) greenMarker else orangeMarker
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = !isMapReady,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun CheckpointsBottomSheet(
    questPoints: List<QuestPoint>,
    elapsedTimeSeconds: Long,
    sessionId: Int,
    userLocation: LatLng?,
    onMoveCamera: (LatLng) -> Unit,
    onNavigateToTask: ((sessionId: Int, pointId: Int, pointName: String) -> Unit)?,
    onPeekHeightMeasured: (Float) -> Unit,
    onFullHeightMeasured: (Float) -> Unit
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
            .onGloballyPositioned { layoutCoordinates ->
                onFullHeightMeasured(layoutCoordinates.size.height.toFloat())
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { layoutCoordinates ->
                    onPeekHeightMeasured(layoutCoordinates.size.height.toFloat())
                }
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
                    text = stringResource(
                        R.string.checkpoints_progress,
                        completedCount,
                        totalCount
                    ),
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
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.checkpoints),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (currentCheckpoint != null) {
                Text(
                    text = stringResource(R.string.navigate_to_next),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.height(400.dp)
        ) {
            items(questPoints) { point ->
                Column {
                    val distance = if (userLocation != null &&
                        point.latitude != null &&
                        point.longitude != null
                    ) {
                        calculateDistance(
                            userLocation,
                            LatLng(point.latitude, point.longitude)
                        )
                    } else null

                    val previousPoint = questPoints.getOrNull(questPoints.indexOf(point) - 1)
                    val isPreviousTaskBlocking = previousPoint?.let {
                        it.isPassed &&
                                it.hasTask &&
                                it.taskStatus in setOf(
                            TaskStatus.NOT_STARTED,
                            TaskStatus.IN_PROGRESS
                        )
                    } ?: false

                    val distanceText = distance?.let {
                        if (it < 1000) {
                            "${it.toInt()} ${stringResource(R.string.unit_meters)}"
                        } else {
                            val km = it / 1000
                            String.format(
                                Locale.US,
                                "%.1f",
                                km
                            ) + " ${stringResource(R.string.unit_kilometers)}"
                        }
                    }

                    CheckpointItem(
                        point = point,
                        isNext = point == currentCheckpoint,
                        distanceText = distanceText,
                        isPreviousTaskBlocking = isPreviousTaskBlocking,
                        onClick = {
                            if (point.latitude != null && point.longitude != null) {
                                onMoveCamera(LatLng(point.latitude, point.longitude))
                            }
                        }
                    )
                    val shouldShowTaskButton = point.isPassed
                            && point.hasTask
                            && (point.taskStatus == TaskStatus.NOT_STARTED || point.taskStatus == TaskStatus.IN_PROGRESS)

                    if (shouldShowTaskButton) {
                        Spacer(modifier = Modifier.height(12.dp))
                        PrimaryButton(
                            text = stringResource(R.string.open_task),
                            onClick = {
                                onNavigateToTask?.invoke(sessionId, point.pointId, point.name)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskStatusBadge(taskStatus: TaskStatus?) {
    if (taskStatus == TaskStatus.NOT_STARTED || taskStatus == null) {
        return
    }

    val backgroundColor = when (taskStatus) {
        TaskStatus.COMPLETED_SUCCESS -> MaterialTheme.colorScheme.primaryContainer
        TaskStatus.COMPLETED_FAILED -> MaterialTheme.colorScheme.errorContainer
        TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiaryContainer
        TaskStatus.IN_REVIEW -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when (taskStatus) {
        TaskStatus.COMPLETED_SUCCESS -> MaterialTheme.colorScheme.onPrimaryContainer
        TaskStatus.COMPLETED_FAILED -> MaterialTheme.colorScheme.onErrorContainer
        TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.onTertiaryContainer
        TaskStatus.IN_REVIEW -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val icon = when (taskStatus) {
        TaskStatus.COMPLETED_SUCCESS -> Icons.Default.CheckCircle
        TaskStatus.COMPLETED_FAILED -> Icons.Default.Close
        TaskStatus.IN_PROGRESS -> Icons.AutoMirrored.Outlined.Assignment
        TaskStatus.IN_REVIEW -> Icons.Default.AccessTime
        else -> Icons.Default.AccessTime
    }

    val text = when (taskStatus) {
        TaskStatus.COMPLETED_SUCCESS -> stringResource(R.string.task_completed)
        TaskStatus.COMPLETED_FAILED -> stringResource(R.string.task_failed)
        TaskStatus.IN_PROGRESS -> stringResource(R.string.task_in_progress)
        TaskStatus.IN_REVIEW -> stringResource(R.string.task_in_review)
        else -> stringResource(R.string.task_expired)
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = contentColor
        )
    }
}

@Composable
private fun CheckpointItem(
    point: QuestPoint,
    isNext: Boolean,
    distanceText: String? = null,
    isPreviousTaskBlocking: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isNext && !point.isPassed && !isPreviousTaskBlocking) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
        ),
        onClick = onClick,
        border = if (isNext && !point.isPassed && !isPreviousTaskBlocking) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        } else null
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
                            isNext -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        point.isPassed -> Icons.Default.Check
                        isNext -> Icons.Default.LockOpen
                        else -> Icons.Default.Lock
                    },
                    contentDescription = null,
                    tint = if (point.isPassed) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = point.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (point.hasTask) {
                        Spacer(modifier = Modifier.height(4.dp))
                        TaskStatusBadge(
                            taskStatus = point.taskStatus,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = when {
                            point.isPassed -> stringResource(R.string.passed)
                            isNext && !isPreviousTaskBlocking -> stringResource(R.string.go_to_this_checkpoint)
                            isNext && isPreviousTaskBlocking -> stringResource(R.string.complete_previous_task_first)
                            else -> stringResource(R.string.locked_until_previous_task_completed)
                        },
                        fontSize = 14.sp,
                        fontWeight = if (isNext && !point.isPassed && !isPreviousTaskBlocking) FontWeight.SemiBold else FontWeight.Normal,
                        color = when {
                            point.isPassed -> MaterialTheme.colorScheme.primary
                            isNext && !isPreviousTaskBlocking -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                        }
                    )

                    if (distanceText != null && !point.isPassed && !isPreviousTaskBlocking) {
                        Text(
                            text = "• $distanceText",
                            fontSize = 14.sp,
                            fontWeight = if (isNext) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, secs)
    }
}

private fun calculateDistance(from: LatLng, to: LatLng): Float {
    val results = FloatArray(1)
    android.location.Location.distanceBetween(
        from.latitude,
        from.longitude,
        to.latitude,
        to.longitude,
        results
    )
    return results[0]
}