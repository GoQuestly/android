package com.goquestly.domain.mapper

import com.goquestly.data.remote.dto.UserDto
import com.goquestly.domain.model.User

fun UserDto.toDomainModel() = User(
    id = userId,
    name = name,
    email = email,
    photoUrl = photoUrl,
    isEmailVerified = isEmailVerified
)