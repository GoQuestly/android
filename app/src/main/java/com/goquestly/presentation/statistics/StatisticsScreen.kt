package com.goquestly.presentation.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.goquestly.R
import com.goquestly.domain.model.SessionStatistics
import com.goquestly.presentation.core.theme.Black18
import kotlin.math.roundToInt

private val CardShape = RoundedCornerShape(20.dp)
private val SmallCardShape = RoundedCornerShape(18.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    val pageBg = MaterialTheme.colorScheme.surface

    Scaffold(
        containerColor = pageBg,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = pageBg
                ),
                title = {
                    Text(
                        text = stringResource(R.string.statistics),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(pageBg)
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.error_something_went_wrong),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = viewModel::load) {
                                Text(stringResource(R.string.try_again))
                            }
                        }
                    }
                }

                state.statistics != null -> {
                    StatisticsContent(
                        modifier = Modifier.fillMaxSize(),
                        stats = state.statistics!!
                    )
                }
            }
        }
    }
}

@Composable
private fun StatisticsContent(
    modifier: Modifier,
    stats: SessionStatistics
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.total_sessions),
                    value = stats.totalSessions.toString()
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.total_score),
                    value = stats.totalScore.toString(),
                    icon = Icons.Default.Star
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.best_rank),
                    value = "#${stats.bestRank}",
                    icon = Icons.Default.EmojiEvents
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.average_rank),
                    value = "#${stats.averageRank}"
                )
            }
        }

        item {
            BigCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.finish_rate),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FinishRateDonut(
                            percent = stats.finishRate.toFloat(),
                            modifier = Modifier.size(124.dp)
                        )

                        Column(Modifier.weight(1f)) {
                            Text(
                                text = stringResource(
                                    R.string.finished_sessions_of_total,
                                    stats.finishedSessions,
                                    stats.totalSessions
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(10.dp))
                            SessionsStatusBars(stats)
                        }
                    }
                }
            }
        }

        item {
            BigCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.progress_breakdown),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MiniStat(
                            modifier = Modifier.weight(1f),
                            title = stringResource(R.string.checkpoints_passed),
                            value = stats.totalCheckpointsPassed.toString()
                        )
                        MiniStat(
                            modifier = Modifier.weight(1f),
                            title = stringResource(R.string.tasks_completed),
                            value = stats.totalTasksCompleted.toString()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun cardContainerColor(): Color {
    return if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        Color.White
    } else {
        Black18
    }
}

@Composable
private fun BigCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = cardContainerColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(content = content)
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector? = null
) {
    Card(
        modifier = modifier,
        shape = SmallCardShape,
        colors = CardDefaults.cardColors(containerColor = cardContainerColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MiniStat(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    val container = MaterialTheme.colorScheme.surface

    Surface(
        modifier = modifier.clip(SmallCardShape),
        color = container
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FinishRateDonut(
    percent: Float,
    modifier: Modifier = Modifier
) {
    val safe = percent.coerceIn(0f, 100f)
    val sweep = (safe / 100f) * 360f

    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val valueColor = MaterialTheme.colorScheme.primary

    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = Stroke(
                width = size.minDimension * 0.10f,
                cap = StrokeCap.Round
            )

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )

            drawArc(
                color = valueColor,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                style = stroke
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${safe.roundToInt()}%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.finished),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SessionsStatusBars(stats: SessionStatistics) {
    val total = stats.totalSessions.coerceAtLeast(1)
    val finished = stats.finishedSessions.coerceAtLeast(0)
    val bad = (stats.rejectedSessions + stats.disqualifiedSessions).coerceAtLeast(0)
    val other = (total - finished - bad).coerceAtLeast(0)

    val finishedRatio = finished.toFloat() / total
    val badRatio = bad.toFloat() / total
    val otherRatio = other.toFloat() / total

    val finishedColor = MaterialTheme.colorScheme.primary
    val badColor = MaterialTheme.colorScheme.error
    val otherColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
    val track = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LegendRow(
            label = stringResource(R.string.finished),
            value = finished,
            total = total,
            color = finishedColor
        )
        LegendRow(
            label = stringResource(R.string.rejected),
            value = bad,
            total = total,
            color = badColor
        )
        LegendRow(
            label = stringResource(R.string.other),
            value = other,
            total = total,
            color = otherColor
        )
    }
}

@Composable
private fun LegendRow(
    label: String,
    value: Int,
    total: Int,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "$value/$total",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}