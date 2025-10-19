package com.goquesty.presentation

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.goquesty.presentation.core.navigation.NavGraph
import com.goquesty.presentation.core.navigation.NavScreen
import com.goquesty.presentation.core.theme.GoquestlyTheme
import com.goquesty.presentation.home.HomeScreen
import com.goquesty.presentation.login.LoginScreen
import com.goquesty.presentation.registration.RegisterScreen
import com.goquesty.presentation.verify_email.VerifyEmailScreen
import com.goquesty.presentation.welcome.WelcomeScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoquestlyTheme {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = NavGraph.AUTH_GRAPH.route
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
                                            navController.navigate(NavScreen.Login.route)
                                        },
                                        onRegisterSuccess = {
                                            navController.navigate(NavScreen.VerifyEmail.route) {
                                                popUpTo(NavScreen.Welcome.route) {
                                                    inclusive = true
                                                }
                                            }
                                        }
                                    )
                                }
                                composable(NavScreen.Login.route) {
                                    LoginScreen(
                                        onRegisterClick = {
                                            navController.navigate(NavScreen.Register.route)
                                        },
                                        onLoginSuccess = { isEmailVerificationNeeded ->
                                            val route =
                                                if (isEmailVerificationNeeded) NavScreen.VerifyEmail else NavScreen.Home

                                            navController.navigate(route.route) {
                                                popUpTo(NavScreen.Welcome.route) {
                                                    inclusive = true
                                                }
                                            }
                                        },
                                    )
                                }
                                composable(NavScreen.VerifyEmail.route) {
                                    VerifyEmailScreen()
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