package com.goquesty.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class VerifyEmailRequestDto(
    val code: String
)
