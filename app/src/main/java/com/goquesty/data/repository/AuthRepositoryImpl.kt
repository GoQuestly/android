package com.goquesty.data.repository

import com.goquesty.data.remote.ApiService
import com.goquesty.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : AuthRepository
