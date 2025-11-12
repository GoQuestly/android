package com.goquestly.presentation.invite

data class InviteHandlerState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val sessionId: Int? = null,
    val isJoined: Boolean = false
)
