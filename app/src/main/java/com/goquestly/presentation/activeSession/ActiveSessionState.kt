package com.goquestly.presentation.activeSession

import com.google.android.gms.maps.model.LatLng
import com.goquestly.domain.model.QuestPoint
import com.goquestly.domain.model.QuestSession

data class ActiveSessionState(
    val session: QuestSession? = null,
    val questPoints: List<QuestPoint> = emptyList(),
    val userLocation: LatLng? = null,
    val userBearing: Float = 0f,
    val isLoading: Boolean = true,
    val error: String? = null,
    val elapsedTimeSeconds: Long = 0,
    val isLeaveConfirmationSheetOpen: Boolean = false,
    val isSessionCompleted: Boolean = false,
    val isUserRejected: Boolean = false,
    val isCameraTrackingEnabled: Boolean = true
)
