package com.goquesty.presentation.login

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LoginScreen(viewModel: LoginViewModel = hiltViewModel()) {
    LoginScreenContent()
}

@Composable
fun LoginScreenContent() {
    Text("Login NavScreen")
}

@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreenContent()
}