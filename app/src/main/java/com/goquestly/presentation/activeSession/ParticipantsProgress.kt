package com.goquestly.presentation.activeSession

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.goquestly.domain.model.ParticipantScore
import com.goquestly.presentation.core.components.ProfileAvatar

@Composable
fun ParticipantsProgress(
    leaderboard: List<ParticipantScore>,
    currentUserId: Int?
) {
    if (leaderboard.isEmpty()) return

    Column {
        Text(
            text = "Participants progress",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        leaderboard.forEachIndexed { index, participant ->
            ParticipantProgressItem(
                rank = index + 1,
                participant = participant,
                isMe = currentUserId != null && participant.userId == currentUserId
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ParticipantProgressItem(
    rank: Int,
    participant: ParticipantScore,
    isMe: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isMe) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainer,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$rank", fontWeight = FontWeight.Bold)

        Spacer(Modifier.width(12.dp))

        ProfileAvatar(
            avatarUrl = participant.photoUrl,
            onClick = {},
            size = 40.dp
        )

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(participant.userName, fontWeight = FontWeight.SemiBold)
            Text(
                "Tasks: ${participant.completedTasksCount}/${participant.totalTasksInQuest}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            "${participant.totalScore}",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}