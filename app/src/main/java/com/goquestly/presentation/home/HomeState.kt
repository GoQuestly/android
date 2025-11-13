package com.goquestly.presentation.home

import com.goquestly.domain.model.QuestSessionSummary
import com.goquestly.presentation.core.pagination.PaginationState

data class HomeState(
    val pagination: PaginationState<QuestSessionSummary> = PaginationState(),
    val isInitialLoading: Boolean = false,
    val error: String? = null
)
