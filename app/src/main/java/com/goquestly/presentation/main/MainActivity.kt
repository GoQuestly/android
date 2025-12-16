package com.goquestly.presentation.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.goquestly.data.service.FirebaseMessagingService
import com.goquestly.domain.model.AuthState
import com.goquestly.presentation.core.components.ErrorScreen
import com.goquestly.presentation.core.theme.GoquestlyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity(), OnMapsSdkInitializedCallback {

    private val mainViewModel: MainViewModel by viewModels()
    private var currentIntent by mutableStateOf<Intent?>(null)

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST, this)

        currentIntent = intent
        handleNotificationIntent(intent)

        splashScreen.setKeepOnScreenCondition {
            mainViewModel.state.value.isLoading
        }

        enableEdgeToEdge()
        setContent {
            GoquestlyTheme {
                val state by mainViewModel.state.collectAsState()
                val navController = rememberNavController()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    NotificationPermissionRequest(authState = state.authState)
                }

                Scaffold(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        when {
                            state.isLoading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }

                            state.isError -> {
                                ErrorScreen(
                                    onRetry = mainViewModel::checkAuthState
                                )
                            }

                            state.authState != null -> {
                                AppNavHost(
                                    navController = navController,
                                    authState = state.authState!!,
                                    activeSessionId = state.activeSessionId,
                                    initialIntent = currentIntent,
                                    pendingInviteToken = state.pendingInviteToken,
                                    onAuthStateChanged = mainViewModel::checkAuthState,
                                    onLogout = mainViewModel::logout,
                                    onSetPendingInviteToken = mainViewModel::setPendingInviteToken
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        currentIntent = intent
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        intent?.getStringExtra(FirebaseMessagingService.EXTRA_NOTIFICATION_TYPE)?.let { type ->
            Log.d(TAG, "Notification clicked with type: $type")
        }
    }

    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        Log.d(TAG, "Maps SDK initialized with renderer: $renderer")
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun NotificationPermissionRequest(authState: AuthState?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationPermissionState = rememberPermissionState(
            Manifest.permission.POST_NOTIFICATIONS
        )

        LaunchedEffect(authState) {
            if (authState is AuthState.Authenticated && !notificationPermissionState.status.isGranted) {
                notificationPermissionState.launchPermissionRequest()
            }
        }
    }
}