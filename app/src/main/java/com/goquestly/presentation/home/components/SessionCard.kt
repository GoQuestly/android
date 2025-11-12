package com.goquestly.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.goquestly.R
import com.goquestly.domain.model.QuestSessionSummary
import com.goquestly.util.formatDateTime
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun SessionCard(
    session: QuestSessionSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (session.questPointCount > 0) {
        (session.passedQuestPointCount.toFloat() / session.questPointCount.toFloat() * 100).toInt()
    } else {
        0
    }

    val status = when {
        session.endDate != null -> SessionStatus.COMPLETED
        session.isActive -> SessionStatus.IN_PROGRESS
        else -> SessionStatus.SCHEDULED
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = session.questTitle,
                    modifier = Modifier.weight(1f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                SessionStatusBadge(status = status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            val dateText = if (session.endDate != null) {
                stringResource(R.string.completed_format, formatDateTime(session.endDate))
            } else {
                stringResource(R.string.starts_format, formatDateTime(session.startDate))
            }

            Text(
                text = dateText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.progress),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "$progress%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            SessionProgressBar(progress = progress / 100f)
        }
    }
}

enum class SessionStatus {
    IN_PROGRESS,
    SCHEDULED,
    COMPLETED
}

@Composable
fun SessionStatusBadge(
    status: SessionStatus,
    modifier: Modifier = Modifier
) {
    val text = when (status) {
        SessionStatus.IN_PROGRESS -> stringResource(R.string.in_progress)
        SessionStatus.SCHEDULED -> stringResource(R.string.scheduled)
        SessionStatus.COMPLETED -> stringResource(R.string.completed)
    }

    val backgroundColor = when (status) {
        SessionStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
        SessionStatus.SCHEDULED -> MaterialTheme.colorScheme.surfaceContainer
        SessionStatus.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
    }

    val textColor = when (status) {
        SessionStatus.IN_PROGRESS -> MaterialTheme.colorScheme.onSecondary
        SessionStatus.SCHEDULED -> MaterialTheme.colorScheme.onSecondaryContainer
        SessionStatus.COMPLETED -> MaterialTheme.colorScheme.onTertiaryContainer
    }

    Text(
        text = text,
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = textColor
    )
}

@Composable
fun SessionProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val progressClamped = progress.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(4.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progressClamped)
                .height(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                )
        )
    }
}
