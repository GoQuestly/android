package com.goquestly.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goquestly.data.local.ActiveSessionManager
import com.goquestly.data.messaging.FcmTokenManager
import com.goquestly.domain.model.AuthState
import com.goquestly.domain.repository.AuthRepository
import com.goquestly.domain.useCase.GetAuthStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val authRepository: AuthRepository,
    private val activeSessionManager: ActiveSessionManager,
    private val fcmTokenManager: FcmTokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state = _state.asStateFlow()

    init {
        checkAuthState()
    }

    fun checkAuthState() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isError = false) }
            val authStateResult = getAuthStateUseCase()
            val activeSessionId = activeSessionManager.getActiveSessionId()
            _state.update {
                it.copy(
                    isLoading = false,
                    isError = authStateResult.isFailure,
                    authState = authStateResult.getOrNull(),
                    activeSessionId = activeSessionId
                )
            }

            if (authStateResult.getOrNull() is AuthState.Authenticated) {
                fcmTokenManager.registerTokenIfNeeded()
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            fcmTokenManager.unregisterToken()
            authRepository.logout()
            checkAuthState()
        }
    }

    fun setPendingInviteToken(token: String?) {
        _state.update { it.copy(pendingInviteToken = token) }
    }
}