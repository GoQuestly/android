package com.goquestly.presentation.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.goquestly.domain.model.AuthState
import com.goquestly.presentation.core.components.MainScaffold
import com.goquestly.presentation.core.navigation.NavGraph
import com.goquestly.presentation.core.navigation.authGraph
import com.goquestly.presentation.core.navigation.mainGraph

@Composable
fun AppNavHost(
    navController: NavHostController,
    authState: AuthState,
    onAuthStateChanged: () -> Unit,
    onLogout: () -> Unit
) {
    val startDestination = when (authState) {
        is AuthState.Unauthenticated -> NavGraph.AUTH_GRAPH.route
        is AuthState.Authenticated -> NavGraph.MAIN_GRAPH.route
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Unauthenticated -> {
                if (navController.currentDestination?.route != NavGraph.AUTH_GRAPH.route) {
                    navController.navigate(NavGraph.AUTH_GRAPH.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }

            is AuthState.Authenticated -> {
                if (navController.currentDestination?.route != NavGraph.MAIN_GRAPH.route) {
                    navController.navigate(NavGraph.MAIN_GRAPH.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    MainScaffold(navController = navController) { modifier ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier
        ) {
            authGraph(
                navController = navController,
                onAuthSuccess = onAuthStateChanged
            )

            mainGraph(
                navController = navController,
                startWithVerification = when (authState) {
                    is AuthState.Authenticated -> !authState.isEmailVerified
                    else -> false
                },
                onLogout = onLogout
            )
        }
    }
}