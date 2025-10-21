package com.goquesty.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goquesty.domain.repository.AuthRepository
import com.goquesty.domain.useCase.GetAuthStateUseCase
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
            _state.update {
                it.copy(
                    isLoading = false,
                    isError = authStateResult.isFailure,
                    authState = authStateResult.getOrNull(),
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            checkAuthState()
        }
    }
}