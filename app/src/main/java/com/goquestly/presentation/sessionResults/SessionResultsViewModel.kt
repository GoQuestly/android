package com.goquestly.presentation.sessionResults

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goquestly.R
import com.goquestly.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionResultsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: SessionRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val sessionId: Int = checkNotNull(savedStateHandle["sessionId"])

    private val _state = MutableStateFlow(SessionResultsState())
    val state = _state.asStateFlow()

    fun loadResults() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            sessionRepository.getSessionResults(sessionId)
                .onSuccess { results ->
                    _state.update {
                        it.copy(
                            results = results,
                            isLoading = false
                        )
                    }
                }
                .onFailure {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = context.getString(R.string.error_check_connection_and_retry)
                        )
                    }
                }
        }
    }

    fun retry() {
        loadResults()
    }
}
