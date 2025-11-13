package com.goquestly.presentation.core.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.goquestly.presentation.login.LoginScreen
import com.goquestly.presentation.registration.RegisterScreen
import com.goquestly.presentation.resetPassword.ResetPasswordScreen
import com.goquestly.presentation.welcome.WelcomeScreen

fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    onAuthSuccess: () -> Unit
) {
    navigation(
        startDestination = NavScreen.Welcome.route,
        route = NavGraph.AUTH_GRAPH.route
    ) {
        composable(NavScreen.Welcome.route) {
            WelcomeScreen(
                onLoginClick = {
                    navController.navigate(NavScreen.Login.route)
                },
                onRegisterClick = {
                    navController.navigate(NavScreen.Register.route)
                }
            )
        }

        composable(NavScreen.Register.route) {
            RegisterScreen(
                onLoginClick = {
                    navController.navigate(NavScreen.Login.route) {
                        popUpTo(NavScreen.Welcome.route)
                    }
                },
                onRegisterSuccess = {
                    onAuthSuccess()
                }
            )
        }

        composable(NavScreen.Login.route) {
            LoginScreen(
                onRegisterClick = {
                    navController.navigate(NavScreen.Register.route) {
                        popUpTo(NavScreen.Welcome.route)
                    }
                },
                onLoginSuccess = {
                    onAuthSuccess()
                },
                onForgotPasswordClick = {
                    navController.navigate(NavScreen.ResetPassword.route)
                }
            )
        }

        composable(NavScreen.ResetPassword.route) {
            ResetPasswordScreen(
                openLogin = {
                    navController.navigate(NavScreen.Login.route) {
                        popUpTo(NavScreen.ResetPassword.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}