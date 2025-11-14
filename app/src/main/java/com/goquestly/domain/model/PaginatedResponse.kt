package com.goquestly.domain.model

data class PaginatedResponse<T>(
    val items: List<T>,
    val total: Int,
    val limit: Int,
    val offset: Int
)
