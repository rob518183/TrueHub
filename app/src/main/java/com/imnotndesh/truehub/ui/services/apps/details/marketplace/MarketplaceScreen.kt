package com.imnotndesh.truehub.ui.services.apps.details.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.services.apps.AppsScreenViewModel

@Composable
fun MarketplaceScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit,
    onAppDetailsClick: (Apps.AppAvailableItem) -> Unit
) {
    val viewModel: AppsScreenViewModel = viewModel(
        factory = AppsScreenViewModel.AppsScreenViewModelFactory(manager)
    )
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (uiState.marketplaceApps.isEmpty()) {
            viewModel.loadMarketplaceApps()
        }
    }

    val searchFilteredApps by remember(searchQuery, uiState.marketplaceApps) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                uiState.marketplaceApps
            } else {
                uiState.marketplaceApps.filter { app ->
                    app.title.contains(searchQuery, ignoreCase = true) ||
                            app.name.contains(searchQuery, ignoreCase = true) ||
                            app.tags.contains(searchQuery, ignoreCase = true) ||
                            app.description.contains(searchQuery, ignoreCase = true)
                }
            }
        }
    }

    val recommendedApps by remember(searchFilteredApps) {
        derivedStateOf { searchFilteredApps.filter { it.recommended } }
    }

    val localCategories by remember(searchFilteredApps) {
        derivedStateOf {
            searchFilteredApps.flatMap { it.categories ?: emptyList() }.distinct().sorted()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            UnifiedScreenHeader(
                title = "Marketplace",
                subtitle = "Discover chart applications",
                isLoading = uiState.isLoading,
                isRefreshing = uiState.isRefreshing,
                error = uiState.error,
                onDismissError = { viewModel.clearError() },
                manager = manager,
                onBackPressed = onNavigateBack
            )

            // Play Store Style Search Input Component Header
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search apps, utilities, tools...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Box(modifier = Modifier.weight(1f)) {
                if (uiState.isLoading && uiState.marketplaceApps.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        if (searchQuery.isBlank()) {
                            if (recommendedApps.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Recommended Solutions",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 12.dp)
                                    )
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(recommendedApps, key = { "rec-${it.name}" }) { app ->
                                            MarketplaceFeaturedCard(app = app, onClick = { onAppDetailsClick(app) })
                                        }
                                    }
                                }
                            }

                            items(localCategories) { categoryName ->
                                val categoryApps = searchFilteredApps.filter { it.categories?.contains(categoryName) == true }
                                if (categoryApps.isNotEmpty()) {
                                    Text(
                                        text = categoryName.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp)
                                    )
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(categoryApps, key = { "cat-$categoryName-${it.name}" }) { app ->
                                            MarketplaceRowStandardItem(app = app, onClick = { onAppDetailsClick(app) })
                                        }
                                    }
                                }
                            }
                        }
                        else {
                            item {
                                Text(
                                    text = "Search Results (${searchFilteredApps.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 12.dp)
                                )
                            }
                            items(searchFilteredApps, key = { "search-${it.name}" }) { app ->
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                    MarketplaceSearchRowResultItem(app = app, onClick = { onAppDetailsClick(app) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MarketplaceFeaturedCard(
    app: Apps.AppAvailableItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIconFrame(iconUrl = app.icon_url, title = app.title, size = 48)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.title.ifBlank { app.name },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (app.installed) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, Modifier.size(12.dp), tint = Color(0xFF2E7D32))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Installed", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = app.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MarketplaceRowStandardItem(
    app: Apps.AppAvailableItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(96.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            AppIconFrame(iconUrl = app.icon_url, title = app.title, size = 64)
            if (app.installed) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(Color.White, CircleShape)
                        .padding(1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp), tint = Color(0xFF2E7D32))
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = app.title.ifBlank { app.name },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun MarketplaceSearchRowResultItem(
    app: Apps.AppAvailableItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (app.installed) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Installed",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.padding(end = 10.dp).size(20.dp)
                )
            }
            AppIconFrame(iconUrl = app.icon_url, title = app.title, size = 52)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.title.ifBlank { app.name },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AppIconFrame(iconUrl: String?, title: String, size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (!iconUrl.isNullOrBlank()) {
            AsyncImage(
                model = iconUrl,
                contentDescription = "$title logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(6.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size((size / 2).dp)
            )
        }
    }
}