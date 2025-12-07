package com.goquestly.presentation.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.goquestly.R
import com.goquestly.domain.model.SessionStatus

@Composable
fun SessionStatusBadge(
    status: SessionStatus,
    modifier: Modifier = Modifier
) {
    val text = when (status) {
        SessionStatus.IN_PROGRESS -> stringResource(R.string.in_progress)
        SessionStatus.SCHEDULED -> stringResource(R.string.scheduled)
        SessionStatus.COMPLETED -> stringResource(R.string.completed)
        SessionStatus.CANCELLED -> stringResource(R.string.cancelled)
    }

    val backgroundColor = when (status) {
        SessionStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
        SessionStatus.SCHEDULED -> MaterialTheme.colorScheme.surfaceContainer
        SessionStatus.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
        SessionStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
    }

    val textColor = when (status) {
        SessionStatus.IN_PROGRESS -> MaterialTheme.colorScheme.onSecondary
        SessionStatus.SCHEDULED -> MaterialTheme.colorScheme.onSecondaryContainer
        SessionStatus.COMPLETED -> MaterialTheme.colorScheme.onTertiaryContainer
        SessionStatus.CANCELLED -> MaterialTheme.colorScheme.onErrorContainer
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