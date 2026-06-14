package com.imnotndesh.truehub.ui.widgets.pools

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
import androidx.glance.appwidget.LinearProgressIndicator
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
import androidx.glance.layout.wrapContentHeight
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.imnotndesh.truehub.MainActivity
import com.imnotndesh.truehub.data.helpers.WidgetDataStore
import com.imnotndesh.truehub.data.models.System
import java.text.DecimalFormat

// ── Size breakpoints (mirrors AppsUpdateWidget pattern) ──────────────────────
private val MEDIUM_WIDTH  = 180.dp
private val MEDIUM_HEIGHT = 180.dp

class PoolsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = PoolsWidget()
}

class PoolsWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val pools by WidgetDataStore.poolsFlow(context)
                .collectAsState(initial = emptyList())

            GlanceTheme {
                val size    = LocalSize.current
                val isSmall = size.width  < MEDIUM_WIDTH  || size.height < MEDIUM_HEIGHT
                val isLarge = size.width  > MEDIUM_WIDTH  && size.height > MEDIUM_HEIGHT + 60.dp

                when {
                    isSmall -> PoolsSmallWidget(pools = pools, context = context)
                    isLarge -> PoolsLargeWidget(pools = pools, context = context)
                    else    -> PoolsMediumWidget(pools = pools, context = context)
                }
            }
        }
    }
}

// ── Intent helpers ────────────────────────────────────────────────────────────

private fun openPoolsIntent(context: Context): Intent =
    Intent(context, MainActivity::class.java).apply {
        // Reuse the existing deep-link convention from the app
        action = "com.imnotndesh.truehub.OPEN_HOME"
        flags  = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

// ── Byte formatter (mirrors Homepage.kt StorageCard) ─────────────────────────

private fun formatBytes(bytes: Long): String {
    val units     = arrayOf("B", "KB", "MB", "GB", "TB")
    var size      = bytes.toDouble()
    var unitIndex = 0
    while (size >= 1024 && unitIndex < units.size - 1) { size /= 1024.0; unitIndex++ }
    return "${DecimalFormat("#.#").format(size)} ${units[unitIndex]}"
}

// ── Small widget ──────────────────────────────────────────────────────────────

@Composable
private fun PoolsSmallWidget(pools: List<System.Pool>, context: Context) {
    val unhealthy = pools.count { !it.healthy }
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(20.dp)
            .clickable(actionStartActivity(openPoolsIntent(context))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment   = Alignment.CenterVertically,
            modifier            = GlanceModifier.padding(8.dp)
        ) {
            Text(
                text  = if (pools.isEmpty()) "–" else pools.size.toString(),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize   = 28.sp,
                    color      = if (unhealthy > 0)
                        GlanceTheme.colors.error
                    else
                        GlanceTheme.colors.primary
                )
            )
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text  = when {
                    pools.isEmpty() -> "No pools"
                    unhealthy > 0   -> "$unhealthy issue(s)"
                    else            -> "All healthy"
                },
                style = TextStyle(
                    fontSize = 11.sp,
                    color    = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }
    }
}

// ── Medium widget ─────────────────────────────────────────────────────────────

@Composable
private fun PoolsMediumWidget(pools: List<System.Pool>, context: Context) {
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
                text     = "Storage Pools",
                style    = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize   = 14.sp,
                    color      = GlanceTheme.colors.primary
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            PoolCountPill(pools = pools)
        }

        if (pools.isEmpty()) {
            Box(
                modifier         = GlanceModifier.defaultWeight().fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = "No pools configured",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color    = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }
        } else {
            pools.take(2).forEach { pool ->
                CompactPoolRow(pool = pool, context = context)
            }
            if (pools.size > 2) {
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    text  = "+${pools.size - 2} more",
                    style = TextStyle(
                        fontSize = 11.sp,
                        color    = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }
            Spacer(GlanceModifier.defaultWeight())
        }

        PoolsFooterButton(label = "Open Dashboard", context = context)
    }
}

// ── Large widget ──────────────────────────────────────────────────────────────

@Composable
private fun PoolsLargeWidget(pools: List<System.Pool>, context: Context) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(20.dp)
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
    ) {
        // Header — mirrors Homepage StorageCard header layout exactly
        Row(
            modifier          = GlanceModifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text  = "TrueNAS Pools",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp,
                        color      = GlanceTheme.colors.primary
                    )
                )
                val unhealthy = pools.count { !it.healthy }
                Text(
                    text  = when {
                        pools.isEmpty() -> "No pools configured"
                        unhealthy > 0   -> "$unhealthy pool(s) need attention"
                        else            -> "All pools healthy"
                    },
                    style = TextStyle(
                        fontSize = 12.sp,
                        color    = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }
            PoolCountPill(pools = pools)
        }

        // Divider (mirrors the LargeWidget in AppsUpdateWidget)
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(1.dp)
                .background(GlanceTheme.colors.onSurfaceVariant)
        ) {}
        Spacer(GlanceModifier.height(8.dp))

        if (pools.isEmpty()) {
            Box(
                modifier         = GlanceModifier.defaultWeight().fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = "No storage pools are currently configured.",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color    = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }
        } else {
            Column(modifier = GlanceModifier.defaultWeight()) {
                pools.take(4).forEach { pool ->
                    LargePoolRow(pool = pool, context = context)
                    Spacer(GlanceModifier.height(8.dp))
                }
                if (pools.size > 4) {
                    Text(
                        text  = "+${pools.size - 4} more pools",
                        style = TextStyle(
                            fontSize = 11.sp,
                            color    = GlanceTheme.colors.onSurfaceVariant
                        ),
                        modifier = GlanceModifier.padding(top = 4.dp)
                    )
                }
            }
        }

        PoolsFooterButton(label = "Open Dashboard", context = context)
    }
}

// ── Shared sub-composables ────────────────────────────────────────────────────

/**
 * Pill badge showing pool count — healthy pools green, any unhealthy turns it error-tinted.
 * Mirrors [com.imnotndesh.truehub.ui.widgets.BadgePill] in AppsUpdateWidget but with health-aware color.
 */
@Composable
private fun PoolCountPill(pools: List<System.Pool>) {
    if (pools.isEmpty()) return
    val anyUnhealthy = pools.any { !it.healthy }
    Box(
        modifier         = GlanceModifier
            .background(
                if (anyUnhealthy) GlanceTheme.colors.errorContainer
                else GlanceTheme.colors.primaryContainer
            )
            .cornerRadius(100.dp)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = pools.size.toString(),
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize   = 12.sp,
                color      = if (anyUnhealthy) GlanceTheme.colors.onErrorContainer
                else GlanceTheme.colors.onPrimaryContainer
            )
        )
    }
}

/**
 * Compact single-line pool row for the medium widget.
 * Shows pool name + status badge, matching [com.imnotndesh.truehub.ui.widgets.CompactAppRow] style.
 */
@Composable
private fun CompactPoolRow(pool: System.Pool, context: Context) {
    val isHealthy    = pool.healthy
    val hasWarning   = pool.warning
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable(actionStartActivity(openPoolsIntent(context))),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text     = pool.name,
            style    = TextStyle(
                fontSize   = 13.sp,
                fontWeight = FontWeight.Medium,
                color      = GlanceTheme.colors.onSurface
            ),
            modifier = GlanceModifier.defaultWeight(),
            maxLines = 1
        )
        Spacer(GlanceModifier.width(6.dp))
        // Status pill — mirrors Homepage StorageCard status Surface
        val statusText = when {
            !isHealthy -> "Error"
            hasWarning -> "Warning"
            else       -> "OK"
        }
        Box(
            modifier         = GlanceModifier
                .background(
                    when {
                        !isHealthy -> GlanceTheme.colors.errorContainer
                        hasWarning -> GlanceTheme.colors.tertiaryContainer
                        else       -> GlanceTheme.colors.primaryContainer
                    }
                )
                .cornerRadius(8.dp)
                .padding(horizontal = 8.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = statusText,
                style = TextStyle(
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color      = when {
                        !isHealthy -> GlanceTheme.colors.onErrorContainer
                        hasWarning -> GlanceTheme.colors.onTertiaryContainer
                        else       -> GlanceTheme.colors.onPrimaryContainer
                    }
                ),
                maxLines = 1
            )
        }
    }
}

/**
 * Full pool card for the large widget.
 * Mirrors [StorageCard] from Homepage.kt — pool name, health status badge,
 * used/free labels and a LinearProgressIndicator styled the same way.
 */
@Composable
private fun LargePoolRow(pool: System.Pool, context: Context) {
    val isHealthy  = pool.healthy
    val hasWarning = pool.warning

    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(GlanceTheme.colors.surface)
            .cornerRadius(16.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .clickable(actionStartActivity(openPoolsIntent(context)))
    ) {
        // Pool name + status row
        Row(
            modifier          = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                // "Storage Pool" label — identical to Homepage StorageCard
                Text(
                    text  = "Storage Pool",
                    style = TextStyle(
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color      = GlanceTheme.colors.primary
                    )
                )
                Text(
                    text     = pool.name,
                    style    = TextStyle(
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color      = GlanceTheme.colors.onSurface
                    ),
                    maxLines = 1
                )
            }
            Spacer(GlanceModifier.width(8.dp))
            val statusText = when {
                !isHealthy -> "Error"
                hasWarning -> "Warning"
                else       -> "Healthy"
            }
            Box(
                modifier         = GlanceModifier
                    .background(
                        when {
                            !isHealthy -> GlanceTheme.colors.errorContainer
                            hasWarning -> GlanceTheme.colors.tertiaryContainer
                            else       -> GlanceTheme.colors.primaryContainer
                        }
                    )
                    .cornerRadius(100.dp)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = statusText,
                    style = TextStyle(
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color      = when {
                            !isHealthy -> GlanceTheme.colors.onErrorContainer
                            hasWarning -> GlanceTheme.colors.onTertiaryContainer
                            else       -> GlanceTheme.colors.onPrimaryContainer
                        }
                    )
                )
            }
        }

        // Storage progress section — mirrors StorageCard logic exactly
        val size      = pool.size
        val allocated = pool.allocated
        val free      = pool.free

        if (size != null && allocated != null && size > 0) {
            val usedFraction = (allocated.toFloat() / size.toFloat()).coerceIn(0f, 1f)

            Spacer(GlanceModifier.height(10.dp))
            Row(
                modifier          = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text     = "Used: ${formatBytes(allocated)}",
                    style    = TextStyle(
                        fontSize = 11.sp,
                        color    = GlanceTheme.colors.onSurfaceVariant
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
                Text(
                    text  = "Free: ${free?.let { formatBytes(it) } ?: "N/A"}",
                    style = TextStyle(
                        fontSize = 11.sp,
                        color    = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }
            Spacer(GlanceModifier.height(6.dp))
            // Glance's LinearProgressIndicator – color mirrors Homepage progress tiers
            LinearProgressIndicator(
                progress = usedFraction,
                modifier = GlanceModifier.fillMaxWidth().height(8.dp),
                color    = when {
                    usedFraction > 0.8f -> GlanceTheme.colors.error
                    usedFraction > 0.6f -> GlanceTheme.colors.tertiary   // orange-ish
                    else                -> GlanceTheme.colors.primary
                },
                backgroundColor = GlanceTheme.colors.surfaceVariant
            )
            Spacer(GlanceModifier.height(6.dp))
            Text(
                text  = "Total: ${formatBytes(size)}" +
                        (pool.fragmentation?.let { " • Frag: $it%" } ?: ""),
                style = TextStyle(
                    fontSize = 10.sp,
                    color    = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }

        // Warning detail — mirrors Homepage StorageCard warning text
        if (hasWarning && !pool.status_detail.isNullOrEmpty()) {
            Spacer(GlanceModifier.height(6.dp))
            Text(
                text  = pool.status_detail,
                style = TextStyle(
                    fontSize = 10.sp,
                    color    = GlanceTheme.colors.tertiary
                ),
                maxLines = 2
            )
        }
    }
}

/**
 * Footer button — mirrors [com.imnotndesh.truehub.ui.widgets.WidgetFooterButton] in AppsUpdateWidget exactly.
 */
@Composable
private fun PoolsFooterButton(label: String, context: Context) {
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 8.dp, bottom = 12.dp)
            .background(GlanceTheme.colors.primary)
            .cornerRadius(14.dp)
            .clickable(actionStartActivity(openPoolsIntent(context))),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text     = label,
            style    = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize   = 13.sp,
                color      = GlanceTheme.colors.onPrimary
            ),
            modifier = GlanceModifier.padding(vertical = 10.dp)
        )
    }
}