package com.goquestly.presentation.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goquestly.R
import com.goquestly.domain.exception.UnauthorizedException
import com.goquestly.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, passwordError = null) }
    }

    fun onLoginClick() {
        if (!validateFields()) {
            return
        }

        _state.update {
            it.copy(
                isLoading = true,
                generalError = null,
                emailError = null,
                passwordError = null
            )
        }

        viewModelScope.launch {
            authRepository.login(
                email = _state.value.email,
                password = _state.value.password
            ).also {
                _state.update { it.copy(isLoading = false) }
            }.onSuccess { user ->
                _state.update {
                    it.copy(
                        isLoginSuccessful = true,
                    )
                }
            }.onFailure { error ->
                if (error is UnauthorizedException) {
                    _state.update { it.copy(generalError = context.getString(R.string.error_invalid_credentials)) }
                } else {
                    _state.update { it.copy(generalError = context.getString(R.string.error_something_went_wrong)) }
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

        if (currentState.password.isBlank()) {
            _state.update { it.copy(passwordError = context.getString(R.string.error_password_required)) }
            isValid = false
        }

        return isValid
    }
}