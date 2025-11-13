package com.goquestly.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavBackStackEntry
import com.goquestly.R
import com.goquestly.domain.model.QuestSessionSummary
import com.goquestly.presentation.core.pagination.LazyListPaginator
import com.goquestly.presentation.core.pagination.PaginationState
import com.goquestly.presentation.core.preview.ThemePreview
import com.goquestly.presentation.core.theme.GoquestlyTheme
import com.goquestly.presentation.home.components.SessionCard
import kotlin.time.ExperimentalTime
import kotlin.time.Instant.Companion.parse

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    navBackStackEntry: NavBackStackEntry,
    onSessionClick: (Int) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    val isFirstResume = rememberSaveable { mutableStateOf(true) }
    val lifecycle = navBackStackEntry.getLifecycle()

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (isFirstResume.value) {
                    isFirstResume.value = false
                } else {
                    viewModel.onRefresh()
                }
            }
        }

        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    HomeScreenContent(
        state = state,
        onRefresh = viewModel::onRefresh,
        onLoadNextPage = viewModel::loadNextPage,
        onSessionClick = onSessionClick
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
private fun HomeScreenContent(
    state: HomeState = HomeState(),
    onRefresh: () -> Unit = {},
    onLoadNextPage: () -> Unit = {},
    onSessionClick: (Int) -> Unit = {}
) {
    val listState = rememberLazyListState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.pagination.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isInitialLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                state.error != null && state.pagination.items.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.error),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.error,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                state.pagination.items.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.no_sessions),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.no_sessions_message),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            vertical = 10.dp, horizontal = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = stringResource(R.string.my_sessions),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        items(
                            items = state.pagination.items,
                            key = { it.id }
                        ) { session ->
                            SessionCard(
                                session = session,
                                onClick = { onSessionClick(session.id) }
                            )
                        }

                        if (state.pagination.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    LazyListPaginator(
                        listState = listState,
                        hasMoreItems = state.pagination.hasMoreItems,
                        isLoadingMore = state.pagination.isLoadingMore,
                        threshold = 3,
                        onLoadMore = onLoadNextPage
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@ThemePreview
@Composable
private fun HomeScreenPreview() {
    GoquestlyTheme {
        HomeScreenContent(
            state = HomeState(
                pagination = PaginationState(
                    items = listOf(
                        QuestSessionSummary(
                            id = 1,
                            questId = 8,
                            questTitle = "The Lost Temple of Eldoria",
                            startDate = parse("2023-10-26T10:00:00.000Z"),
                            endDate = null,
                            isActive = true,
                            participantCount = 1,
                            questPointCount = 8,
                            passedQuestPointCount = 6
                        ),
                        QuestSessionSummary(
                            id = 2,
                            questId = 9,
                            questTitle = "Cipher of the Ancient City",
                            startDate = parse("2023-11-15T14:00:00.000Z"),
                            endDate = null,
                            isActive = false,
                            participantCount = 1,
                            questPointCount = 8,
                            passedQuestPointCount = 0
                        ),
                        QuestSessionSummary(
                            id = 3,
                            questId = 10,
                            questTitle = "The Alchemist's Secret",
                            startDate = parse("2023-09-01T09:00:00.000Z"),
                            endDate = parse("2023-09-30T12:00:00.000Z"),
                            isActive = false,
                            participantCount = 1,
                            questPointCount = 10,
                            passedQuestPointCount = 10
                        )
                    ),
                    totalItems = 3,
                    currentPage = 1
                )
            )
        )
    }
}