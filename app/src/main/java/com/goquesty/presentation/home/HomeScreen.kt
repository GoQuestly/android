package com.goquesty.presentation.home

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    HomeScreenContent()
}

@Composable
fun HomeScreenContent() {
    Text("Home NavScreen")
}