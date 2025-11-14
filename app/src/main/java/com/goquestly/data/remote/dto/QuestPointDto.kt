package com.goquestly.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class QuestPointDto(
    val pointName: String,
    val orderNumber: Int,
    val isPassed: Boolean,
    val pointLatitude: Double? = null,
    val pointLongitude: Double? = null
)
