package com.goquestly.domain.model

data class QuestPoint(
    val name: String,
    val orderNumber: Int,
    val isPassed: Boolean,
    val latitude: Double?,
    val longitude: Double?
)
