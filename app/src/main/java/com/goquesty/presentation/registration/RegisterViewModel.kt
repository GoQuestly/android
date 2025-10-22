package com.goquesty.presentation.registration

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goquesty.R
import com.goquesty.domain.exception.BadRequestException
import com.goquesty.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val authRepository: AuthRepository
) : ViewModel() {

    private companion object {
        const val MIN_PASSWORD_LENGTH = 4
    }

    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, emailError = null) }
    }

    fun onUsernameChange(username: String) {
        _state.update { it.copy(name = username, nameError = null) }
    }

    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, passwordError = null) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _state.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
    }

    fun onRegisterClick() {
        if (!validateFields()) {
            return
        }

        _state.update {
            it.copy(
                isLoading = true,
                generalError = null,
                emailError = null,
                nameError = null,
                passwordError = null,
                confirmPasswordError = null,
            )
        }

        viewModelScope.launch {
            authRepository.register(
                email = _state.value.email,
                name = _state.value.name,
                password = _state.value.password
            ).onSuccess {
                authRepository.sendVerificationCode()
                _state.update { it.copy(isRegistrationSuccessful = true, isLoading = false) }
            }.onFailure { error ->
                if (error is BadRequestException) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            emailError = context.getString(R.string.error_email_already_exists)
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            generalError = context.getString(R.string.error_something_went_wrong)
                        )
                    }
                }
            }
        }
    }

    fun onGoogleSignInClick() {

    }

    private fun validateFields(): Boolean {
        val currentState = _state.value
        var isValid = true

        if (currentState.email.isBlank()) {
            _state.update { it.copy(emailError = context.getString(R.string.error_email_required)) }
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            _state.update { it.copy(emailError = context.getString(R.string.error_email_invalid)) }
            isValid = false
        }

        if (currentState.name.isBlank()) {
            _state.update { it.copy(nameError = context.getString(R.string.error_name_required)) }
            isValid = false
        }

        if (currentState.password.isBlank()) {
            _state.update { it.copy(passwordError = context.getString(R.string.error_password_required)) }
            isValid = false
        } else if (currentState.password.length < MIN_PASSWORD_LENGTH) {
            _state.update {
                it.copy(
                    passwordError = context.getString(
                        R.string.error_password_too_short,
                        MIN_PASSWORD_LENGTH
                    )
                )
            }
            isValid = false
        }

        if (currentState.confirmPassword.isBlank()) {
            _state.update { it.copy(confirmPasswordError = context.getString(R.string.error_confirm_password_required)) }
            isValid = false
        } else if (currentState.password != currentState.confirmPassword) {
            _state.update { it.copy(confirmPasswordError = context.getString(R.string.error_passwords_not_match)) }
            isValid = false
        }

        return isValid
    }
}