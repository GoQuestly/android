package com.goquestly.data.remote

import com.goquestly.data.remote.annotation.RequiresAuth
import com.goquestly.data.remote.dto.AuthResponseDto
import com.goquestly.data.remote.dto.CodeWordSubmitDto
import com.goquestly.data.remote.dto.DeviceTokenRequestDto
import com.goquestly.data.remote.dto.GoogleSignInRequestDto
import com.goquestly.data.remote.dto.JoinSessionRequestDto
import com.goquestly.data.remote.dto.LoginRequestDto
import com.goquestly.data.remote.dto.PaginatedResponseDto
import com.goquestly.data.remote.dto.QuestPointDto
import com.goquestly.data.remote.dto.QuestSessionDto
import com.goquestly.data.remote.dto.QuestSessionSummaryDto
import com.goquestly.data.remote.dto.QuestTaskDto
import com.goquestly.data.remote.dto.QuizAnswerResponseDto
import com.goquestly.data.remote.dto.QuizAnswerSubmitDto
import com.goquestly.data.remote.dto.RegisterRequestDto
import com.goquestly.data.remote.dto.ResetPasswordRequestDto
import com.goquestly.data.remote.dto.ServerTimeDto
import com.goquestly.data.remote.dto.SessionResultsDto
import com.goquestly.data.remote.dto.SessionScoresDto
import com.goquestly.data.remote.dto.SessionStatisticsDto
import com.goquestly.data.remote.dto.TaskStartResponseDto
import com.goquestly.data.remote.dto.TaskSubmitResponseDto
import com.goquestly.data.remote.dto.UpdateProfileDto
import com.goquestly.data.remote.dto.UserDto
import com.goquestly.data.remote.dto.VerificationStatusResponseDto
import com.goquestly.data.remote.dto.VerifyEmailRequestDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("/server-time")
    suspend fun getServerTime(): ServerTimeDto

    @POST("/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): AuthResponseDto

    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    @POST("/auth/google/mobile")
    suspend fun signInWithGoogle(@Body request: GoogleSignInRequestDto): AuthResponseDto

    @POST("/auth/request-password-reset")
    suspend fun requestPasswordReset(@Body request: ResetPasswordRequestDto)

    @RequiresAuth
    @GET("/auth/verification-status")
    suspend fun getVerificationStatus(): VerificationStatusResponseDto

    @RequiresAuth
    @POST("/auth/send-verification-code")
    suspend fun sendVerificationCode()

    @RequiresAuth
    @POST("/auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequestDto)

    @RequiresAuth
    @GET("/user/profile")
    suspend fun getUserProfile(): UserDto

    @RequiresAuth
    @PATCH("/user/profile")
    suspend fun updateUserProfile(@Body user: UpdateProfileDto): UserDto

    @RequiresAuth
    @Multipart
    @POST("user/profile/avatar")
    suspend fun updateAvatar(
        @Part file: MultipartBody.Part
    )

    @RequiresAuth
    @POST("/user/device-token")
    suspend fun registerDeviceToken(@Body request: DeviceTokenRequestDto)

    @RequiresAuth
    @DELETE("/user/device-token")
    suspend fun deleteDeviceToken()

    @RequiresAuth
    @POST("/participant/sessions/join")
    suspend fun joinSession(@Body request: JoinSessionRequestDto): QuestSessionDto

    @RequiresAuth
    @GET("/participant/sessions/my")
    suspend fun getJoinedSessions(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): PaginatedResponseDto<QuestSessionSummaryDto>

    @RequiresAuth
    @GET("/participant/sessions/{id}")
    suspend fun getSessionDetails(@Path("id") sessionId: Int): QuestSessionDto

    @RequiresAuth
    @DELETE("/participant/sessions/{id}/leave")
    suspend fun leaveSession(@Path("id") sessionId: Int)

    @RequiresAuth
    @GET("/participant/sessions/{id}/points")
    suspend fun getQuestPoints(@Path("id") sessionId: Int): List<QuestPointDto>

    @RequiresAuth
    @GET("/participant/sessions/{sessionId}/points/{pointId}/task")
    suspend fun getTask(
        @Path("sessionId") sessionId: Int,
        @Path("pointId") pointId: Int
    ): QuestTaskDto

    @RequiresAuth
    @GET("/participant/sessions/{sessionId}/active-task")
    suspend fun getActiveTask(
        @Path("sessionId") sessionId: Int
    ): TaskStartResponseDto?

    @RequiresAuth
    @POST("/participant/sessions/{sessionId}/points/{pointId}/task/start")
    suspend fun startTask(
        @Path("sessionId") sessionId: Int,
        @Path("pointId") pointId: Int
    ): TaskStartResponseDto

    @RequiresAuth
    @POST("/participant/sessions/{sessionId}/points/{pointId}/task/submit/code-word")
    suspend fun submitCodeWord(
        @Path("sessionId") sessionId: Int,
        @Path("pointId") pointId: Int,
        @Body request: CodeWordSubmitDto
    ): TaskSubmitResponseDto

    @RequiresAuth
    @POST("/participant/sessions/{sessionId}/points/{pointId}/task/submit/quiz/answer")
    suspend fun submitQuizAnswer(
        @Path("sessionId") sessionId: Int,
        @Path("pointId") pointId: Int,
        @Body request: QuizAnswerSubmitDto
    ): QuizAnswerResponseDto

    @RequiresAuth
    @Multipart
    @POST("/participant/sessions/{sessionId}/points/{pointId}/task/submit/photo")
    suspend fun submitPhoto(
        @Path("sessionId") sessionId: Int,
        @Path("pointId") pointId: Int,
        @Part file: MultipartBody.Part
    ): TaskSubmitResponseDto

    @RequiresAuth
    @GET("/participant/sessions/{id}/scores")
    suspend fun getSessionScores(
        @Path("id") sessionId: Int
    ): SessionScoresDto

    @RequiresAuth
    @GET("/participant/sessions/statistics")
    suspend fun getSessionStatistics(): SessionStatisticsDto

    @RequiresAuth
    @GET("/participant/sessions/{id}/results")
    suspend fun getSessionResults(
        @Path("id") sessionId: Int
    ): SessionResultsDto
}