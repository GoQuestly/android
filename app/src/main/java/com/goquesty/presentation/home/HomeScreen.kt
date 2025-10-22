package com.goquesty.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.goquesty.presentation.core.preview.ThemePreview
import com.goquesty.presentation.core.theme.GoquestlyTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    HomeScreenContent()
}

@Composable
private fun HomeScreenContent() {
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text("Home Screen")
        }
    }
}

@ThemePreview
@Composable
private fun HomeScreenPreview() {
    GoquestlyTheme {
        HomeScreenContent()
    }
}