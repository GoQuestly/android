package com.goquesty.data.remote

import com.goquesty.data.remote.dto.AuthResponseDto
import com.goquesty.data.remote.dto.LoginRequestDto
import com.goquesty.data.remote.dto.RegisterRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): AuthResponseDto

    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto
}