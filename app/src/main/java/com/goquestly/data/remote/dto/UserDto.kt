package com.goquestly.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val userId: Int,
    val name: String,
    val email: String,
    val photoUrl: String?,
    val isEmailVerified: Boolean
)
