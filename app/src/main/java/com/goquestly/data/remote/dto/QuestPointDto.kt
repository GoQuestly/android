package com.goquestly.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class QuestPointDto(
    val pointId: Int,
    val pointName: String,
    val orderNumber: Int,
    val isPassed: Boolean,
    val pointLatitude: Double? = null,
    val pointLongitude: Double? = null,
    val hasTask: Boolean = false,
    val isTaskSuccessCompletionRequiredForNextPoint: Boolean = false,
    val taskStatus: String? = null
)
