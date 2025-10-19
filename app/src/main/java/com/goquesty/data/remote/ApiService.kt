package com.goquesty.data.remote

import com.goquesty.data.remote.dto.RegisterRequestDto
import com.goquesty.data.remote.dto.RegisterResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): RegisterResponseDto
}