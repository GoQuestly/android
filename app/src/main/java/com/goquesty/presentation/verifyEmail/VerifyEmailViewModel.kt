package com.goquesty.presentation.verifyEmail

import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goquesty.R
import com.goquesty.domain.exception.BadRequestException
import com.goquesty.domain.model.VerificationStatus
import com.goquesty.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val authRepository: AuthRepository
) : ViewModel() {

    private companion object {
        const val VERIFICATION_CODE_LENGTH = 6
    }

    private val _state = MutableStateFlow(VerifyEmailState())
    val state = _state.asStateFlow()

    private var timerJob: Job? = null

    fun checkVerificationStatus() {
        viewModelScope.launch {
            authRepository.getVerificationStatus()
                .onSuccess { status ->
                    when (status) {
                        is VerificationStatus.Verified -> {
                            _state.update { it.copy(isVerified = true) }
                        }

                        is VerificationStatus.Unverified.CanResendCode -> {
                            _state.update { it.copy(secondsBeforeResend = null) }
                        }

                        is VerificationStatus.Unverified.CannotResendCode -> {
                            startResendTimer(status.canResendAtElapsedRealtime)
                        }
                    }
                }
                .onFailure {
                    _state.update {
                        it.copy(
                            generalError = context.getString(R.string.error_something_went_wrong)
                        )
                    }
                }
        }
    }

    fun onCodeChange(code: String) {
        if (code.length > VERIFICATION_CODE_LENGTH) {
            return
        }
        _state.update { it.copy(verificationCode = code, codeError = null) }
    }

    fun verifyCode() {
        _state.update {
            it.copy(
                isLoading = true,
                generalError = null,
                codeError = null
            )
        }

        viewModelScope.launch {
            authRepository.verifyEmail(code = _state.value.verificationCode)
                .also {
                    _state.update { it.copy(isLoading = false) }
                }
                .onSuccess {
                    timerJob?.cancel()
                    _state.update { it.copy(isVerified = true) }
                }
                .onFailure { error ->
                    if (error is BadRequestException) {
                        _state.update {
                            it.copy(
                                generalError = context.getString(R.string.error_invalid_verification_code)
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                generalError = context.getString(R.string.error_something_went_wrong)
                            )
                        }
                    }
                }
        }
    }

    fun resendVerificationCode() {
        if (_state.value.secondsBeforeResend != null) {
            return
        }

        _state.update {
            it.copy(
                isLoading = true,
                generalError = null,
                codeError = null
            )
        }

        viewModelScope.launch {
            authRepository.sendVerificationCode()
                .also {
                    _state.update { it.copy(isLoading = false) }
                }
                .onSuccess {
                    _state.update { it.copy(isCodeResent = true, verificationCode = "") }
                    checkVerificationStatus()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            generalError = context.getString(R.string.error_something_went_wrong)
                        )
                    }
                }
        }
    }

    fun onCodeResentShown() {
        _state.update { it.copy(isCodeResent = false) }
    }

    private fun startResendTimer(canResendAtElapsedRealtime: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val currentElapsedRealtime = SystemClock.elapsedRealtime()
                val remainingMillis = canResendAtElapsedRealtime - currentElapsedRealtime

                if (remainingMillis <= 0) {
                    _state.update { it.copy(secondsBeforeResend = null) }
                    break
                }

                val remainingSeconds = (remainingMillis / 1000).toInt()
                _state.update { it.copy(secondsBeforeResend = remainingSeconds) }

                delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}