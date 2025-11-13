package com.goquestly.presentation.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goquestly.R
import com.goquestly.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            userRepository.getProfile()
                .onSuccess { user ->
                    _state.update {
                        it.copy(
                            name = user.name,
                            editableName = user.name,
                            email = user.email,
                            photoUrl = user.photoUrl,
                            isEmailVerified = user.isEmailVerified,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = context.getString(R.string.error_something_went_wrong)
                        )
                    }
                }
        }
    }

    fun onNameChange(newName: String) {
        _state.update {
            it.copy(
                editableName = newName,
                nameError = null,
                errorMessage = null
            )
        }
    }

    fun updateName() {
        val newName = _state.value.editableName.trim()

        if (newName.isBlank()) {
            _state.update {
                it.copy(nameError = context.getString(R.string.error_name_required))
            }
            return
        }

        if (newName == _state.value.name) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, nameError = null) }

            userRepository.updateProfile(name = newName)
                .onSuccess { user ->
                    _state.update {
                        it.copy(
                            name = user.name,
                            editableName = user.name,
                            isLoading = false,
                            successMessage = context.getString(R.string.profile_updated_successfully)
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = context.getString(R.string.error_something_went_wrong)
                        )
                    }
                }
        }
    }

    fun onAvatarSelected(context: Context, uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val file = uriToFile(context, uri)
                userRepository.updateAvatar(avatar = file)
                    .onSuccess {
                        loadUserProfile()
                        _state.update {
                            it.copy(
                                successMessage = context.getString(R.string.avatar_updated_successfully)
                            )
                        }
                    }
                    .onFailure { error ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = context.getString(R.string.error_something_went_wrong)
                            )
                        }
                    }
                    .also {
                        file.delete()
                    }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = context.getString(R.string.error_something_went_wrong)
                    )
                }
            }
        }
    }

    fun onSuccessMessageShown() {
        _state.update { it.copy(successMessage = null) }
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Cannot open image")

        val tempFile = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
        tempFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        inputStream.close()

        return tempFile
    }
}