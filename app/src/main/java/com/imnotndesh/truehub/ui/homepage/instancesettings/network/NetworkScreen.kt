package com.imnotndesh.truehub.ui.homepage.instancesettings.network

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader

@Composable
fun NetworkScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {},
    onNavigateToEdit: () -> Unit = {}
) {
    val vm: NetworkConfigViewModel = viewModel(
        factory = NetworkConfigViewModel.ViewModelFactory(manager)
    )
    val uiState by vm.uiState.collectAsState()

    LaunchedEffect(Unit) { vm.loadAll() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        colorScheme.surface,
                        colorScheme.surfaceContainer
                    )
                )
            )
    ) {
        UnifiedScreenHeader(
            title = "Network",
            subtitle = "Summary, interfaces, and configuration",
            isLoading = uiState.isLoading,
            isRefreshing = false,
            error = uiState.error,
            onDismissError = { vm.clearError() },
            manager = manager,
            onBackPressed = onNavigateBack
        )

        when {
            uiState.isLoading -> LoadingScreen("Loading network information...")
            uiState.summary != null && uiState.config != null -> {
                UnifiedContent(
                    summary = uiState.summary!!,
                    config = uiState.config!!,
                    onNavigateToEdit = onNavigateToEdit
                )
            }
            uiState.error != null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        uiState.error ?: "Unknown error",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun UnifiedContent(
    summary: System.NetworkGeneralSummaryResult,
    config: System.NetworkConfigurationEntry,
    onNavigateToEdit: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Interfaces ──
        item {
            SectionHeader("Interfaces", Icons.Default.SettingsEthernet)
        }
        if (summary.ips.isEmpty()) {
            item {
                EmptyFieldCard("No interface data available")
            }
        } else {
            summary.ips.forEach { (iface, ipInfo) ->
                item(key = "iface_$iface") {
                    InterfaceCard(iface, ipInfo)
                }
            }
        }

        // ── Default Routes ──
        item {
            SectionHeader("Default Routes", Icons.Default.Router)
        }
        if (summary.defaultRoutes.isEmpty()) {
            item { EmptyFieldCard("No default routes configured") }
        } else {
            summary.defaultRoutes.forEachIndexed { idx, route ->
                item(key = "route_$idx") { SimpleFieldCard(route, Icons.Default.Router) }
            }
        }

        // ── Nameservers ──
        item {
            SectionHeader("Nameservers", Icons.Default.Dns)
        }
        if (summary.nameservers.isEmpty()) {
            item { EmptyFieldCard("No DNS nameservers configured") }
        } else {
            summary.nameservers.forEachIndexed { idx, ns ->
                item(key = "ns_$idx") { SimpleFieldCard(ns, Icons.Default.Language) }
            }
        }

        // ── Divider between summary and config ──
        item {
            HorizontalDivider(
                color = colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        item {
            Text(
                "Configuration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // ── Identity ──
        item { SectionHeader("Identity", Icons.Default.Public) }
        item {
            DetailCard(
                items = listOf(
                    Pair("Hostname", config.hostname),
                    Pair("Hostname (local)", config.hostnameLocal?.ifEmpty { null } ?: "Not configured"),
                    Pair("Domain", config.domain)
                )
            )
        }

        // ── Gateways ──
        item { SectionHeader("Gateways", Icons.Default.Router) }
        item {
            DetailCard(
                items = listOf(
                    Pair("IPv4 Gateway", config.ipv4Gateway ?: "Not set"),
                    Pair("IPv6 Gateway", config.ipv6Gateway ?: "Not set")
                )
            )
        }

        // ── DNS ──
        item { SectionHeader("DNS Servers", Icons.Default.Language) }
        item {
            DetailCard(
                items = listOf(
                    Pair("Nameserver 1", config.nameserver1 ?: "Not set"),
                    Pair("Nameserver 2", config.nameserver2 ?: "Not set"),
                    Pair("Nameserver 3", config.nameserver3 ?: "Not set")
                )
            )
        }

        // ── HTTP Proxy ──
        if (!config.httpProxy.isNullOrBlank()) {
            item { SectionHeader("HTTP Proxy", Icons.Default.Visibility) }
            item { DetailCard(items = listOf(Pair("Proxy URL", config.httpProxy ?: ""))) }
        }

        // ── Search Domains ──
        if (config.domains.isNotEmpty()) {
            item { SectionHeader("Search Domains", Icons.Default.Language) }
            item { DetailCard(items = config.domains.map { Pair("Domain", it) }) }
        }

        // ── Static Hosts ──
        if (config.hosts.isNotEmpty()) {
            item { SectionHeader("Static Hosts", Icons.Default.Shield) }
            item { DetailCard(items = config.hosts.map { Pair("Entry", it) }) }
        }

        // ── Service Announcement ──
        item { SectionHeader("Service Announcement", Icons.Default.Visibility) }
        item {
            val sa = config.serviceAnnouncement
            if (sa != null) {
                DetailCard(
                    items = listOf(
                        Pair("NetBIOS", if (sa.netbios == true) "Enabled" else "Disabled"),
                        Pair("mDNS", if (sa.mdns == true) "Enabled" else "Disabled"),
                        Pair("WSD", if (sa.wsd == true) "Enabled" else "Disabled")
                    )
                )
            } else {
                DetailCard(items = listOf(Pair("Service Announcement", "Not available")))
            }
        }

        // ── Activity Filtering ──
        item { SectionHeader("Activity Filtering", Icons.Default.Shield) }
        item {
            DetailCard(
                items = listOf(
                    Pair("Type", config.activity.type),
                    Pair("Activities", if (config.activity.activities.isEmpty()) "None" else config.activity.activities.joinToString(", "))
                )
            )
        }

        // ── HA Hostnames ──
        if (config.hostnameB != null || config.hostnameVirtual != null) {
            item { SectionHeader("HA Hostnames", Icons.Default.Public) }
            item {
                val haItems: List<Pair<String, String>?> = listOf(
                    config.hostnameB?.let { Pair("Hostname B", it) },
                    config.hostnameVirtual?.let { Pair("Virtual Hostname", it) }
                )
                DetailCard(items = haItems.filterNotNull())
            }
        }

        // ── Current State ──
        item { SectionHeader("Current State", Icons.Default.CheckCircle) }
        item {
            val st = config.state
            val stateItems: List<Pair<String, String>?> = listOf(
                Pair("IPv4 Gateway", st.ipv4Gateway ?: "Not set"),
                Pair("IPv6 Gateway", st.ipv6Gateway ?: "Not set"),
                Pair("Nameserver 1", st.nameserver1 ?: "Not set"),
                Pair("Nameserver 2", st.nameserver2 ?: "Not set"),
                Pair("Nameserver 3", st.nameserver3 ?: "Not set"),
                if (st.hosts.isNotEmpty()) Pair("Hosts", "${st.hosts.size} entries") else null
            )
            DetailCard(items = stateItems.filterNotNull())
        }

        // ── Edit button ──
        item {
            Button(
                onClick = onNavigateToEdit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Edit Configuration")
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InterfaceCard(iface: String, ipInfo: System.NetworkGeneralSummaryIP) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SettingsEthernet, contentDescription = null, tint = colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(iface, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
            ipInfo.ipv4?.forEach { addr ->
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("IPv4", style = MaterialTheme.typography.labelSmall, color = colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    Text(addr, style = MaterialTheme.typography.bodyMedium)
                }
            }
            ipInfo.ipv6?.forEach { addr ->
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("IPv6", style = MaterialTheme.typography.labelSmall, color = colorScheme.tertiary, fontWeight = FontWeight.SemiBold)
                    Text(addr, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)) {
        Icon(icon, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = colorScheme.primary)
    }
}

@Composable
private fun SimpleFieldCard(value: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) { Icon(icon, contentDescription = null, tint = colorScheme.onSecondaryContainer, modifier = Modifier.size(18.dp)) }
            Spacer(Modifier.width(12.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun DetailCard(items: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            items.forEachIndexed { idx, (label, value) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = colorScheme.onSurfaceVariant)
                    Text(value, style = MaterialTheme.typography.bodyMedium)
                }
                if (idx < items.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = colorScheme.outlineVariant.copy(alpha = 0.4f))
                }
            }
        }
    }
}

@Composable
private fun EmptyFieldCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow)
    ) {
        Text(message, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
    }
}
