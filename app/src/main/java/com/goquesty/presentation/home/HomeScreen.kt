package com.goquesty.presentation.home

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.goquesty.presentation.core.preview.ThemePreview
import com.goquesty.presentation.core.theme.GoquestlyTheme

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    HomeScreenContent()
}

@Composable
private fun HomeScreenContent() {
    Text("Home Screen")
}

@ThemePreview
@Composable
private fun HomeScreenPreview() {
    GoquestlyTheme {
        HomeScreenContent()
    }
}

