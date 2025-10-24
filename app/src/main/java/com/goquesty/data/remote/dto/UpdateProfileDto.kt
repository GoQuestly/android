package com.goquesty.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileDto(
    val name: String,
)
