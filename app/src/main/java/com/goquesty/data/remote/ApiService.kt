package com.goquesty.data.remote

import com.goquesty.data.remote.annotation.RequiresAuth
import com.goquesty.data.remote.dto.AuthResponseDto
import com.goquesty.data.remote.dto.LoginRequestDto
import com.goquesty.data.remote.dto.RegisterRequestDto
import com.goquesty.data.remote.dto.ResetPasswordRequestDto
import com.goquesty.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): AuthResponseDto

    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    @POST("/auth/request-password-reset")
    suspend fun requestPasswordReset(@Body request: ResetPasswordRequestDto)

    @RequiresAuth
    @GET("/user/profile")
    suspend fun getUserProfile(): UserDto
}