package com.imnotndesh.truehub.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.homepage.HomeScreen
import com.imnotndesh.truehub.ui.homepage.dataset.DatasetExplorerScreen
import com.imnotndesh.truehub.ui.homepage.details.DiskInfoScreen
import com.imnotndesh.truehub.ui.homepage.details.PerformanceScreen
import com.imnotndesh.truehub.ui.homepage.details.ShareInfoScreen
import com.imnotndesh.truehub.ui.homepage.pools.PoolDataHolder
import com.imnotndesh.truehub.ui.homepage.pools.PoolDetailsScreen
import com.imnotndesh.truehub.ui.services.apps.AppsScreen
import com.imnotndesh.truehub.ui.services.apps.AppsScreenViewModel
import com.imnotndesh.truehub.ui.services.apps.details.appdetails.AppConfigPageValues
import com.imnotndesh.truehub.ui.services.apps.details.appdetails.AppConfigScreen
import com.imnotndesh.truehub.ui.services.apps.details.appdetails.AppDataHolder
import com.imnotndesh.truehub.ui.services.apps.details.appdetails.AppInfoScreen
import com.imnotndesh.truehub.ui.services.apps.details.marketplace.MarketplaceAppDetailsScreen
import com.imnotndesh.truehub.ui.services.apps.details.marketplace.MarketplaceAppInstallScreen
import com.imnotndesh.truehub.ui.services.apps.details.marketplace.MarketplaceScreen
import com.imnotndesh.truehub.ui.services.apps.details.rollback.RollbackVersionScreen
import com.imnotndesh.truehub.ui.services.apps.details.upgrade.UpgradeSummaryScreen
import com.imnotndesh.truehub.ui.services.containers.ContainersScreen
import com.imnotndesh.truehub.ui.services.containers.details.ContainerDataHolder
import com.imnotndesh.truehub.ui.services.containers.details.ContainerInfoScreen
import com.imnotndesh.truehub.ui.services.vm.VmsScreen
import com.imnotndesh.truehub.ui.services.vm.details.VmDataHolder
import com.imnotndesh.truehub.ui.services.vm.details.VmInfoScreen


private data class NavItem(
    val screen: Screen,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun MainScreen(manager: TrueNASApiManager, rootNavController: NavController) {
    val navController = rememberNavController()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val routesWithoutBottomBar = remember {
        setOf(
            Screen.AppConfigScreen.route,
            Screen.Settings.route,
            Screen.AppUpgrade.route,
            Screen.RollbackVersion.route,
            Screen.AppDetailsScreen.route,
            Screen.Marketplace.route,
            Screen.MarketplaceAppDetails.route,
            Screen.MarketplaceCategory.route
        )
    }

    val navItems = remember {
        listOf(
            NavItem(Screen.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home),
            NavItem(Screen.Apps, "Apps", Icons.Filled.Apps, Icons.Outlined.Apps),
            NavItem(Screen.Containers, "Containers", Icons.Filled.Inventory, Icons.Outlined.Inventory2),
            NavItem(Screen.Vms, "VMs", Icons.Filled.Computer, Icons.Outlined.Computer)
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    if (isLandscape) {
        Row {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxHeight(),
                header = {}
            ) {
                navItems.forEach { item ->
                    val selected = currentRoute == item.screen.route
                    NavigationRailItem(
                        selected = selected,
                        onClick = { onNavClick(navController, item.screen.route) },
                        label = { Text(item.title, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                        icon = {
                            Crossfade(targetState = selected, label = "iconFade") { isSelected ->
                                Icon(if (isSelected) item.selectedIcon else item.unselectedIcon, item.title)
                            }
                        },
                        colors = NavigationRailItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
            TrueHubNavGraph(navController = navController, manager = manager, rootNavController = rootNavController, modifier = Modifier.weight(1f))
        }
    } else {
        Scaffold(
            bottomBar = {if (currentRoute !in routesWithoutBottomBar){
                run {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        navItems.forEach { item ->
                            val selected = currentRoute == item.screen.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { onNavClick(navController, item.screen.route) },
                                label = {
                                    Text(
                                        item.title,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                icon = {
                                    Crossfade(
                                        targetState = selected,
                                        label = "iconFade"
                                    ) { isSelected ->
                                        Icon(
                                            if (isSelected) item.selectedIcon else item.unselectedIcon,
                                            item.title
                                        )
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
            }
        ) { innerPadding ->
            TrueHubNavGraph(navController = navController, manager = manager, rootNavController = rootNavController, modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
private fun TrueHubNavGraph(
    navController: NavHostController,
    manager: TrueNASApiManager,
    rootNavController: NavController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
        }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                manager,
                onNavigateToSettings = { rootNavController.navigate(Screen.Settings.route) },
                onPoolClick = { pool: System.Pool ->
                    PoolDataHolder.currentPool = pool
                    navController.navigate(Screen.PoolDetails.route)
                },
                onNavigateToShareInfo = { shareType ->
                    AppDataHolder.selectedShareType = shareType
                    navController.navigate("share_info")
                },
                onDisksClick = {
                    navController.navigate(Screen.DiskInfo.route)
                },
                onNavigateToPerformance = {metricType ->
                    AppDataHolder.initialMetricType = metricType
                    navController.navigate(Screen.Performance.route)
                }
            )
        }
        composable(Screen.DiskInfo.route){
            val disks = AppDataHolder.disks
            DiskInfoScreen(
                disks,
                manager
            ) {
                navController.popBackStack()
            }
        }
        composable(Screen.AppConfigScreen.route){
            val currAppValues = AppDataHolder.selectedAppValues
            AppConfigScreen(
                manager,
                currAppValues,
                {
                    navController.popBackStack()
                }
            )
        }

        composable("share_info") {
            val shareType = AppDataHolder.selectedShareType
            if (shareType != null) {
                ShareInfoScreen(
                    shareType = shareType,
                    manager = manager,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        composable(Screen.Performance.route) {
            PerformanceScreen(
                cpuData = AppDataHolder.cpuData,
                memoryData = AppDataHolder.memoryData,
                temperatureData = AppDataHolder.temperatureData,
                initialMetricType = AppDataHolder.initialMetricType,
                isLoading = false,
                manager = manager,
                onNavigateBack = { navController.popBackStack() },
                onRefresh = {}
            )
        }

        composable(Screen.Apps.route) {
            AppsScreen(
                manager = manager,
                onNavigateToAppInfo = { app ->
                    AppDataHolder.selectedApp = app
                    navController.navigate(Screen.AppDetailsScreen.route)
                },
                onNavigateToUpgrade = { appName -> navController.navigate(Screen.AppUpgrade.createRoute(appName)) },
                onNavigateToRollback = { navController.navigate(Screen.RollbackVersion.createRoute(it)) },
                onNavigateToMarketplace = { navController.navigate(Screen.Marketplace.route) }
            )
        }

        composable(Screen.PoolDetails.route) {
            PoolDetailsScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToFiles = { poolName: String -> navController.navigate(Screen.DatasetExplorer.createRoute(poolName)) }
            )
        }
        composable(
            route = Screen.RollbackVersion.route,
            arguments = listOf(navArgument("appName") { type = NavType.StringType })
        ) { backStackEntry ->
            val appName = backStackEntry.arguments?.getString("appName") ?: ""
            val context = LocalContext.current
            val appsViewModel: AppsScreenViewModel = viewModel(factory = AppsScreenViewModel.AppsScreenViewModelFactory(manager))
            val uiState by appsViewModel.uiState.collectAsState()

            LaunchedEffect(appName) {
                appsViewModel.loadRollbackVersions(appName)
            }

            RollbackVersionScreen(
                appName = appName,
                versions = uiState.rollbackVersions,
                isLoadingVersions = uiState.isLoadingRollbackVersions,
                fetchError = uiState.error,
                manager = manager,
                onConfirmRollback = { targetVersion, rollbackSnapshot ->
                    appsViewModel.rollbackApp(context, appName, targetVersion, rollbackSnapshot)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AppDetailsScreen.route) {
            val app = AppDataHolder.selectedApp
            val appsViewModel: AppsScreenViewModel = viewModel(factory = AppsScreenViewModel.AppsScreenViewModelFactory(manager))
            LaunchedEffect(Unit) {
                if (appsViewModel.uiState.value.marketplaceApps.isEmpty()) appsViewModel.loadMarketplaceApps()
            }
            AppInfoScreen(
                app = app!!,
                manager = manager,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMarketplaceCategory = { categoryName -> navController.navigate(Screen.MarketplaceCategory.createRoute(categoryName)) },
                onNavigateToMarketplaceAppDetails = { appName ->
                    val matchedAvailableItem = appsViewModel.uiState.value.marketplaceApps.find { it.name == appName }
                    if (matchedAvailableItem != null) {
                        AppDataHolder.selectedMarketplaceApp = matchedAvailableItem
                        navController.navigate("marketplace_app_details")
                    } else {
                        navController.navigate("marketplace?category=")
                    }
                },
                onDeleteSuccess = { navController.navigate(Screen.Apps.route) },
                onEditClick = {appName , appTrain->
                    AppDataHolder.selectedAppValues = AppConfigPageValues(
                        appName,
                        appTrain
                    )
                    navController.navigate(Screen.AppConfigScreen.route)
                }
            )
        }

        composable(
            route = Screen.MarketplaceCategory.route,
            arguments = listOf(navArgument("category") { type = NavType.StringType; defaultValue = ""; nullable = true })
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category")?.takeIf { it.isNotBlank() }
            MarketplaceScreen(manager = manager, initialCategory = category, onNavigateBack = { navController.popBackStack() }, onMarketplaceApplicationClicked = { app -> AppDataHolder.selectedMarketplaceApp = app; navController.navigate("marketplace_app_details") })
        }

        composable(Screen.Marketplace.route) {
            MarketplaceScreen(manager = manager, onNavigateBack = { navController.popBackStack() }, onMarketplaceApplicationClicked = { app -> AppDataHolder.selectedMarketplaceApp = app; navController.navigate("marketplace_app_details") })
        }

        composable(
            route = Screen.AppUpgrade.route,
            arguments = listOf(navArgument("appName")
            { type = NavType.StringType })
        ) { backStackEntry ->
            val appName = backStackEntry.arguments?.getString("appName") ?: ""
            val context = LocalContext.current
            val viewModel: AppsScreenViewModel = viewModel(factory = AppsScreenViewModel.AppsScreenViewModelFactory(manager))
            val uiState by viewModel.uiState.collectAsState()
            LaunchedEffect(appName) { viewModel.clearUpgradeSummary(); viewModel.loadUpgradeSummary(appName) }
            val summary = uiState.upgradeSummaryResult
            val isLoading = (uiState.isLoadingUpgradeSummaryForApp == appName) && (summary == null)
            if (isLoading) {
                LoadingScreen("Checking upgrades...")
            } else if (summary != null) {
                UpgradeSummaryScreen(appName = appName, summary = summary, manager = manager, onConfirmUpgrade = { viewModel.upgradeApp(appName, context) }, onNavigateBack = { navController.popBackStack() })
            } else if (uiState.error != null) {
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }

        composable(Screen.MarketplaceAppDetails.route) {
            val appsViewModel: AppsScreenViewModel = viewModel(factory = AppsScreenViewModel.AppsScreenViewModelFactory(manager))
            val app = AppDataHolder.selectedMarketplaceApp
            if (app != null) {
                MarketplaceAppDetailsScreen(app = app, onNavigateBack = { navController.popBackStack() }, manager = manager, onInstallClick = { appName, train -> appsViewModel.loadCatalogAppDetails(appName, train); navController.navigate(Screen.CatalogInstall.createRoute(appName,train)) })
            }
        }

        composable(route = Screen.CatalogInstall.route, arguments = listOf(navArgument("appName") { type = NavType.StringType }, navArgument("train") { type = NavType.StringType })) { backStackEntry ->
            val appsViewModel: AppsScreenViewModel = viewModel(factory = AppsScreenViewModel.AppsScreenViewModelFactory(manager))
            val appName = backStackEntry.arguments?.getString("appName") ?: ""
            val train = backStackEntry.arguments?.getString("train") ?: ""
            MarketplaceAppInstallScreen(appName = appName, train = train, viewModel = appsViewModel, manager = manager, onBack = { navController.popBackStack() }, onInstallSuccess = { navController.popBackStack() })
        }

        composable(Screen.Containers.route) {
            ContainersScreen(manager = manager, onNavigateToContainerInfo = { container -> ContainerDataHolder.selectedContainer = container; navController.navigate("container_info") })
        }

        composable(Screen.ContainerInfo.route) {
            val container = ContainerDataHolder.selectedContainer
            ContainerInfoScreen(manager = manager, container = container!!, onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Vms.route) {
            VmsScreen(manager = manager, onNavigateToVmInfo = { vmInfo -> VmDataHolder.selectedVm = vmInfo; navController.navigate("vm_details") })
        }

        composable(Screen.VmDetails.route) {
            val vm = VmDataHolder.selectedVm
            VmInfoScreen(vm = vm!!, manager = manager, onNavigateBack = { navController.popBackStack() })
        }

        composable(route = Screen.DatasetExplorer.route, arguments = listOf(navArgument("poolName") { type = NavType.StringType })) { backStackEntry ->
            val poolName = backStackEntry.arguments?.getString("poolName") ?: ""
            DatasetExplorerScreen(manager = manager, onNavigateBack = { navController.popBackStack() }, poolName = poolName)
        }
    }
}

private fun onNavClick(navController: NavController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}