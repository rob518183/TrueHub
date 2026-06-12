package com.imnotndesh.truehub.ui.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.imnotndesh.truehub.MainActivity
import com.imnotndesh.truehub.data.helpers.WidgetDataStore
import com.imnotndesh.truehub.data.models.Apps

class AppsUpdateWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = AppsUpdateWidget()
}

class AppsUpdateWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val apps by WidgetDataStore.upgradableAppsFlow(context)
                .collectAsState(initial = emptyList())

            AppsUpdateWidgetContent(
                context = context,
                upgradableApps = apps
            )
        }
    }
}

@Composable
private fun AppsUpdateWidgetContent(
    context: Context,
    upgradableApps: List<Apps.AppQueryResponse>
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .padding(12.dp)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "App Updates",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = GlanceTheme.colors.primary
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            Text(
                text = "${upgradableApps.size} available",
                style = TextStyle(
                    fontSize = 11.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }

        Spacer(GlanceModifier.height(8.dp))

        when {
            upgradableApps.isEmpty() -> {
                Text(
                    text = "All apps up to date ✓",
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    ),
                    modifier = GlanceModifier.fillMaxWidth()
                )
            }
            else -> {
                upgradableApps.take(4).forEach { app ->
                    AppUpdateRow(app = app)
                    Spacer(GlanceModifier.height(4.dp))
                }
                if (upgradableApps.size > 4) {
                    Text(
                        text = "+ ${upgradableApps.size - 4} more",
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                }
            }
        }

        Spacer(GlanceModifier.defaultWeight())
        Button(
            text = "Open Updates",
            onClick = actionStartActivity<MainActivity>()
        )
    }
}

@Composable
private fun AppUpdateRow(app: Apps.AppQueryResponse) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = app.metadata?.title ?: app.name,
            style = TextStyle(fontSize = 12.sp),
            modifier = GlanceModifier.defaultWeight(),
            maxLines = 1
        )
        Text(
            text = app.latestVersion ?: "→",
            style = TextStyle(
                fontSize = 11.sp,
                color = GlanceTheme.colors.primary
            )
        )
    }
}