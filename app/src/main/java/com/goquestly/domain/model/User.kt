package com.goquestly.domain.model

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val photoUrl: String?,
    val isEmailVerified: Boolean
)