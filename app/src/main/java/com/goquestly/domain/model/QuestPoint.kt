package com.goquestly.domain.model

data class QuestPoint(
    val pointId: Int,
    val name: String,
    val orderNumber: Int,
    val isPassed: Boolean,
    val latitude: Double?,
    val longitude: Double?,
    val hasTask: Boolean = false,
    val isTaskSuccessCompletionRequiredForNextPoint: Boolean = false,
    val taskStatus: TaskStatus? = null
)
