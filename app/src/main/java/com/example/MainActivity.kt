package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.MainScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.CuteTheme
import com.example.ui.theme.FriendCratTheme
import com.example.ui.viewmodel.ChatViewModel
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.collectLatest

enum class AuthFlowScreen {
    WELCOME, LOGIN, REGISTER
}

class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channel for chat messages
        NotificationHelper.createNotificationChannel(this)

        // Handle possible notification click
        handleNotificationIntent(intent)

        setContent {
            val appTheme by viewModel.appTheme.collectAsStateWithLifecycle()

            FriendCratTheme(appTheme = appTheme) {
                val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
                var authScreenState by remember { mutableStateOf(AuthFlowScreen.WELCOME) }

                // Request notification permission on Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val notificationPermissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        if (!isGranted) {
                            // User declined notifications; chat will still work normally
                        }
                    }

                    LaunchedEffect(Unit) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                // Observe toast messages
                LaunchedEffect(Unit) {
                    viewModel.toastMessage.collectLatest { msg ->
                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CuteTheme.colors.background
                ) {
                    Crossfade(
                        targetState = isLoggedIn,
                        modifier = Modifier.fillMaxSize(),
                        label = "AuthCrossfade"
                    ) { loggedIn ->
                        if (loggedIn) {
                            MainScreen(viewModel = viewModel)
                        } else {
                            when (authScreenState) {
                                AuthFlowScreen.WELCOME -> {
                                    WelcomeScreen(
                                        onNavigateToLogin = { authScreenState = AuthFlowScreen.LOGIN },
                                        onNavigateToRegister = { authScreenState = AuthFlowScreen.REGISTER }
                                    )
                                }
                                AuthFlowScreen.LOGIN -> {
                                    BackHandler { authScreenState = AuthFlowScreen.WELCOME }
                                    AuthScreen(
                                        initialIsRegister = false,
                                        viewModel = viewModel,
                                        onBack = { authScreenState = AuthFlowScreen.WELCOME }
                                    )
                                }
                                AuthFlowScreen.REGISTER -> {
                                    BackHandler { authScreenState = AuthFlowScreen.WELCOME }
                                    AuthScreen(
                                        initialIsRegister = true,
                                        viewModel = viewModel,
                                        onBack = { authScreenState = AuthFlowScreen.WELCOME }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val targetFriendId = intent?.getStringExtra(NotificationHelper.EXTRA_FRIEND_ID)
        if (!targetFriendId.isNullOrBlank()) {
            viewModel.openChatByUserId(targetFriendId)
        }
    }
}
