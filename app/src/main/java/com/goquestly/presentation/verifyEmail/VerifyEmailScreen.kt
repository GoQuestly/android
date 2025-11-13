package com.goquestly.presentation.verifyEmail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.goquestly.R
import com.goquestly.presentation.core.components.FullScreenLoader
import com.goquestly.presentation.core.components.button.LogoutButton
import com.goquestly.presentation.core.components.button.PrimaryButton
import com.goquestly.presentation.core.components.button.SecondaryButton
import com.goquestly.presentation.core.components.textField.AppTextField
import com.goquestly.presentation.core.preview.ThemePreview
import com.goquestly.presentation.core.theme.GoquestlyTheme

@Composable
fun VerifyEmailScreen(
    viewModel: VerifyEmailViewModel = hiltViewModel(),
    onVerificationSuccess: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val codeResentMessage = stringResource(R.string.verification_code_resent)

    LaunchedEffect(Unit) {
        viewModel.checkVerificationStatus()
    }

    val currentOnVerificationSuccess by rememberUpdatedState(onVerificationSuccess)
    LaunchedEffect(state.isVerified) {
        if (state.isVerified) {
            currentOnVerificationSuccess()
        }
    }

    LaunchedEffect(state.isCodeResent) {
        if (state.isCodeResent) {
            snackbarHostState.showSnackbar(
                message = codeResentMessage,
                duration = SnackbarDuration.Short
            )
            viewModel.onCodeResentShown()
        }
    }

    VerifyEmailScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onCodeChange = viewModel::onCodeChange,
        onVerifyClick = viewModel::verifyCode,
        onResendClick = viewModel::resendVerificationCode,
        onLogoutClick = onLogoutClick
    )
}

@Composable
private fun VerifyEmailScreenContent(
    state: VerifyEmailState = VerifyEmailState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onCodeChange: (String) -> Unit = {},
    onVerifyClick: () -> Unit = {},
    onResendClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            ) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    shape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionColor = MaterialTheme.colorScheme.primary
                )
            }
        },
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                LogoutButton(
                    onClick = onLogoutClick,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(
                            top = 20.dp, end = 20.dp
                        )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(WindowInsets.ime)
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 30.dp)
                    .padding(top = 60.dp, bottom = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    state.secondsBeforeResend?.let { seconds ->
                        TimerCircle(
                            seconds = seconds,
                            modifier = Modifier.size(120.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    Text(
                        text = stringResource(R.string.please_enter_code_from_email),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    AppTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.verificationCode,
                        onValueChange = onCodeChange,
                        placeholder = stringResource(R.string.verification_code),
                        isError = state.codeError != null,
                        errorMessage = state.codeError,
                        enabled = !state.isLoading && !state.isVerified,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                onVerifyClick()
                            }
                        )
                    )

                    AnimatedVisibility(
                        visible = state.generalError != null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = state.generalError ?: "",
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .fillMaxWidth(),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    PrimaryButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        text = stringResource(R.string.ok),
                        onClick = onVerifyClick,
                        enabled = !state.isLoading && state.verificationCode.isNotBlank()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SecondaryButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        text = stringResource(R.string.resend_code),
                        onClick = onResendClick,
                        enabled = !state.isLoading && state.secondsBeforeResend == null
                    )
                }
            }
        }
    }
    if (state.isLoading) {
        FullScreenLoader()
    }
}

@Composable
private fun TimerCircle(
    seconds: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.secondary,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formatTime(seconds),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondary
        )
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}

@ThemePreview
@Composable
private fun VerifyEmailScreenPreview() {
    GoquestlyTheme {
        VerifyEmailScreenContent(
            state = VerifyEmailState(
                secondsBeforeResend = 299
            )
        )
    }
}

@ThemePreview
@Composable
private fun VerifyEmailScreenNoTimerPreview() {
    GoquestlyTheme {
        VerifyEmailScreenContent(
            state = VerifyEmailState(
                secondsBeforeResend = null,
                verificationCode = "123456"
            )
        )
    }
}