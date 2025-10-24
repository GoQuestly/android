package com.goquesty.data.remote

import com.goquesty.data.remote.annotation.RequiresAuth
import com.goquesty.data.remote.dto.AuthResponseDto
import com.goquesty.data.remote.dto.LoginRequestDto
import com.goquesty.data.remote.dto.RegisterRequestDto
import com.goquesty.data.remote.dto.ResetPasswordRequestDto
import com.goquesty.data.remote.dto.UpdateProfileDto
import com.goquesty.data.remote.dto.UserDto
import com.goquesty.data.remote.dto.VerificationStatusResponseDto
import com.goquesty.data.remote.dto.VerifyEmailRequestDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @POST("/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): AuthResponseDto

    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

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
}