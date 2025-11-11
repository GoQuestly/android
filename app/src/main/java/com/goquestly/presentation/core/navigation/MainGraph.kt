package com.goquestly.presentation.core.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.goquestly.presentation.home.HomeScreen
import com.goquestly.presentation.profile.ProfileScreen
import com.goquestly.presentation.verifyEmail.VerifyEmailScreen

fun NavGraphBuilder.mainGraph(
    navController: NavHostController,
    startWithVerification: Boolean,
    onLogout: () -> Unit
) {
    navigation(
        startDestination = if (startWithVerification) {
            NavScreen.VerifyEmail.route
        } else {
            NavScreen.Home.route
        },
        route = NavGraph.MAIN_GRAPH.route
    ) {
        composable(NavScreen.VerifyEmail.route) {
            VerifyEmailScreen(
                onVerificationSuccess = {
                    navController.navigate(NavScreen.Home.route) {
                        popUpTo(NavScreen.VerifyEmail.route) {
                            inclusive = true
                        }
                    }
                },
                onLogoutClick = onLogout
            )
        }

        composable(NavScreen.Home.route) {
            HomeScreen()
        }

        composable(NavScreen.Profile.route) {
            ProfileScreen(
                onLogoutClick = onLogout
            )
        }
    }
}