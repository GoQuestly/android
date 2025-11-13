package com.goquestly.data.repository

import com.goquestly.data.remote.ApiService
import com.goquestly.data.remote.dto.JoinSessionRequestDto
import com.goquestly.domain.mapper.toDomainModel
import com.goquestly.domain.model.PaginatedResponse
import com.goquestly.domain.model.QuestPoint
import com.goquestly.domain.model.QuestSession
import com.goquestly.domain.model.QuestSessionSummary
import com.goquestly.domain.repository.SessionRepository
import com.goquestly.util.runCatchingAppException
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : SessionRepository {

    override suspend fun joinSession(inviteToken: String): Result<QuestSession> =
        runCatchingAppException {
            val request = JoinSessionRequestDto(inviteToken)
            apiService.joinSession(request).toDomainModel()
        }

    override suspend fun getJoinedSessions(
        limit: Int,
        offset: Int
    ): Result<PaginatedResponse<QuestSessionSummary>> = runCatchingAppException {
        val response = apiService.getJoinedSessions(limit, offset)
        response.toDomainModel { it.toDomainModel() }
    }

    override suspend fun getSessionDetails(sessionId: Int): Result<QuestSession> =
        runCatchingAppException {
            apiService.getSessionDetails(sessionId).toDomainModel()
        }

    override suspend fun leaveSession(sessionId: Int): Result<Unit> = runCatchingAppException {
        apiService.leaveSession(sessionId)
    }

    override suspend fun getQuestPoints(sessionId: Int): Result<List<QuestPoint>> =
        runCatchingAppException {
            apiService.getQuestPoints(sessionId).map { it.toDomainModel() }
        }
}
