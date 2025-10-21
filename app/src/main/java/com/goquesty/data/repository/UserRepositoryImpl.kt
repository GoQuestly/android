package com.goquesty.data.repository

import com.goquesty.data.remote.ApiService
import com.goquesty.domain.mapper.toDomainModel
import com.goquesty.domain.model.User
import com.goquesty.domain.repository.UserRepository
import com.goquesty.util.runCatchingAppException
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository {

    override suspend fun getProfile(): Result<User> = runCatchingAppException {
        apiService.getUserProfile().toDomainModel()
    }
}