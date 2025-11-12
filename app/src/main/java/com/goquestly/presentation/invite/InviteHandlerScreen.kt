package com.goquestly.presentation.invite

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.goquestly.R
import com.goquestly.presentation.core.components.button.PrimaryButton
import com.goquestly.presentation.core.preview.ThemePreview
import com.goquestly.presentation.core.theme.GoquestlyTheme

@Composable
fun InviteHandlerScreen(
    viewModel: InviteHandlerViewModel = hiltViewModel(),
    onNavigateToSessionDetails: (Int) -> Unit,
    onNavigateToHome: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isJoined, state.sessionId) {
        val sessionId = state.sessionId
        if (state.isJoined && sessionId != null) {
            onNavigateToSessionDetails(sessionId)
        }
    }

    InviteHandlerContent(
        state = state,
        onRetry = viewModel::retry,
        onNavigateToHome = onNavigateToHome
    )
}

@Composable
private fun InviteHandlerContent(
    state: InviteHandlerState,
    onRetry: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(R.string.joining_session),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                state.error != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.error_oops),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.error,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        PrimaryButton(
                            text = stringResource(R.string.try_again),
                            onClick = onRetry,
                            modifier = Modifier.padding(horizontal = 48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        PrimaryButton(
                            text = stringResource(R.string.open_home),
                            onClick = onNavigateToHome,
                            modifier = Modifier.padding(horizontal = 48.dp)
                        )
                    }
                }
            }
        }
    }
}

@ThemePreview
@Composable
private fun InviteHandlerContentLoadingPreview() {
    GoquestlyTheme {
        InviteHandlerContent(
            state = InviteHandlerState(
                isLoading = true,
                isJoined = false,
                sessionId = null,
                error = null
            ),
            onRetry = {},
            onNavigateToHome = {}
        )
    }
}
