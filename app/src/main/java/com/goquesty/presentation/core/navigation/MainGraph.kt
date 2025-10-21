package com.goquesty.presentation.core.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.goquesty.presentation.home.HomeScreen
import com.goquesty.presentation.verifyEmail.VerifyEmailScreen

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
                onVerified = {
                    navController.navigate(NavScreen.Home.route) {
                        popUpTo(NavScreen.VerifyEmail.route) {
                            inclusive = true
                        }
                    }
                },
                onLogout = onLogout
            )
        }

        composable(NavScreen.Home.route) {
            HomeScreen(
                onLogout = onLogout
            )
        }
    }
}