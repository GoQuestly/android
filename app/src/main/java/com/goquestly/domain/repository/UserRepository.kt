package com.goquestly.domain.repository

import com.goquestly.domain.model.User
import java.io.File

interface UserRepository {
    suspend fun getProfile(): Result<User>
    suspend fun updateProfile(name: String): Result<User>
    suspend fun updateAvatar(avatar: File): Result<Unit>
}