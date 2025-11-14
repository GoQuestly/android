package com.goquestly.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponseDto(
    @SerialName("access_token")
    val accessToken: String,
    val user: UserDto
)
