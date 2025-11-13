package com.goquestly.presentation.core.pagination

data class PaginationState<T>(
    val items: List<T> = emptyList(),
    val currentPage: Int = 0,
    val pageSize: Int = 20,
    val totalItems: Int = 0,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    val hasMoreItems: Boolean
        get() = items.size < totalItems

    val offset: Int
        get() = currentPage * pageSize

    fun loadingNextPage(): PaginationState<T> = copy(
        isLoadingMore = true,
        error = null
    )

    fun appendPage(newItems: List<T>, totalItems: Int): PaginationState<T> = copy(
        items = items + newItems,
        currentPage = currentPage + 1,
        totalItems = totalItems,
        isLoadingMore = false,
        error = null
    )

    fun refreshing(): PaginationState<T> = copy(
        isRefreshing = true,
        error = null
    )

    fun refreshed(newItems: List<T>, totalItems: Int): PaginationState<T> = copy(
        items = newItems,
        currentPage = if (newItems.isNotEmpty()) 1 else 0,
        totalItems = totalItems,
        isRefreshing = false,
        error = null
    )

    fun withError(error: String): PaginationState<T> = copy(
        isLoadingMore = false,
        isRefreshing = false,
        error = error
    )
}