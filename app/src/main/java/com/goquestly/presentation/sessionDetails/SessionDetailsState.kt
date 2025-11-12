package com.goquestly.presentation.sessionDetails

import com.goquestly.domain.model.Participant
import com.goquestly.domain.model.QuestSession

data class SessionDetailsState(
    val session: QuestSession? = null,
    val participants: List<Participant> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isParticipantsSheetOpen: Boolean = false,
    val isLeaveConfirmationSheetOpen: Boolean = false
)
