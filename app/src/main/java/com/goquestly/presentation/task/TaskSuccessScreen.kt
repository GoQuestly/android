package com.goquestly.presentation.task

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.goquestly.R
import com.goquestly.presentation.core.components.button.PrimaryButton
import com.goquestly.presentation.core.preview.ThemePreview
import com.goquestly.presentation.core.theme.GoquestlyTheme

@Composable
fun TaskSuccessScreen(
    scoreEarned: Int = 0,
    passed: Boolean = true,
    isPhotoTask: Boolean = false,
    onReturnToQuest: () -> Unit
) {
    val passed = passed || isPhotoTask

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        if (passed) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.errorContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (passed) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = if (passed) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = when {
                    isPhotoTask -> stringResource(R.string.photo_submitted)
                    passed -> stringResource(R.string.task_completed)
                    else -> stringResource(R.string.task_failed)
                },
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isPhotoTask) {
                Text(
                    text = stringResource(R.string.photo_under_review),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.photo_review_message),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = stringResource(
                        R.string.you_have_earned_points,
                        scoreEarned
                    ),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (passed) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.progress_saved),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            PrimaryButton(
                text = stringResource(R.string.return_to_quest),
                onClick = onReturnToQuest,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@ThemePreview
@Composable
private fun TaskSuccessScreenPreview() {
    GoquestlyTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            TaskSuccessScreen(
                scoreEarned = 10,
                passed = true,
                isPhotoTask = false,
                onReturnToQuest = {}
            )
        }
    }
}

@ThemePreview
@Composable
private fun TaskFailedScreenPreview() {
    GoquestlyTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            TaskSuccessScreen(
                scoreEarned = 3,
                passed = false,
                isPhotoTask = false,
                onReturnToQuest = {}
            )
        }
    }
}

@ThemePreview
@Composable
private fun PhotoTaskSubmittedPreview() {
    GoquestlyTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            TaskSuccessScreen(
                scoreEarned = 0,
                passed = true,
                isPhotoTask = true,
                onReturnToQuest = {}
            )
        }
    }
}
