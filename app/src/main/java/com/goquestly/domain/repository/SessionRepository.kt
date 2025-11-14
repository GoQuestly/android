package com.goquestly.domain.repository

import com.goquestly.domain.model.PaginatedResponse
import com.goquestly.domain.model.QuestPoint
import com.goquestly.domain.model.QuestSession
import com.goquestly.domain.model.QuestSessionSummary

interface SessionRepository {
    suspend fun joinSession(inviteToken: String): Result<QuestSession>
    suspend fun getJoinedSessions(
        limit: Int,
        offset: Int
    ): Result<PaginatedResponse<QuestSessionSummary>>

    suspend fun getSessionDetails(sessionId: Int): Result<QuestSession>
    suspend fun leaveSession(sessionId: Int): Result<Unit>
    suspend fun getQuestPoints(sessionId: Int): Result<List<QuestPoint>>
}
