package com.goquestly.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GoogleSignInRequestDto(
    val token: String
)
