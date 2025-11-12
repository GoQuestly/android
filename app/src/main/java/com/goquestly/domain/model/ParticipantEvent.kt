package com.goquestly.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
sealed class ParticipantEvent {
    abstract val participantId: Int
    abstract val userId: Int
    abstract val userName: String
    abstract val sessionId: Int

    data class Joined(
        override val participantId: Int,
        override val userId: Int,
        override val userName: String,
        override val sessionId: Int,
        val joinedAt: Instant
    ) : ParticipantEvent()

    data class Left(
        override val participantId: Int,
        override val userId: Int,
        override val userName: String,
        override val sessionId: Int,
        val leftAt: Instant
    ) : ParticipantEvent()
}
