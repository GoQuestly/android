package com.goquestly.presentation.main

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    initialIntent: Intent?,
    onAuthStateChanged: () -> Unit,
    onLogout: () -> Unit
) {
    val startDestination = when (authState) {
        is AuthState.Unauthenticated -> NavGraph.AUTH_GRAPH.route
        is AuthState.Authenticated -> NavGraph.MAIN_GRAPH.route
    }

    val isInitialIntentDeeplink = remember {
        initialIntent?.data?.pathSegments?.firstOrNull() == "invite"
    }

    var hasHandledInitialNavigation by remember { mutableStateOf(false) }
    var lastHandledIntent by remember { mutableStateOf<Intent?>(null) }

    LaunchedEffect(initialIntent) {
        val intentData = initialIntent?.data

        if (initialIntent == lastHandledIntent) {
            return@LaunchedEffect
        }

        val isCurrentIntentDeeplink = intentData?.pathSegments?.firstOrNull() == "invite"

        if (!hasHandledInitialNavigation && isInitialIntentDeeplink) {
            return@LaunchedEffect
        }

        if (isCurrentIntentDeeplink) {
            val inviteToken = intentData?.pathSegments?.getOrNull(1)
            if (inviteToken != null && authState is AuthState.Authenticated) {
                val targetRoute = "invite/$inviteToken"
                if (navController.currentDestination?.route != targetRoute) {
                    navController.navigate(targetRoute) {
                        launchSingleTop = true
                    }
                    lastHandledIntent = initialIntent
                }
            }
        }
    }

    LaunchedEffect(authState) {
        if (isInitialIntentDeeplink && !hasHandledInitialNavigation) {
            hasHandledInitialNavigation = true
            return@LaunchedEffect
        }

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