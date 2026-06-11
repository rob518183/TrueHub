package com.imnotndesh.truehub.ui.widgets

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.TrueNASClient
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.MultiAccountPrefs
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.data.models.Config
import com.imnotndesh.truehub.ui.services.apps.details.upgrade.UpgradeSummaryScreen
import com.imnotndesh.truehub.ui.theme.TrueHubAppTheme
import kotlinx.coroutines.launch

class UpgradeBottomSheetActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appName = intent.getStringExtra("EXTRA_APP_NAME")
        if (appName == null) {
            finish()
            return
        }

        setContent {
            TrueHubAppTheme {
                BottomSheetContent(
                    appName = appName,
                    onDismiss = { finish() }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun BottomSheetContent(appName: String, onDismiss: () -> Unit) {
        val scope = rememberCoroutineScope()

        var manager by remember { mutableStateOf<TrueNASApiManager?>(null) }
        var summary by remember { mutableStateOf<Apps.AppUpgradeSummaryResult?>(null) }
        var currentVersion by remember { mutableStateOf("") }
        var displayName by remember { mutableStateOf(appName) }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            val servers = MultiAccountPrefs.getServers(this@UpgradeBottomSheetActivity)
            if (servers.isNotEmpty()) {
                val activeServer = servers.first()

                val clientConfig = Config.ClientConfig(
                    serverUrl = activeServer.serverUrl,
                    insecure = activeServer.insecure
                )
                val client = TrueNASClient(clientConfig)
                client.connect()

                val m = TrueNASApiManager(client, this@UpgradeBottomSheetActivity)
                manager = m

                val appsResult = m.apps.getInstalledAppsWithResult()
                if (appsResult is ApiResult.Success) {
                    val app = appsResult.data.find { it.name == appName }
                    if (app != null) {
                        currentVersion = app.version!!
                        displayName = app.name
                    }
                }

                val summaryResult = m.apps.getUpgradeSummaryWithResult(appName)
                if (summaryResult is ApiResult.Success) {
                    summary = summaryResult.data
                }
            }
            isLoading = false
        }

        if (manager != null && summary != null) {
            ModalBottomSheet(
                onDismissRequest = onDismiss,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                UpgradeSummaryScreen(
                    appName = appName,
                    currentVersion = currentVersion,
                    currentHumanVersion = null,
                    summary = summary!!,
                    manager = manager!!,
                    onConfirmUpgrade = { selectedVersion, backup ->
                        scope.launch {
                            manager!!.apps.upgradeAppWithResult(
                                appName = appName,
                                version = selectedVersion,
                                backup = backup
                            )
                            onDismiss()
                        }
                    },
                    onNavigateBack = onDismiss
                )
            }
        } else if (isLoading) {
            ModalBottomSheet(
                onDismissRequest = onDismiss,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {
            // If data fetching failed, dismiss automatically
            LaunchedEffect(Unit) { onDismiss() }
        }
    }
}