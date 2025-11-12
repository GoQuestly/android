package com.goquestly.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goquestly.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        loadInitialSessions()
    }

    private fun loadInitialSessions() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isInitialLoading = true,
                    error = null
                )
            }

            loadSessionsPage(limit = _state.value.pagination.pageSize, offset = 0)
                .onSuccess { paginatedResponse ->
                    _state.update {
                        it.copy(
                            pagination = it.pagination.refreshed(
                                newItems = paginatedResponse.items,
                                totalItems = paginatedResponse.total
                            ),
                            isInitialLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isInitialLoading = false,
                            error = throwable.message
                        )
                    }
                }
        }
    }

    fun loadNextPage() {
        val currentState = _state.value

        if (currentState.pagination.isLoadingMore || !currentState.pagination.hasMoreItems) {
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    pagination = it.pagination.loadingNextPage()
                )
            }

            val pagination = _state.value.pagination
            loadSessionsPage(limit = pagination.pageSize, offset = pagination.offset)
                .onSuccess { paginatedResponse ->
                    _state.update {
                        it.copy(
                            pagination = it.pagination.appendPage(
                                newItems = paginatedResponse.items,
                                totalItems = paginatedResponse.total
                            )
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            pagination = it.pagination.withError(
                                error = throwable.message ?: "Unknown error"
                            )
                        )
                    }
                }
        }
    }

    fun onRefresh() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    pagination = it.pagination.refreshing(),
                    error = null
                )
            }

            loadSessionsPage(limit = _state.value.pagination.pageSize, offset = 0)
                .onSuccess { paginatedResponse ->
                    _state.update {
                        it.copy(
                            pagination = it.pagination.refreshed(
                                newItems = paginatedResponse.items,
                                totalItems = paginatedResponse.total
                            ),
                            error = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            pagination = it.pagination.withError(
                                error = throwable.message ?: "Unknown error"
                            ),
                            error = throwable.message
                        )
                    }
                }
        }
    }

    private suspend fun loadSessionsPage(limit: Int, offset: Int) =
        sessionRepository.getJoinedSessions(limit = limit, offset = offset)
}