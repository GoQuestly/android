package com.goquesty.presentation.core.navigation

sealed class NavScreen(val route: String) {
    object Welcome : NavScreen("welcome")
    object Login : NavScreen("login")
    object Register : NavScreen("register")
    object VerifyEmail : NavScreen("verify_email")
    object Home : NavScreen("home")
}