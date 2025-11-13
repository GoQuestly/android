package com.goquestly.presentation.invite

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goquestly.R
import com.goquestly.domain.exception.ConflictException
import com.goquestly.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InviteHandlerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: SessionRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val inviteToken: String = checkNotNull(savedStateHandle["inviteToken"])

    private val _state = MutableStateFlow(InviteHandlerState())
    val state = _state.asStateFlow()

    init {
        joinSession()
    }

    private fun joinSession() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            sessionRepository.joinSession(inviteToken)
                .onSuccess { session ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            sessionId = session.id,
                            isJoined = true
                        )
                    }
                }
                .onFailure { error ->
                    val errorMessage = when (error) {
                        is ConflictException -> context.getString(R.string.error_already_joined_session)
                        else -> context.getString(R.string.error_invalid_invite_link)
                    }

                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = errorMessage
                        )
                    }
                }
        }
    }

    fun retry() {
        joinSession()
    }
}
