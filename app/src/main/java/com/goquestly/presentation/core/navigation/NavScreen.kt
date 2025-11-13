package com.goquestly.presentation.core.navigation

sealed class NavScreen(val route: String) {
    data object Welcome : NavScreen("welcome")
    data object Login : NavScreen("login")
    data object Register : NavScreen("register")
    data object ResetPassword : NavScreen("reset_password")
    data object VerifyEmail : NavScreen("verify_email")
    data object Home : NavScreen("home")
    data object Profile : NavScreen("profile")
    data object SessionDetails : NavScreen("session_details/{sessionId}") {
        fun createRoute(sessionId: Int) = "session_details/$sessionId"
    }
    data object InviteHandler : NavScreen("invite/{inviteToken}") {
        fun createRoute(inviteToken: String) = "invite/$inviteToken"
    }
    data object ActiveSession : NavScreen("active_session/{sessionId}") {
        fun createRoute(sessionId: Int) = "active_session/$sessionId"
    }

    companion object {
        private val screensWithBottomNav by lazy {
            setOf(
                Home.route,
                Profile.route
            )
        }

        fun shouldShowBottomNav(route: String?): Boolean {
            return route in screensWithBottomNav
        }
    }
}