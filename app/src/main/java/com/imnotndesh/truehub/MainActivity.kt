package com.imnotndesh.truehub

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.imnotndesh.truehub.data.helpers.Prefs
import com.imnotndesh.truehub.ui.MainScreen
import com.imnotndesh.truehub.ui.Screen
import com.imnotndesh.truehub.ui.account.AccountSwitcherScreen
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.ModernToastHost
import com.imnotndesh.truehub.ui.components.NoInternetScreen
import com.imnotndesh.truehub.ui.components.ToastManager
import com.imnotndesh.truehub.ui.login.LoginScreen
import com.imnotndesh.truehub.ui.settings.SettingsScreen
import com.imnotndesh.truehub.ui.settings.screens.AboutScreen
import com.imnotndesh.truehub.ui.settings.screens.LicensesScreen
import com.imnotndesh.truehub.ui.settings.screens.ThemeScreen
import com.imnotndesh.truehub.ui.theme.AppTheme
import com.imnotndesh.truehub.ui.theme.TrueHubAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // Use activity-level viewModels() delegate so we can access the VM
    // both inside and outside setContent — this is the standard pattern.
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle widget deep-link on cold start
        handleWidgetIntent(intent)

        setContent {
            var currentTheme by rememberSaveable { mutableStateOf(Prefs.loadTheme(this)) }
            TrueHubAppTheme(theme = currentTheme) {
                MainActivityContent(
                    viewModel = viewModel,
                    currentTheme = currentTheme,
                    onThemeChanged = { newTheme ->
                        currentTheme = newTheme
                    }
                )
            }
        }
    }

    // Called when the app is already running and the widget is tapped again.
    // onNewIntent is a plain lifecycle method — NOT @Composable.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // update the stored intent
        handleWidgetIntent(intent)
    }

    private fun handleWidgetIntent(intent: Intent?) {
        if (intent?.action == "com.imnotndesh.truehub.OPEN_APPS") {
            viewModel.requestNavigateTo(Screen.Apps.route)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainActivityContent(
    viewModel: MainViewModel,
    currentTheme: AppTheme,
    onThemeChanged: (AppTheme) -> Unit
) {
    val context = LocalContext.current
    val appState by viewModel.appState.collectAsState()
    val manager by viewModel.manager.collectAsState()
    val navController = rememberNavController()
    val notifPermission = rememberPermissionState(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    LaunchedEffect(Unit) {
        viewModel.initializeApp(context)
    }
    LaunchedEffect(manager) {
        manager?.let {
            viewModel.startPeriodicPing(context)
            viewModel.startPeriodicAppSync(context)
        }
    }
    LaunchedEffect(Unit) {
        if (!notifPermission.status.isGranted) {
            notifPermission.launchPermissionRequest()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (appState) {
            is AppState.Initializing -> LoadingScreen("Initializing...")
            is AppState.CheckingConnection -> LoadingScreen("Connecting to server...")
            is AppState.ValidatingToken -> LoadingScreen("Validating credentials...")
            is AppState.AttemptingAutoLogin -> LoadingScreen("Attempting auto sign-in...")
            is AppState.Ready -> {
                AppNavigation(
                    startRoute = (appState as AppState.Ready).startRoute,
                    navController = navController,
                    viewModel = viewModel,
                    manager = manager,
                    onThemeChanged = onThemeChanged,
                    currentTheme = currentTheme
                )
            }
            is AppState.Error -> {
                LaunchedEffect((appState as AppState.Error).message) {
                    ToastManager.showError((appState as AppState.Error).message)
                }
                AppNavigation(
                    startRoute = (appState as AppState.Error).fallbackRoute,
                    navController = navController,
                    viewModel = viewModel,
                    manager = manager,
                    onThemeChanged = onThemeChanged,
                    currentTheme = currentTheme
                )
            }
            is AppState.NoInternet -> {
                TrueHubAppTheme {
                    NoInternetScreen(
                        message = "No internet connection.",
                        onRetry = {
                            viewModel.initializeApp(context)
                        }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            ModernToastHost()
        }
    }
}

@Composable
private fun AppNavigation(
    currentTheme: AppTheme,
    onThemeChanged: (AppTheme) -> Unit,
    startRoute: String,
    navController: NavHostController,
    viewModel: MainViewModel,
    manager: com.imnotndesh.truehub.data.api.TrueNASApiManager?
) {
    val context = LocalContext.current

    // Consume the pending navigation signal from the widget.
    // When the ViewModel has a route queued (e.g. "apps" from the widget tap),
    // we navigate to Screen.Main first (so the bottom nav is visible), then
    // post a second navigate inside MainScreen to the Apps tab.
    // The simplest correct approach: navigate to Main, and pass the deep route
    // via the ViewModel so MainScreen can pick it up.
    val pendingNav by viewModel.pendingNavigation.collectAsState()
    LaunchedEffect(pendingNav) {
        val route = pendingNav ?: return@LaunchedEffect
        // If we're not already on Main, go there first
        if (navController.currentDestination?.route != Screen.Main.route) {
            navController.navigate(Screen.Main.route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
            }
        }
        // Don't clear yet — MainScreen will consume it to switch its inner tab
    }

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                existingManager = manager,
                navController = navController,
                onManagerInitialized = { newManager ->
                    viewModel.updateManager(newManager)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.AccountSwitcher.route) {
            AccountSwitcherScreen(
                onAccountSelected = { server, account ->
                    (context as? ComponentActivity)?.lifecycleScope?.launch {
                        val loginManager = viewModel.attemptLoginWithProfile(context, server, account)
                        if (loginManager != null) {
                            viewModel.updateManager(loginManager)
                            navController.navigate(Screen.Main.route) {
                                popUpTo(Screen.AccountSwitcher.route) { inclusive = true }
                            }
                        } else {
                            ToastManager.showError("Failed to login with saved account")
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.AccountSwitcher.route) { inclusive = true }
                            }
                        }
                    }
                },
                onAddNewAccount = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(Screen.Main.route) {
            manager?.let { validManager ->
                MainScreen(
                    manager = validManager,
                    rootNavController = navController,
                    viewModel = viewModel
                )
            } ?: run {
                LaunchedEffect(Unit) {
                    ToastManager.showError("Session invalid. Please log in again.")
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
                LoadingScreen("Redirecting to login...")
            }
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                manager = manager,
                onDummyAction = { settingAction ->
                    ToastManager.showInfo("Work in progress for: $settingAction")
                },
                onNavigateToTheme = {
                    navController.navigate(Screen.Theme.route)
                },
                onNavigateToAbout = {
                    navController.navigate(Screen.About.route)
                },
                onNavigateToLicenses = {
                    navController.navigate(Screen.Licenses.route)
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.AccountSwitcher.route) {
                        popUpTo(Screen.Settings.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.About.route) {
            manager?.let { validManager ->
                AboutScreen(
                    manager = validManager,
                    onNavigateBack = { navController.popBackStack() }
                )
            } ?: run {
                LoadingScreen("Redirecting...")
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }

        composable(Screen.Licenses.route) {
            manager?.let { validManager ->
                LicensesScreen(
                    manager = validManager,
                    onNavigateBack = { navController.popBackStack() }
                )
            } ?: run {
                LoadingScreen("Redirecting...")
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }

        composable(Screen.Theme.route) {
            ThemeScreen(
                currentTheme = currentTheme,
                onThemeSelected = { newTheme -> onThemeChanged(newTheme) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}