package com.goquesty.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordRequestDto(
    val email: String
)
