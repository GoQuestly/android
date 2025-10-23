package com.goquesty.presentation.resetPassword

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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import com.goquesty.R
import com.goquesty.presentation.core.components.FullScreenLoader
import com.goquesty.presentation.core.components.button.PrimaryButton
import com.goquesty.presentation.core.components.button.SecondaryButton
import com.goquesty.presentation.core.components.textField.AppTextField
import com.goquesty.presentation.core.preview.ThemePreview
import com.goquesty.presentation.core.theme.GoquestlyTheme

@Composable
fun ResetPasswordScreen(
    viewModel: ResetPasswordViewModel = hiltViewModel(),
    openLogin: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val successMessage = stringResource(R.string.reset_link_sent_successfully)

    LaunchedEffect(state.isLinkSent) {
        if (state.isLinkSent) {
            snackbarHostState.showSnackbar(
                message = successMessage,
                duration = SnackbarDuration.Short
            )
            viewModel.onLinkSentHandled()
        }
    }

    ResetPasswordScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onEmailChange = viewModel::onEmailChange,
        onSendLinkClick = viewModel::sendResetPasswordLink,
        onOpenLoginClick = openLogin
    )
}

@Composable
private fun ResetPasswordScreenContent(
    state: ResetPasswordState = ResetPasswordState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onEmailChange: (String) -> Unit = {},
    onSendLinkClick: () -> Unit = {},
    onOpenLoginClick: () -> Unit = {},
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(WindowInsets.ime)
                .background(MaterialTheme.colorScheme.surface)
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
                        .padding(vertical = 32.dp, horizontal = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.reset_password),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    AppTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.email,
                        onValueChange = onEmailChange,
                        placeholder = stringResource(R.string.email),
                        isError = state.emailError != null,
                        errorMessage = state.emailError,
                        enabled = !state.isLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                onSendLinkClick()
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
                        text = stringResource(R.string.send_reset_link),
                        onClick = onSendLinkClick,
                        enabled = !state.isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SecondaryButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        text = stringResource(R.string.back_to_login),
                        onClick = onOpenLoginClick,
                        enabled = !state.isLoading
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            if (state.isLoading) {
                FullScreenLoader()
            }
        }
    }
}

@ThemePreview
@Composable
private fun ResetPasswordScreenPreview() {
    GoquestlyTheme {
        ResetPasswordScreenContent()
    }
}