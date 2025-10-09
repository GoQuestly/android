package com.goquesty.presentation.core.navigation

sealed class NavScreen(val route: String) {
    object Login : NavScreen("login")
    object Home : NavScreen("home")
}