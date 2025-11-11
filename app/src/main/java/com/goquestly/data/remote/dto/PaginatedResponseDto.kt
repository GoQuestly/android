package com.goquestly.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PaginatedResponseDto<T>(
    val items: List<T>,
    val total: Int,
    val limit: Int,
    val offset: Int
)
