package com.goquestly.presentation.core.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.goquestly.presentation.activeSession.ActiveSessionScreen
import com.goquestly.presentation.home.HomeScreen
import com.goquestly.presentation.invite.InviteHandlerScreen
import com.goquestly.presentation.profile.ProfileScreen
import com.goquestly.presentation.sessionDetails.SessionDetailsScreen
import com.goquestly.presentation.task.TaskScreen
import com.goquestly.presentation.task.TaskSuccessScreen
import com.goquestly.presentation.verifyEmail.VerifyEmailScreen
import com.goquestly.presentation.statistics.StatisticsScreen
import com.goquestly.util.INVITE_DEEP_LINK_PREFIX

fun NavGraphBuilder.mainGraph(
    navController: NavHostController,
    startWithVerification: Boolean,
    onLogout: () -> Unit
) {
    val startDestination = when {
        startWithVerification -> NavScreen.VerifyEmail.route
        else -> NavScreen.Home.route
    }

    navigation(
        startDestination = startDestination,
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
            val navBackStackEntry = it
            HomeScreen(
                navBackStackEntry = navBackStackEntry,
                onSessionClick = { sessionId ->
                    navController.navigate(NavScreen.SessionDetails.createRoute(sessionId))
                }
            )
        }

        composable(NavScreen.Profile.route) {
            ProfileScreen(
                onLogoutClick = onLogout
            )
        }

        composable(
            route = NavScreen.SessionDetails.route,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.IntType }
            )
        ) {
            SessionDetailsScreen(
                onJoinSession = {
                    navController.navigate(NavScreen.ActiveSession.createRoute(it))
                },
                onNavigateBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(NavScreen.Home.route) {
                            popUpTo(NavScreen.Home.route) {
                                inclusive = true
                            }
                        }
                    }
                }
            )
        }

        composable(
            route = NavScreen.InviteHandler.route,
            arguments = listOf(
                navArgument("inviteToken") { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "$INVITE_DEEP_LINK_PREFIX/{inviteToken}"
                }
            )
        ) {
            InviteHandlerScreen(
                onNavigateToSessionDetails = { sessionId ->
                    navController.navigate(NavScreen.SessionDetails.createRoute(sessionId)) {
                        popUpTo(NavScreen.Home.route) {
                            inclusive = false
                        }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(NavScreen.Home.route) {
                        popUpTo(NavScreen.Home.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(
            route = NavScreen.ActiveSession.route,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getInt("sessionId") ?: 0
            ActiveSessionScreen(
                onLeaveSession = {
                    navController.navigate(NavScreen.SessionDetails.createRoute(sessionId)) {
                        popUpTo(NavScreen.Home.route) {
                            inclusive = false
                        }
                    }
                },
                onNavigateToTask = { sId, pointId, pointName ->
                    navController.navigate(NavScreen.Task.createRoute(sId, pointId, pointName))
                }
            )
        }

        composable(
            route = NavScreen.Task.route,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.IntType },
                navArgument("pointId") { type = NavType.IntType },
                navArgument("pointName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            TaskScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSuccess = { sId, pId, score, maxScore, passed, isPhotoTask ->
                    navController.navigate(
                        NavScreen.TaskSuccess.createRoute(
                            sId,
                            pId,
                            score,
                            passed,
                            isPhotoTask
                        )
                    ) {
                        popUpTo(NavScreen.Task.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(
            route = NavScreen.TaskSuccess.route,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.IntType },
                navArgument("pointId") { type = NavType.IntType },
                navArgument("scoreEarned") { type = NavType.IntType },
                navArgument("passed") { type = NavType.BoolType },
                navArgument("isPhotoTask") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getInt("sessionId") ?: 0
            val scoreEarned = backStackEntry.arguments?.getInt("scoreEarned") ?: 0
            val passed = backStackEntry.arguments?.getBoolean("passed") ?: false
            val isPhotoTask = backStackEntry.arguments?.getBoolean("isPhotoTask") ?: false

            TaskSuccessScreen(
                scoreEarned = scoreEarned,
                passed = passed,
                isPhotoTask = isPhotoTask,
                onReturnToQuest = {
                    navController.navigate(NavScreen.ActiveSession.createRoute(sessionId)) {
                        popUpTo(NavScreen.ActiveSession.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(NavScreen.Statistics.route) {
            StatisticsScreen()
        }
    }
}