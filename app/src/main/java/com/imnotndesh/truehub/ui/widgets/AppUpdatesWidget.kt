package com.imnotndesh.truehub.ui.widgets

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.TrueNASClient
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.MultiAccountPrefs
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.data.models.Config

class AppUpdatesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AppUpdatesWidget()
}

class AppUpdatesWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val servers = MultiAccountPrefs.getServers(context)
        val activeServer = servers.firstOrNull()

        var upgradableApps = emptyList<Apps.AppQueryResponse>()

        if (activeServer != null) {
            try {
                val client =
                    TrueNASClient(Config.ClientConfig(activeServer.serverUrl, activeServer.insecure))
                client.connect()
                val manager = TrueNASApiManager(client, context)
                val result = manager.apps.getInstalledAppsWithResult()
                if (result is ApiResult.Success) {
                    upgradableApps = result.data.filter { it.upgrade_available }
                }
                client.disconnect()
            } catch (e: Exception) {
                // Log error
            }
        }
        /* TODO:
            1. Introduce cache to apps page
            2. Force user to open the app in order for the widget to work
            3. Restructure how the worker works so it also pre-caches the app information that we need. not just showing alerts only
         */


        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .cornerRadius(24.dp)
                        .background(GlanceTheme.colors.surfaceVariant)
                ) {
                    if (upgradableApps.isEmpty()) {
                        Text("No updates available")
                    } else {
                        Text("Updates Available (${upgradableApps.size})")
                        upgradableApps.forEach { app ->
                            AppUpdateRow(app.metadata?.title ?: app.name)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun AppUpdateRow(appName: String) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(android.R.drawable.ic_menu_crop),
                contentDescription = null,
                modifier = GlanceModifier.size(32.dp)
            )

            Spacer(modifier = GlanceModifier.width(12.dp))

            Text(
                text = appName,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            val intent = Intent(LocalContext.current, UpgradeBottomSheetActivity::class.java).apply {
                putExtra("EXTRA_APP_NAME", appName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            Text(
                text = "Upgrade",
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                modifier = GlanceModifier
                    .background(GlanceTheme.colors.primaryContainer)
                    .cornerRadius(16.dp)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable(actionStartActivity(intent))
            )
        }
    }
}