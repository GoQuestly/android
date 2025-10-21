package com.goquesty.presentation.verifyEmail

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.goquesty.presentation.core.preview.ThemePreview

@Composable
fun VerifyEmailScreen(
    viewModel: VerifyEmailViewModel = hiltViewModel(),
    onVerified: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    VerifyEmailScreenContent()
}

@Composable
private fun VerifyEmailScreenContent() {
    Text("Verify Email Screen")
}

@ThemePreview
@Composable
private fun VerifyEmailScreenPreview() {
    VerifyEmailScreenContent()
}