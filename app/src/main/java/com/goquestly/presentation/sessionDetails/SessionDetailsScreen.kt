package com.goquestly.presentation.sessionDetails

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.goquestly.R
import com.goquestly.domain.model.Participant
import com.goquestly.domain.model.ParticipationBlockReason
import com.goquestly.domain.model.ParticipationStatus
import com.goquestly.domain.model.QuestSession
import com.goquestly.domain.model.SessionStatus
import com.goquestly.presentation.core.components.ConfirmationBottomSheet
import com.goquestly.presentation.core.components.SessionStatusBadge
import com.goquestly.presentation.core.components.button.PrimaryButton
import com.goquestly.presentation.core.preview.ThemePreview
import com.goquestly.presentation.core.theme.GoquestlyTheme
import com.goquestly.util.formatDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
fun SessionDetailsScreen(
    viewModel: SessionDetailsViewModel = hiltViewModel(),
    onJoinSession: (sessionId: Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSessionDetails()
    }

    SessionDetailsContent(
        state = state,
        onNavigateBack = { onNavigateBack() },
        onViewAllParticipants = viewModel::toggleParticipantsSheet,
        onDismissParticipantsSheet = viewModel::toggleParticipantsSheet,
        onJoinSession = { viewModel.joinSession(onJoinSession) },
        onShowLeaveConfirmation = viewModel::showLeaveConfirmation,
        onDismissLeaveConfirmation = viewModel::dismissLeaveConfirmation,
        onConfirmLeave = { viewModel.leaveSession { onNavigateBack() } }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
private fun SessionDetailsContent(
    state: SessionDetailsState,
    onNavigateBack: () -> Unit,
    onViewAllParticipants: () -> Unit,
    onDismissParticipantsSheet: () -> Unit,
    onJoinSession: (sessionId: Int) -> Unit,
    onShowLeaveConfirmation: () -> Unit,
    onDismissLeaveConfirmation: () -> Unit,
    onConfirmLeave: () -> Unit
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
                            text = stringResource(R.string.quest_session),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        if (state.session?.status == SessionStatus.SCHEDULED) {
                            IconButton(
                                modifier = Modifier.align(Alignment.CenterEnd),
                                onClick = onShowLeaveConfirmation
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = stringResource(R.string.leave_session),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                },
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

            state.error != null && state.session == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            state.session != null -> {
                SessionDetails(
                    session = state.session,
                    participants = state.participants,
                    currentUserParticipationStatus = state.currentUserParticipationStatus,
                    currentUserBlockReason = state.currentUserBlockReason,
                    onViewAllParticipants = onViewAllParticipants,
                    onJoinSession = onJoinSession,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }

        if (state.isParticipantsSheetOpen) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = onDismissParticipantsSheet,
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                ParticipantsBottomSheet(participants = state.participants)
            }
        }

        if (state.isLeaveConfirmationSheetOpen) {
            ConfirmationBottomSheet(
                title = stringResource(R.string.leave_session_confirmation_title),
                message = stringResource(R.string.leave_session_confirmation_message),
                confirmText = stringResource(R.string.leave),
                cancelText = stringResource(R.string.cancel),
                onConfirm = onConfirmLeave,
                onDismiss = onDismissLeaveConfirmation
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun SessionDetails(
    session: QuestSession,
    participants: List<Participant>,
    currentUserParticipationStatus: ParticipationStatus?,
    currentUserBlockReason: String?,
    onViewAllParticipants: () -> Unit,
    onJoinSession: (sessionId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        if (session.questPhotoUrl != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 24.dp,
                            topEnd = 24.dp
                        )
                    )
            ) {
                AsyncImage(
                    model = session.questPhotoUrl,
                    contentDescription = session.questTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                SessionStatusBadge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    status = session.status
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp)
        ) {
            Text(
                text = session.questTitle,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (session.questDescription != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = session.questDescription,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.starts_at))
                    append(" ")
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                        append(formatDateTime(session.startDate))
                    }
                },
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(20.dp))

            SessionInfoRow(
                icon = {
                    Icon(
                        Icons.Outlined.Timer,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null
                    )
                },
                label = stringResource(R.string.duration_label),
                value = formatDuration(session.questMaxDurationMinutes),
            )

            Spacer(modifier = Modifier.height(16.dp))

            SessionInfoRow(
                icon = {
                    Icon(
                        Icons.Outlined.Flag,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null
                    )
                },
                label = stringResource(R.string.checkpoints_label),
                value = session.questPointCount.toString()
            )

            Spacer(modifier = Modifier.height(16.dp))

            SessionInfoRow(
                icon = {
                    Icon(
                        Icons.Default.LocationOn,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null
                    )
                },
                label = stringResource(R.string.start_point_label),
                value = session.startPointName
            )

            Spacer(modifier = Modifier.height(15.dp))

            ParticipantsPreview(
                participants = participants,
                onViewAll = onViewAllParticipants
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        val isBlockedFromParticipation = currentUserParticipationStatus != null
        val blockReason = currentUserBlockReason?.let {
            ParticipationBlockReason.entries.find { reason -> reason.value == it }
        }

        if (isBlockedFromParticipation) {
            Card(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = when (blockReason) {
                        ParticipationBlockReason.NO_LOCATION -> stringResource(R.string.rejection_reason_no_location)
                        ParticipationBlockReason.TOO_FAR_FROM_START -> stringResource(R.string.rejection_reason_too_far_from_start)
                        ParticipationBlockReason.REQUIRED_TASK_NOT_COMPLETED -> stringResource(R.string.rejection_reason_required_task_not_completed)
                        null -> when (currentUserParticipationStatus) {
                            ParticipationStatus.REJECTED -> stringResource(R.string.participant_rejected_details_message)
                            ParticipationStatus.DISQUALIFIED -> stringResource(R.string.participant_disqualified_details_message)
                            else -> ""
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        PrimaryButton(
            text = when {
                blockReason != null -> stringResource(R.string.participant_rejected_title)
                currentUserParticipationStatus == ParticipationStatus.REJECTED ->
                    stringResource(R.string.participant_rejected_title)

                currentUserParticipationStatus == ParticipationStatus.DISQUALIFIED ->
                    stringResource(R.string.participant_disqualified_title)

                session.isActive -> stringResource(R.string.join_session)
                else -> stringResource(R.string.session_not_active)
            },
            onClick = { onJoinSession(session.id) },
            enabled = session.isActive && !isBlockedFromParticipation,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .height(56.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SessionInfoRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = label,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ParticipantsPreview(
    participants: List<Participant>,
    onViewAll: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Outlined.People,
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = stringResource(R.string.participants_label),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(12.dp))

            val visibleParticipantCount = 5
            Row(horizontalArrangement = Arrangement.spacedBy((-15).dp)) {
                participants.take(visibleParticipantCount).forEach { participant ->
                    ParticipantAvatar(participant.userName, participant.photoUrl)
                }

                if (participants.size > visibleParticipantCount) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+${participants.size - visibleParticipantCount}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Text(
            text = stringResource(R.string.view_all),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable(onClick = onViewAll)
        )
    }
}

@Composable
private fun ParticipantAvatar(name: String, photoUrl: String?) {
    Box(
        modifier = Modifier
            .size(40.dp)
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
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun ParticipantsBottomSheet(participants: List<Participant>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = stringResource(R.string.participants_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(24.dp))

        participants.forEach { participant ->
            ParticipantItem(participant)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ParticipantItem(participant: Participant) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        ParticipantAvatar(participant.userName, participant.photoUrl)

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = participant.userName,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun formatDuration(minutes: Int): String {
    val hours = minutes / 60
    val remainingMinutes = minutes % 60

    return when {
        hours > 0 && remainingMinutes > 0 -> stringResource(
            R.string.duration_hours_minutes,
            hours,
            remainingMinutes
        )

        hours > 0 -> stringResource(R.string.duration_hours_only, hours)
        else -> stringResource(R.string.duration_minutes_only, remainingMinutes)
    }
}

@OptIn(ExperimentalTime::class)
@ThemePreview
@Composable
private fun SessionDetailsScreenPreview() {
    GoquestlyTheme {
        SessionDetailsContent(
            state = SessionDetailsState(
                isLoading = false,
                error = null,
                session = QuestSession(
                    id = 1,
                    questId = 1,
                    questTitle = "Mystery of the Lost City",
                    startDate = Instant.parse("2024-06-01T10:00:00Z"),
                    endDate = null,
                    endReason = null,
                    inviteToken = "abc123",
                    participants = emptyList(),
                    isActive = true,
                    participantCount = 5,
                    questPointCount = 10,
                    passedQuestPointCount = 0,
                    questPhotoUrl = null,
                    questDescription = "Explore the ancient ruins and uncover hidden secrets.",
                    questMaxDurationMinutes = 120,
                    startPointName = "Old Town Square"
                ),
                participants = listOf(
                    Participant(
                        id = 1,
                        userId = 1,
                        userName = "Alice",
                        joinedAt = Instant.parse("2024-06-01T09:50:00Z"),
                        status = ParticipationStatus.APPROVED,
                        rejectionReason = null,
                        photoUrl = null
                    ),
                    Participant(
                        id = 2,
                        userId = 2,
                        userName = "Bob",
                        joinedAt = Instant.parse("2024-06-01T09:55:00Z"),
                        status = ParticipationStatus.APPROVED,
                        rejectionReason = null,
                        photoUrl = null
                    )
                ),
                isParticipantsSheetOpen = false
            ),
            onNavigateBack = {},
            onViewAllParticipants = {},
            onDismissParticipantsSheet = {},
            onJoinSession = {},
            onShowLeaveConfirmation = {},
            onDismissLeaveConfirmation = {},
            onConfirmLeave = {}
        )
    }
}
