package com.goquestly.presentation.resetPassword

import android.content.Context
import android.util.Patterns.EMAIL_ADDRESS
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goquestly.R
import com.goquestly.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ResetPasswordState())
    val state = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, emailError = null) }
    }

    fun onLinkSentHandled() {
        _state.update { it.copy(isLinkSent = false) }
    }

    fun sendResetPasswordLink() {
        if (!validateFields()) {
            return
        }

        _state.update {
            it.copy(
                isLoading = true,
                generalError = null,
                emailError = null,
            )
        }

        viewModelScope.launch {
            authRepository.requestPasswordReset(
                email = _state.value.email,
            ).also {
                _state.update { it.copy(isLoading = false) }
            }.onSuccess { user ->
                _state.update {
                    it.copy(isLinkSent = true)
                }
            }.onFailure {
                _state.update { it.copy(generalError = context.getString(R.string.error_something_went_wrong)) }
            }
        }
    }

    private fun validateFields(): Boolean {
        val currentState = _state.value
        var isValid = true

        if (currentState.email.isBlank()) {
            _state.update { it.copy(emailError = context.getString(R.string.error_email_required)) }
            isValid = false
        } else if (!EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            _state.update { it.copy(emailError = context.getString(R.string.error_email_invalid)) }
            isValid = false
        }

        return isValid
    }
}