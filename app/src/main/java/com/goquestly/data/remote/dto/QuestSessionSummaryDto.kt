package com.goquestly.data.remote.dto

import com.goquestly.data.remote.serializer.InstantIsoSerializer
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Serializable
data class QuestSessionSummaryDto(
    val questSessionId: Int,
    val questId: Int,
    val questTitle: String,
    @Serializable(with = InstantIsoSerializer::class)
    val startDate: Instant,
    @Serializable(with = InstantIsoSerializer::class)
    val endDate: Instant?,
    val endReason: String? = null,
    val isActive: Boolean,
    val participantCount: Int,
    val questPointCount: Int,
    val passedQuestPointCount: Int
)
