package com.goquesty.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterResponseDto(
    @SerialName("access_token")
    val accessToken: String,
    val user: UserDto
)
