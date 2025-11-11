package com.goquestly.presentation.registration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.goquestly.R
import com.goquestly.presentation.core.components.FullScreenLoader
import com.goquestly.presentation.core.components.button.PrimaryButton
import com.goquestly.presentation.core.components.textField.AppTextField
import com.goquestly.presentation.core.components.textField.PasswordTextField
import com.goquestly.presentation.core.preview.ThemePreview
import com.goquestly.presentation.core.theme.GoquestlyTheme

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onLoginClick: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val currentOnRegisterSuccess by rememberUpdatedState(onRegisterSuccess)

    LaunchedEffect(state.isRegistrationSuccessful) {
        if (state.isRegistrationSuccessful) {
            currentOnRegisterSuccess()
        }

    }

    RegisterScreenContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onUsernameChange = viewModel::onUsernameChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onRegisterClick = viewModel::onRegisterClick,
        onGoogleSignInClick = viewModel::onGoogleSignInClick,
        onLoginClick = onLoginClick
    )
}

@Composable
private fun RegisterScreenContent(
    state: RegisterState = RegisterState(),
    onEmailChange: (String) -> Unit = {},
    onUsernameChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onConfirmPasswordChange: (String) -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onGoogleSignInClick: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .windowInsetsPadding(WindowInsets.ime)
                    .navigationBarsPadding()
                    .padding(horizontal = 30.dp)
                    .padding(bottom = 24.dp, top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PrimaryButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    text = stringResource(R.string.register),
                    onClick = onRegisterClick,
                    enabled = !state.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                val annotatedString = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    ) {
                        append(stringResource(R.string.already_have_an_account))
                        append(" ")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    ) {
                        append(stringResource(R.string.login))
                    }
                }

                Text(
                    text = annotatedString,
                    modifier = Modifier.clickable(onClick = onLoginClick)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 30.dp)
                    .padding(top = 60.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    modifier = Modifier.size(60.dp),
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = null
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.app_name),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.participate_in_interactive_quests),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

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
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                AppTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.name,
                    onValueChange = onUsernameChange,
                    placeholder = stringResource(R.string.name),
                    isError = state.nameError != null,
                    errorMessage = state.nameError,
                    enabled = !state.isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { passwordFocusRequester.requestFocus() }
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                PasswordTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(passwordFocusRequester),
                    value = state.password,
                    onValueChange = onPasswordChange,
                    placeholder = stringResource(R.string.password),
                    isError = state.passwordError != null,
                    errorMessage = state.passwordError,
                    enabled = !state.isLoading,
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(
                        onNext = { confirmPasswordFocusRequester.requestFocus() }
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                PasswordTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(confirmPasswordFocusRequester),
                    value = state.confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    placeholder = stringResource(R.string.confirm_password),
                    isError = state.confirmPasswordError != null,
                    errorMessage = state.confirmPasswordError,
                    enabled = !state.isLoading,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onRegisterClick()
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

//                Spacer(modifier = Modifier.height(32.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    HorizontalDivider(
//                        modifier = Modifier.weight(1f),
//                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
//                    )
//                    Text(
//                        text = stringResource(R.string.or),
//                        modifier = Modifier.padding(horizontal = 16.dp),
//                        fontSize = 12.sp,
//                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
//                        fontWeight = FontWeight.Medium
//                    )
//                    HorizontalDivider(
//                        modifier = Modifier.weight(1f),
//                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                GoogleSignInButton(
//                    onClick = onGoogleSignInClick,
//                    enabled = !state.isLoading
//                )
            }
        }
    }
    if (state.isLoading) {
        FullScreenLoader()
    }
}

@ThemePreview
@Composable
private fun RegisterScreenPreview() {
    GoquestlyTheme {
        RegisterScreenContent()
    }
}
