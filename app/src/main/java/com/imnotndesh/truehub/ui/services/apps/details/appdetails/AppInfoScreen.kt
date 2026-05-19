package com.imnotndesh.truehub.ui.services.apps.details.appdetails

import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import dev.jeziellago.compose.markdowntext.MarkdownText
import com.imnotndesh.truehub.R

@Composable
fun AppInfoScreen(
    app: Apps.AppQueryResponse,
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit,
    onNavigateToMarketplaceCategory: (String) -> Unit = {},
    onNavigateToMarketplaceAppDetails: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: AppDetailsViewModel = viewModel(
        factory = AppDetailsViewModel.provideFactory(manager),
        key = app.name
    )

    val similarApps by viewModel.similarApps.collectAsState()
    val isLoadingSimilar by viewModel.isLoadingSimilar.collectAsState()

    LaunchedEffect(app.name) {
        viewModel.loadSimilarApps(
            appName = app.name,
            train = app.metadata?.train ?: "stable"
        )
    }

    Scaffold(
        topBar = {
            UnifiedScreenHeader(
                title = app.metadata?.title ?: app.name,
                subtitle = "App Details",
                isLoading = false,
                isRefreshing = false,
                error = null,
                onDismissError = {},
                manager = manager,
                onBackPressed = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // Hero Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .clickable { onNavigateToMarketplaceAppDetails(app.name) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!app.metadata?.icon.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(app.metadata.icon)
                                    .decoderFactory(SvgDecoder.Factory())
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "${app.metadata.title ?: app.name} icon",
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Fit,
                                placeholder = rememberVectorPainter(Icons.Default.Apps),
                                error = rememberVectorPainter(Icons.Default.Apps)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.metadata?.title ?: app.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.clickable { onNavigateToMarketplaceAppDetails(app.name) }
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Version: ${app.humanVersion ?: app.version ?: "Unknown"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            ExpressiveSection(title = "Basic Information", icon = Icons.Default.Info) {
                ExpressiveInfoCard {
                    InfoRow(label = "App Name", value = app.metadata?.title ?: app.name)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    InfoRow(label = "ID", value = app.id)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    InfoRow(label = "Version", value = app.humanVersion ?: app.version ?: "Unknown")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    InfoRow(label = "Status", value = app.state.replaceFirstChar { it.uppercase() })
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    InfoRow(label = "Catalog", value = app.metadata?.train ?: "Unknown")

                    if (app.upgrade_available) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        InfoRow(label = "Latest Version", value = app.latestVersion ?: "Update Available")
                    }

                    app.metadata?.dateAdded?.let {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        InfoRow(label = "Date Added", value = it)
                    }
                    app.metadata?.lastUpdate?.let {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        InfoRow(label = "Last Updated", value = it)
                    }
                }
            }

            app.metadata?.description?.let { description ->
                ExpressiveSection(title = "Description", icon = Icons.Default.Description) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val textColor = MaterialTheme.colorScheme.onSurfaceVariant
                        val linkColor = MaterialTheme.colorScheme.primary

                        AndroidView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            factory = { ctx ->
                                TextView(ctx).apply {
                                    movementMethod = LinkMovementMethod.getInstance()
                                    textSize = 14f
                                }
                            },
                            update = { textView ->
                                textView.setTextColor(textColor.hashCode())
                                textView.setLinkTextColor(linkColor.hashCode())
                                textView.text = HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_LEGACY)
                            }
                        )
                    }
                }
            }

            if (isLoadingSimilar || similarApps.isNotEmpty()) {
                SimilarAppsSection(
                    apps = similarApps,
                    isLoading = isLoadingSimilar,
                    onSeeMoreClick = { onNavigateToMarketplaceCategory(app.metadata?.categories?.firstOrNull() ?: "") },
                    onAppClick = { selectedSimilarAppName -> onNavigateToMarketplaceAppDetails(selectedSimilarAppName) }
                )
            }

            if (!app.metadata?.categories.isNullOrEmpty() || !app.metadata?.keywords.isNullOrEmpty()) {
                ExpressiveSection(title = "Categories & Tags", icon = Icons.Default.Tag) {
                    app.metadata.categories?.let { categories ->
                        ServiceInfoChipGroup(
                            title = "Categories",
                            items = categories,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            onChipClick = { onNavigateToMarketplaceCategory(it) }
                        )
                    }

                    app.metadata.keywords?.let { keywords ->
                        if (keywords.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            ServiceInfoChipGroup(
                                title = "Keywords",
                                items = keywords,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                onChipClick = {}
                            )
                        }
                    }
                }
            }

            app.portals?.let { portals ->
                if (portals.isNotEmpty()) {
                    ExpressiveSection(title = "Web Portals", icon = Icons.AutoMirrored.Filled.Launch) {
                        portals.forEach { (name, url) ->
                            ServicePortalCard(name = name, url = url)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            app.activeWorkloads?.usedPorts?.let { ports ->
                if (ports.isNotEmpty()) {
                    ExpressiveSection(title = "Network & Ports", icon = Icons.Default.NetworkCheck) {
                        ports.forEach { port ->
                            ServicePortCard(port = port)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            app.activeWorkloads?.containerDetails?.let { containers ->
                if (containers.isNotEmpty()) {
                    ExpressiveSection(title = "Containers", icon = Icons.Default.Apps) {
                        containers.forEach { container ->
                            ServiceContainerCard(container = container)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            val volumes = app.activeWorkloads?.volumes
            val hostMounts = app.metadata?.hostMounts

            if (!volumes.isNullOrEmpty() || !hostMounts.isNullOrEmpty()) {
                ExpressiveSection(title = "Storage & Mounts", icon = Icons.Default.Storage) {
                    volumes?.forEach { volume ->
                        ServiceVolumeCard(volume = volume)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    hostMounts?.forEach { mount ->
                        ServiceHostMountCard(mount = mount)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            app.metadata?.runAsContext?.let { contexts ->
                if (contexts.isNotEmpty()) {
                    ExpressiveSection(title = "Security Context", icon = Icons.Default.AccountBox) {
                        contexts.forEach { context ->
                            ServiceRunAsContextCard(context = context)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            app.metadata?.capabilities?.let { capabilities ->
                if (capabilities.isNotEmpty()) {
                    ExpressiveSection(title = "Capabilities", icon = Icons.Default.Build) {
                        capabilities.forEach { capability ->
                            ServiceCapabilityCard(capability = capability)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            app.metadata?.maintainers?.let { maintainers ->
                if (maintainers.isNotEmpty()) {
                    ExpressiveSection(title = "Maintainers", icon = Icons.Default.Person) {
                        maintainers.forEach { maintainer ->
                            ServiceMaintainerCard(maintainer = maintainer)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            if (app.metadata?.home != null || !app.metadata?.sources.isNullOrEmpty()) {
                ExpressiveSection(title = "Resources", icon = Icons.Default.Link) {
                    app.metadata.home?.let { home ->
                        LinkButton(name = "Homepage", url = home, icon = Icons.Default.Home)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    app.metadata.sources?.forEach { source ->
                        LinkButton(name = "Source Code", url = source, icon = Icons.Default.Code)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    app.metadata.changelogUrl?.let { changelog ->
                        LinkButton(name = "Changelog", url = changelog, icon = Icons.Default.Description)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            app.notes?.let { notes ->
                if (notes.isNotBlank()) {
                    ExpressiveSection(title = "Notes", icon = Icons.AutoMirrored.Filled.Note) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.padding(16.dp)) {
                                MarkdownText(
                                    markdown = notes,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ExpressiveSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Column(content = content)
    }
}

@Composable
fun ExpressiveInfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceInfoChipGroup(
    title: String,
    items: List<String>,
    containerColor: Color,
    contentColor: Color,
    onChipClick: (String) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEach { item ->
                FilterChip(
                    onClick = { onChipClick(item) },
                    label = { Text(text = item) },
                    selected = false,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = containerColor,
                        labelColor = contentColor,
                        disabledContainerColor = containerColor,
                        disabledLabelColor = contentColor
                    ),
                    border = null,
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }
    }
}

@Composable
private fun ServicePortCard(port: Apps.UsedPort) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.NetworkCheck,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Port ${port.containerPort} (${port.protocol.uppercase()})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            port.hostPorts.forEach { hostPort ->
                Text(
                    text = "→ Host: ${hostPort.hostIp}:${hostPort.hostPort}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = 24.dp, top = 4.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ServicePortalCard(name: String, url: String) {
    val uriHandler = LocalUriHandler.current
    Surface(
        onClick = { uriHandler.openUri(url) },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Launch,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ServiceContainerCard(container: Apps.ContainerDetail) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = container.serviceName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Image: ${container.image}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ServiceVolumeCard(volume: Apps.Volume) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Source: ${volume.source}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(text = "Destination: ${volume.destination}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ServiceHostMountCard(mount: Apps.HostMount) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Host Mount", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(text = "Host Path: ${mount.hostPath}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ServiceRunAsContextCard(context: Apps.RunAsContext) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AccountBox, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "UID: ${context.uid} | GID: ${context.gid}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ServiceCapabilityCard(capability: Apps.Capability) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = capability.name, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ServiceMaintainerCard(maintainer: Apps.Maintainer) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = maintainer.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(text = "Email: ${maintainer.email}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun LinkButton(name: String, url: String, icon: ImageVector) {
    val uriHandler = LocalUriHandler.current
    Card(
        onClick = { uriHandler.openUri(url) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                Text(text = url, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    }
}
// TODO: Update this section to fix icons not loading
@Composable
fun SimilarAppsSection(
    apps: List<Apps.AppSimilarResponse>,
    isLoading: Boolean,
    onSeeMoreClick: () -> Unit,
    onAppClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Similar Applications",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (!isLoading) {
                TextButton(onClick = onSeeMoreClick) {
                    Text(text = "See More", fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(apps.take(6)) { appItem ->
                Column(
                    modifier = Modifier
                        .width(72.dp)
                        .clickable { onAppClick(appItem.name) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val context = LocalContext.current
                    val cornerRadius = (60 * 0.22f).dp

                    if (!appItem.iconUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(appItem.iconUrl)
                                .decoderFactory(SvgDecoder.Factory())
                                .crossfade(true)
                                .build(),
                            contentDescription = "${appItem.title ?: appItem.name} icon",
                            contentScale = ContentScale.Fit,
                            placeholder = painterResource(id = R.drawable.missing_app_icon),
                            error = painterResource(id = R.drawable.missing_app_icon),
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(cornerRadius))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(cornerRadius))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size((60 * 0.7f).dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = appItem.title ?: appItem.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}