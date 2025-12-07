package com.goquestly.domain.repository

import com.goquestly.domain.model.PaginatedResponse
import com.goquestly.domain.model.QuestPoint
import com.goquestly.domain.model.QuestSession
import com.goquestly.domain.model.QuestSessionSummary
import com.goquestly.domain.model.QuestTask
import com.goquestly.domain.model.TaskStartData
import com.goquestly.domain.model.TaskSubmitData
import java.io.File

interface SessionRepository {
    suspend fun joinSession(inviteToken: String): Result<QuestSession>

    suspend fun getJoinedSessions(
        limit: Int,
        offset: Int
    ): Result<PaginatedResponse<QuestSessionSummary>>

    suspend fun getSessionDetails(sessionId: Int): Result<QuestSession>

    suspend fun leaveSession(sessionId: Int): Result<Unit>

    suspend fun getQuestPoints(sessionId: Int): Result<List<QuestPoint>>

    suspend fun getTask(sessionId: Int, pointId: Int): Result<QuestTask>

    suspend fun getActiveTask(sessionId: Int): Result<TaskStartData?>

    suspend fun startTask(sessionId: Int, pointId: Int): Result<TaskStartData>

    suspend fun submitCodeWord(
        sessionId: Int,
        pointId: Int,
        codeWord: String
    ): Result<TaskSubmitData>

    suspend fun submitQuizAnswer(
        sessionId: Int,
        pointId: Int,
        questionId: Int,
        answerIds: List<Int>
    ): Result<TaskSubmitData?>

    suspend fun submitPhoto(sessionId: Int, pointId: Int, photoFile: File): Result<TaskSubmitData>
}
