package com.goquesty.domain.mapper

import com.goquesty.data.remote.dto.UserDto
import com.goquesty.domain.model.User

fun UserDto.toDomainModel() = User(
    id = userId,
    name = name,
    email = email,
    photoUrl = photoUrl,
    isEmailVerified = isEmailVerified
)