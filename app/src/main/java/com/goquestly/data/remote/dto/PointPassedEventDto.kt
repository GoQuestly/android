package com.goquestly.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PointPassedEventDto(
    val pointPassed: Boolean,
    val pointName: String,
    val orderNumber: Int,
    val questPointId: Int
)
