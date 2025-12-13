package com.goquestly.presentation.sessionResults

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.goquestly.R
import com.goquestly.domain.model.ParticipantRanking
import com.goquestly.domain.model.SessionResults
import com.goquestly.domain.model.SessionStatistics
import com.goquestly.presentation.core.components.button.PrimaryButton

@Composable
fun SessionResultsScreen(
    viewModel: SessionResultsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadResults()
    }

    SessionResultsContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onRetry = viewModel::retry
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionResultsContent(
    state: SessionResultsState,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        IconButton(
                            modifier = Modifier.align(Alignment.CenterStart),
                            onClick = onNavigateBack
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBackIos,
                                contentDescription = stringResource(R.string.back)
                            )
                        }

                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = stringResource(R.string.session_results_title),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error != null && state.results == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = state.error,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        PrimaryButton(
                            text = stringResource(R.string.retry),
                            onClick = onRetry,
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .height(48.dp)
                        )
                    }
                }
            }

            state.results != null -> {
                SessionResultsDetails(
                    results = state.results,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun SessionResultsDetails(
    results: SessionResults,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        StatisticsCard(statistics = results.statistics)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.leaderboard_title),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (results.rankings.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = stringResource(R.string.no_participants_finished),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            results.rankings.forEachIndexed { index, ranking ->
                RankingItem(ranking = ranking)
                if (index < results.rankings.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StatisticsCard(statistics: SessionStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = stringResource(R.string.session_statistics),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            StatisticRow(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = stringResource(R.string.duration_label),
                value = formatDuration(statistics.sessionDurationSeconds)
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatisticRow(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = stringResource(R.string.total_participants),
                value = statistics.totalParticipantsCount.toString()
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatisticRow(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = stringResource(R.string.finished_participants),
                value = statistics.finishedParticipantsCount.toString()
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatisticRow(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.PersonOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = stringResource(R.string.did_not_finish_participants),
                value = (statistics.rejectedParticipantsCount + statistics.disqualifiedParticipantsCount).toString()
            )
        }
    }
}

@Composable
private fun StatisticRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.size(20.dp),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun RankingItem(ranking: ParticipantRanking) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        RankBadge(rank = ranking.rank)

        Spacer(modifier = Modifier.width(16.dp))

        ParticipantAvatar(
            name = ranking.userName,
            photoUrl = ranking.photoUrl
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = ranking.userName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        R.string.checkpoints_passed,
                        ranking.passedCheckpointsCount
                    ),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (ranking.completionTimeSeconds != null) {
                    Text(
                        text = "• ${formatDuration(ranking.completionTimeSeconds)}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = ranking.totalScore.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.points),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RankBadge(rank: Int) {
    val backgroundColor = when (rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when (rank) {
        1, 2, 3 -> Color.White
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (rank <= 3) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = rank.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
private fun ParticipantAvatar(name: String, photoUrl: String?) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl != null) {
            AsyncImage(
                model = photoUrl,
                contentDescription = name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = name.take(1).uppercase(),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun formatDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return when {
        hours > 0 -> stringResource(R.string.duration_format_hms, hours, minutes, secs)
        minutes > 0 -> stringResource(R.string.duration_format_ms, minutes, secs)
        else -> stringResource(R.string.duration_format_s, secs)
    }
}
