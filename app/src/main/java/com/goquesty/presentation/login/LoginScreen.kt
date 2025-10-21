package com.goquesty.presentation.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
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
import com.goquesty.presentation.core.components.button.GoogleSignInButton
import com.goquesty.presentation.core.components.button.PrimaryButton
import com.goquesty.presentation.core.components.button.SecondaryButton
import com.goquesty.presentation.core.components.textField.AppTextField
import com.goquesty.presentation.core.components.textField.PasswordTextField
import com.goquesty.presentation.core.preview.ThemePreview
import com.goquesty.presentation.core.theme.GoquestlyTheme

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onRegisterClick: () -> Unit,
    onLoginSuccess: (isEmailVerificationNeeded: Boolean) -> Unit,
    onForgotPasswordClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val currentOnLoginSuccess by rememberUpdatedState(onLoginSuccess)

    LaunchedEffect(state.isLoginSuccessful) {
        if (state.isLoginSuccessful) {
            currentOnLoginSuccess(state.needsEmailVerification)
        }
    }

    LoginScreenContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::onLoginClick,
        onGoogleSignInClick = viewModel::onGoogleSignInClick,
        onRegisterClick = onRegisterClick,
        onForgotPasswordClick = onForgotPasswordClick
    )
}

@Composable
private fun LoginScreenContent(
    state: LoginState = LoginState(),
    onEmailChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onLoginClick: () -> Unit = {},
    onGoogleSignInClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val passwordFocusRequester = remember { FocusRequester() }

    val imeInsets = WindowInsets.ime
    val density = LocalDensity.current
    val isKeyboardVisible by remember {
        derivedStateOf {
            imeInsets.getBottom(density) > 0
        }
    }

    val bottomBarBackgroundColor by animateColorAsState(
        targetValue = if (isKeyboardVisible) {
            MaterialTheme.colorScheme.surface
        } else {
            Color.Transparent
        },
        label = "bottomBarBackground"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.login_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
        val surfaceGradientAlphas =
            if (isSystemInDarkTheme()) listOf(0.5f, 0.8f, 1f) else listOf(0f, 0.5f)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = surfaceGradientAlphas.map { surfaceVariantColor.copy(alpha = it) }
                    )
                )
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bottomBarBackgroundColor)
                        .windowInsetsPadding(WindowInsets.ime)
                        .navigationBarsPadding()
                        .padding(horizontal = 30.dp)
                        .padding(bottom = 24.dp, top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PrimaryButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        text = stringResource(R.string.login),
                        onClick = onLoginClick,
                        enabled = !state.isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SecondaryButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        text = stringResource(R.string.register),
                        onClick = onRegisterClick,
                        enabled = !state.isLoading
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 30.dp)
                        .padding(top = 60.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.welcome_to),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.app_name),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Spacer(modifier = Modifier.height(40.dp))

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp, horizontal = 16.dp)
                        ) {
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
                                imeAction = ImeAction.Done,
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        onLoginClick()
                                    }
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = stringResource(R.string.forgot_password),
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .clickable(onClick = onForgotPasswordClick),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
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

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                                Text(
                                    text = stringResource(R.string.or),
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.Medium
                                )
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            GoogleSignInButton(
                                onClick = onGoogleSignInClick,
                                enabled = !state.isLoading
                            )
                        }
                    }
                }
            }
        }

        if (state.isLoading) {
            FullScreenLoader()
        }
    }
}

@ThemePreview
@Composable
private fun LoginScreenPreview() {
    GoquestlyTheme {
        LoginScreenContent()
    }
}