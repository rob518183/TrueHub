package com.imnotndesh.truehub.ui.homepage.instancesettings.boot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A pull-to-refresh wrapper that reuses the same `PullToRefreshBox` behaviour used across
 * the other instance-config pages, but swaps in the Material3 expressive [LoadingIndicator]
 * (the shape-morphing indicator) as the refresh indicator.
 *
 * @param isRefreshing Whether a refresh operation is being triggered.
 * @param onRefresh Callback invoked when the user pulls down far enough.
 * @param content The scrollable content to wrap.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BootPullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val refreshState = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = refreshState,
        modifier = modifier,
        indicator = {
            BootRefreshIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter))
        },
        content = content
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BootRefreshIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .offset(y = 16.dp)
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        LoadingIndicator(
            modifier = Modifier.size(28.dp).align(androidx.compose.ui.Alignment.Center),
            color = MaterialTheme.colorScheme.primary,
            polygons = LoadingIndicatorDefaults.IndeterminateIndicatorPolygons
        )
    }
}
