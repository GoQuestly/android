package com.goquesty.data.repository

import com.goquesty.data.remote.ApiService
import com.goquesty.data.remote.dto.UpdateProfileDto
import com.goquesty.domain.mapper.toDomainModel
import com.goquesty.domain.model.User
import com.goquesty.domain.repository.UserRepository
import com.goquesty.util.imageMimeType
import com.goquesty.util.runCatchingAppException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository {

    override suspend fun getProfile(): Result<User> = runCatchingAppException {
        apiService.getUserProfile().toDomainModel()
    }

    override suspend fun updateProfile(name: String): Result<User> = runCatchingAppException {
        val request = UpdateProfileDto(name)
        apiService.updateUserProfile(request).toDomainModel()
    }

    override suspend fun updateAvatar(avatar: File) = runCatchingAppException {

        val requestFile = avatar.asRequestBody(avatar.imageMimeType.toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData(
            name = "file",
            filename = avatar.name,
            body = requestFile
        )
        apiService.updateAvatar(filePart)
    }
}