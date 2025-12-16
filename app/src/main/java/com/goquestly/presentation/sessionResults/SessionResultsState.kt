package com.goquestly.presentation.sessionResults

import com.goquestly.domain.model.SessionResults

data class SessionResultsState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val results: SessionResults? = null
)
