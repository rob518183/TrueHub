package com.imnotndesh.truehub.ui.widgets

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.imnotndesh.truehub.MainActivity
import com.imnotndesh.truehub.data.helpers.WidgetDataStore
import com.imnotndesh.truehub.data.models.Apps

private val SMALL_WIDTH  = 80.dp
private val MEDIUM_WIDTH = 180.dp

private val SMALL_HEIGHT  = 80.dp
private val MEDIUM_HEIGHT = 180.dp

class AppsUpdateWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = AppsUpdateWidget()
}

class AppsUpdateWidget : GlanceAppWidget() {

    // Respond to every unique size the launcher reports
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val apps by WidgetDataStore.upgradableAppsFlow(context)
                .collectAsState(initial = emptyList())

            GlanceTheme {
                val size = LocalSize.current
                val isSmall  = size.width  < MEDIUM_WIDTH  || size.height < MEDIUM_HEIGHT
                val isLarge  = size.width  > MEDIUM_WIDTH  && size.height > MEDIUM_HEIGHT + 60.dp

                when {
                    isSmall  -> SmallWidget(upgradableApps = apps, context = context)
                    isLarge  -> LargeWidget(upgradableApps = apps, context = context)
                    else     -> MediumWidget(upgradableApps = apps, context = context)
                }
            }
        }
    }
}

private fun openAppsIntent(context: Context): Intent =
    Intent(context, MainActivity::class.java).apply {
        action = "com.imnotndesh.truehub.OPEN_APPS"
        flags  = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

/** Opens the upgrade bottom-sheet for a specific app. */
private fun upgradeIntent(context: Context, appName: String): Intent =
    Intent(context, UpgradeBottomSheetActivity::class.java).apply {
        putExtra("EXTRA_APP_NAME", appName)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }


@Composable
private fun SmallWidget(upgradableApps: List<Apps.AppQueryResponse>, context: Context) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(20.dp)
            .clickable(actionStartActivity(openAppsIntent(context))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment   = Alignment.CenterVertically,
            modifier = GlanceModifier.padding(8.dp)
        ) {
            Text(
                text  = if (upgradableApps.isEmpty()) "✓" else upgradableApps.size.toString(),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize   = 28.sp,
                    color      = if (upgradableApps.isEmpty())
                        GlanceTheme.colors.onSurfaceVariant
                    else
                        GlanceTheme.colors.primary
                )
            )
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text  = if (upgradableApps.isEmpty()) "No updates" else "Updates",
                style = TextStyle(
                    fontSize = 11.sp,
                    color    = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }
    }
}


@Composable
private fun MediumWidget(upgradableApps: List<Apps.AppQueryResponse>, context: Context) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(20.dp)
            .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 12.dp)
    ) {
        // Header
        Row(
            modifier          = GlanceModifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text  = "App Updates",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize   = 14.sp,
                    color      = GlanceTheme.colors.primary
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            if (upgradableApps.isNotEmpty()) {
                BadgePill(count = upgradableApps.size)
            }
        }

        // Content
        if (upgradableApps.isEmpty()) {
            Box(
                modifier          = GlanceModifier.defaultWeight().fillMaxWidth(),
                contentAlignment  = Alignment.Center
            ) {
                Text(
                    text  = "No updates available",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color    = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }
        } else {
            // Show up to 3 rows in 2×2
            upgradableApps.take(3).forEach { app ->
                CompactAppRow(app = app, context = context)
            }
            if (upgradableApps.size > 3) {
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    text  = "+${upgradableApps.size - 3} more",
                    style = TextStyle(
                        fontSize = 11.sp,
                        color    = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }
            Spacer(GlanceModifier.defaultWeight())
        }


    }
}


@Composable
private fun LargeWidget(upgradableApps: List<Apps.AppQueryResponse>, context: Context) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(20.dp)
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
    ) {
        Row(
            modifier          = GlanceModifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text  = "TrueNAS Updates",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp,
                        color      = GlanceTheme.colors.primary
                    )
                )
                Text(
                    text  = if (upgradableApps.isEmpty()) "No updates available"
                    else "${upgradableApps.size} app(s) need attention",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color    = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }
            if (upgradableApps.isNotEmpty()) {
                BadgePill(count = upgradableApps.size)
            }
        }

        Spacer(GlanceModifier.height(8.dp))

        if (upgradableApps.isEmpty()) {
            Box(
                modifier         = GlanceModifier.defaultWeight().fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = "No updates available",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color    = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }
        } else {
            Column(modifier = GlanceModifier.defaultWeight()) {
                upgradableApps.take(6).forEach { app ->
                    LargeAppRow(app = app, context = context)
                    Spacer(GlanceModifier.height(6.dp))
                }
                if (upgradableApps.size > 6) {
                    Text(
                        text  = "+${upgradableApps.size - 6} more updates available",
                        style = TextStyle(
                            fontSize = 11.sp,
                            color    = GlanceTheme.colors.onSurfaceVariant
                        ),
                        modifier = GlanceModifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

// ── Shared sub-composables ────────────────────────────────────────────────────

/** A small rounded pill showing the pending count. */
@Composable
private fun BadgePill(count: Int) {
    Box(
        modifier         = GlanceModifier
            .background(GlanceTheme.colors.primaryContainer)
            .cornerRadius(100.dp)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = count.toString(),
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize   = 12.sp,
                color      = GlanceTheme.colors.onPrimaryContainer
            )
        )
    }
}

/** Compact single-line row used in 2×2. Tapping opens the upgrade sheet. */
@Composable
private fun CompactAppRow(app: Apps.AppQueryResponse, context: Context) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable(actionStartActivity(upgradeIntent(context, app.name))),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text     = app.metadata?.title ?: app.name,
            style    = TextStyle(
                fontSize   = 13.sp,
                fontWeight = FontWeight.Medium,
                color      = GlanceTheme.colors.onSurface
            ),
            modifier = GlanceModifier.defaultWeight(),
            maxLines = 1
        )
        Spacer(GlanceModifier.width(6.dp))
        Box(
            modifier         = GlanceModifier
                .background(GlanceTheme.colors.primaryContainer)
                .cornerRadius(8.dp)
                .padding(horizontal = 8.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = app.latestVersion ?: "↑",
                style = TextStyle(
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color      = GlanceTheme.colors.onPrimaryContainer
                ),
                maxLines = 1
            )
        }
    }
}

/** Larger two-line row used in the large widget. Tapping opens the upgrade sheet. */
@Composable
private fun LargeAppRow(app: Apps.AppQueryResponse, context: Context) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(GlanceTheme.colors.surface)
            .cornerRadius(12.dp)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .clickable(actionStartActivity(upgradeIntent(context, app.name))),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text     = app.metadata?.title ?: app.name,
                style    = TextStyle(
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = GlanceTheme.colors.onSurface
                ),
                maxLines = 1
            )
            if (app.version != null) {
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text  = "v${app.version} → ${app.latestVersion ?: "latest"}",
                    style = TextStyle(
                        fontSize = 11.sp,
                        color    = GlanceTheme.colors.onSurfaceVariant
                    ),
                    maxLines = 1
                )
            }
        }
        Spacer(GlanceModifier.width(8.dp))
        Box(
            modifier         = GlanceModifier
                .background(GlanceTheme.colors.primaryContainer)
                .cornerRadius(8.dp)
                .padding(horizontal = 10.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = "Update",
                style = TextStyle(
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color      = GlanceTheme.colors.onPrimaryContainer
                )
            )
        }
    }
}