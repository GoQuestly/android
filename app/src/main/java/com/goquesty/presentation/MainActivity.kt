package com.goquesty.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.goquesty.R
import com.goquesty.presentation.core.navigation.NavGraph
import com.goquesty.presentation.core.navigation.NavScreen
import com.goquesty.presentation.core.theme.GoquestlyTheme
import com.goquesty.presentation.home.HomeScreen
import com.goquesty.presentation.login.LoginScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoquestlyTheme {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.Companion.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = NavGraph.AUTH_GRAPH.route
                        ) {
                            navigation(
                                startDestination = NavScreen.Login.route,
                                route = NavGraph.AUTH_GRAPH.route
                            ) {
                                composable(NavScreen.Login.route) {
                                    LoginScreen()
                                }
                            }

                            navigation(
                                startDestination = NavScreen.Login.route,
                                route = NavGraph.MAIN_GRAPH.route
                            ) {
                                composable(NavScreen.Home.route) {
                                    HomeScreen()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}