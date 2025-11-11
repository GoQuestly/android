package com.goquestly.presentation.core.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.goquestly.presentation.core.navigation.BottomNavBar
import com.goquestly.presentation.core.navigation.BottomNavItem
import com.goquestly.presentation.core.navigation.NavScreen

@Composable
fun MainScaffold(
    navController: NavHostController,
    content: @Composable (Modifier) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Profile
    )

    val shouldshowBottomNav = NavScreen.shouldShowBottomNav(currentRoute)

    val imeInsets = WindowInsets.ime
    val density = LocalDensity.current
    val isKeyboardVisible by remember {
        derivedStateOf {
            imeInsets.getBottom(density) > 0
        }
    }

    Scaffold(
        bottomBar = {
            if (shouldshowBottomNav && !isKeyboardVisible) {
                BottomNavBar(
                    items = bottomNavItems,
                    currentRoute = currentRoute,
                    onItemClick = { item ->
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            restoreState = true
                            launchSingleTop = true
                        }
                    }
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .then(
                    if (shouldshowBottomNav) {
                        Modifier.imePadding()
                    } else {
                        Modifier
                    }
                )
        ) {
            content(Modifier)
        }
    }
}