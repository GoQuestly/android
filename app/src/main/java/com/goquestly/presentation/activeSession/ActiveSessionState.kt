package com.goquestly.presentation.activeSession

import com.google.android.gms.maps.model.LatLng
import com.goquestly.domain.model.ParticipationBlockReason
import com.goquestly.domain.model.QuestPoint
import com.goquestly.domain.model.QuestSession
import com.goquestly.domain.model.TaskStartData
import com.goquestly.domain.model.TaskStatus

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
    val isParticipationBlocked: Boolean = false,
    val blockReason: ParticipationBlockReason? = null,
    val isCameraTrackingEnabled: Boolean = true,
    val pointPassedEvent: PointPassedEvent? = null,
    val activeTask: TaskStartData? = null
)

data class PointPassedEvent(
    val questPointId: Int,
    val pointName: String,
    val orderNumber: Int,
    val hasTask: Boolean = false,
    val taskStatus: TaskStatus? = null
)
