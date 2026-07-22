package com.imnotndesh.truehub.ui

sealed class Screen(val route:String, val title:String) {
    object Home : Screen("home", "Home")
    object Apps : Screen("apps","Apps")
    object ServicesScreen : Screen("services", "TrueNAS Services")
    object ServicesDetailScreen: Screen("service_detail", "Details about TrueNAS services")
    object Containers : Screen ("containers", "Containers")
    object Vms : Screen("vms","VMs")
    object Login : Screen("login","Login")
    object Main: Screen("main","Main")
    object Performance : Screen("performance", "Performance")
    object Settings : Screen("settings","Settings")
    object Licenses : Screen("licenses","Licenses")
    object ShareInfo : Screen ("share_info","Exposed shares details")
    object About : Screen("about","About")
    object Theme : Screen("theme","Theme")
    object AccountSwitcher : Screen("account_switcher","account_switcher")
    object PoolDetails : Screen("pool_details", "Pool Details")
    object Files : Screen("file_explorer","file_explorer")
    object Marketplace : Screen("marketplace", "Marketplace")
    object MarketplaceAppDetails : Screen("marketplace_app_details", "Marketplace App Details")
    object ContainerInfo : Screen("container_info", "Container Info")
    object VmDetails : Screen("vm_details", "VM Details")
    object ChangePassword : Screen("change_password", "Change Password")
    object DiskInfo : Screen("disk_info", "Disk information")
    object AppConfigScreen : Screen("app_config", "Application config")
    object InstanceConfigScreen : Screen("instance_config", "TrueNAS Instance Configuration")
    object AppDetailsScreen : Screen("app_details", "Application details page")
    object SystemUpdateScreen : Screen("system_update", "TrueNAS Version Update")
    object MarketplaceCategory : Screen("marketplace?category={category}", "Marketplace Category") {
        fun createRoute(category: String): String {
            return "marketplace?category=$category"
        }
    }

    object AppUpgrade : Screen("app_upgrade/{appName}", "App Upgrade") {
        fun createRoute(appName: String): String {
            return "app_upgrade/$appName"
        }
    }

    object CatalogInstall : Screen("catalog_install/{appName}/{train}", "Catalog Install") {
        fun createRoute(appName: String, train: String): String {
            return "catalog_install/$appName/$train"
        }
    }

    object DatasetExplorer : Screen("${Files.route}/{poolName}", "Dataset Explorer") {
        fun createRoute(poolName: String): String {
            return "${Files.route}/$poolName"
        }
    }
    object RollbackVersion : Screen("rollback/{appName}", "Rollback App") {
        fun createRoute(appName: String): String {
            return "rollback/$appName"
        }
    }
    object AlertServicesList : Screen("alert_services_list","alert_services_list")
    object AlertServiceDetail : Screen("alert_service_detail/{serviceId}","alert_service_detail/{serviceId}") {
        fun createRoute(serviceId: Int) = "alert_service_detail/$serviceId"
    }
    object AlertServiceCreate : Screen("alert_service_create","alert_service_create")
    object AlertClassesConfig : Screen("alert_classes_config","\"alert_classes_config\"")

}