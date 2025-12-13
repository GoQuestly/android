package com.goquestly.data.repository

import com.goquestly.data.remote.ApiService
import com.goquestly.data.remote.dto.CodeWordSubmitDto
import com.goquestly.data.remote.dto.JoinSessionRequestDto
import com.goquestly.data.remote.dto.QuizAnswerSubmitDto
import com.goquestly.domain.mapper.toDomain
import com.goquestly.domain.mapper.toDomainModel
import com.goquestly.domain.model.PaginatedResponse
import com.goquestly.domain.model.ParticipantScore
import com.goquestly.domain.model.QuestPoint
import com.goquestly.domain.model.QuestSession
import com.goquestly.domain.model.QuestSessionSummary
import com.goquestly.domain.model.QuestTask
import com.goquestly.domain.model.TaskStartData
import com.goquestly.domain.model.TaskSubmitData
import com.goquestly.domain.repository.SessionRepository
import com.goquestly.util.imageMimeType
import com.goquestly.util.runCatchingAppException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

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

    override suspend fun getTask(sessionId: Int, pointId: Int): Result<QuestTask> =
        runCatchingAppException {
            apiService.getTask(sessionId, pointId).toDomainModel()
        }

    override suspend fun getActiveTask(sessionId: Int): Result<TaskStartData?> =
        runCatchingAppException {
            apiService.getActiveTask(sessionId)?.toDomainModel()
        }

    override suspend fun startTask(sessionId: Int, pointId: Int): Result<TaskStartData> =
        runCatchingAppException {
            apiService.startTask(sessionId, pointId).toDomainModel()
        }

    override suspend fun submitCodeWord(
        sessionId: Int,
        pointId: Int,
        codeWord: String
    ): Result<TaskSubmitData> = runCatchingAppException {
        val request = CodeWordSubmitDto(codeWord)
        apiService.submitCodeWord(sessionId, pointId, request).toDomainModel()
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun submitQuizAnswer(
        sessionId: Int,
        pointId: Int,
        questionId: Int,
        answerIds: List<Int>
    ): Result<TaskSubmitData?> = runCatchingAppException {
        val request = QuizAnswerSubmitDto(questionId, answerIds)
        val response = apiService.submitQuizAnswer(sessionId, pointId, request)

        if (response.completedAt != null) {
            TaskSubmitData(
                success = response.success,
                scoreEarned = response.scoreEarned,
                maxScore = response.maxScore,
                requiredPercentage = response.requiredPercentage,
                passed = response.passed ?: false,
                completedAt = Instant.parse(response.completedAt)
            )
        } else {
            null
        }
    }

    override suspend fun submitPhoto(
        sessionId: Int,
        pointId: Int,
        photoFile: File
    ): Result<TaskSubmitData> = runCatchingAppException {
        val requestFile = photoFile.asRequestBody(photoFile.imageMimeType.toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", photoFile.name, requestFile)
        apiService.submitPhoto(sessionId, pointId, filePart).toDomainModel()
    }

    override suspend fun getSessionScores(sessionId: Int): Result<List<ParticipantScore>> =
        runCatchingAppException {
            apiService.getSessionScores(sessionId).toDomain()
        }
}