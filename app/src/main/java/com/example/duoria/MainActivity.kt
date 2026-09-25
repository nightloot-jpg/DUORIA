package com.example.duoria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.duoria.ui.components.AppDestination
import com.example.duoria.ui.components.AuthDialog
import com.example.duoria.ui.components.DuoriaBottomNav
import com.example.duoria.ui.screens.*
import com.example.duoria.ui.theme.BgDark
import com.example.duoria.ui.theme.DuoriaTheme
import com.example.duoria.viewmodel.DuoriaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DuoriaTheme {
                val viewModel: DuoriaViewModel = viewModel()
                val showAuthDialog by viewModel.showAuthDialog.collectAsState()
                var currentRoute by rememberSaveable { mutableStateOf(AppDestination.HOME.route) }

                BackHandler(enabled = currentRoute != AppDestination.HOME.route) {
                    currentRoute = AppDestination.HOME.route
                }

                if (showAuthDialog) {
                    AuthDialog(
                        viewModel = viewModel,
                        onDismiss = { viewModel.closeAuthDialog() }
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = BgDark,
                    contentWindowInsets = WindowInsets.systemBars,
                    bottomBar = {
                        DuoriaBottomNav(
                            currentRoute = currentRoute,
                            onNavigate = { destination ->
                                currentRoute = destination.route
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentRoute,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "screen_transition"
                        ) { route ->
                            when (route) {
                                AppDestination.HOME.route -> HomeScreen(viewModel = viewModel)
                                AppDestination.CONEXION.route -> ConexionScreen(viewModel = viewModel)
                                AppDestination.CINE.route -> CineScreen(viewModel = viewModel)
                                AppDestination.FEED.route -> FeedScreen(viewModel = viewModel)
                                AppDestination.BOVEDA.route -> BovedaScreen(viewModel = viewModel)
                                else -> HomeScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
