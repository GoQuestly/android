package com.goquesty.domain.repository

import com.goquesty.domain.model.User

interface UserRepository {
    suspend fun getProfile(): Result<User>
}