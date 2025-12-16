package com.goquestly.presentation.statistics

import com.goquestly.domain.model.SessionStatistics

data class StatisticsState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val statistics: SessionStatistics? = null
)