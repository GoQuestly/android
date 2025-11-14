package com.goquestly.domain.mapper

import com.goquestly.data.remote.dto.PaginatedResponseDto
import com.goquestly.domain.model.PaginatedResponse

fun <T, R> PaginatedResponseDto<T>.toDomainModel(
    itemMapper: (T) -> R
) = PaginatedResponse(
    items = items.map(itemMapper),
    total = total,
    limit = limit,
    offset = offset
)
