package com.imnotndesh.truehub.ui.homepage.instancesettings.network

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Router
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader

@Composable
fun NetworkConfigScreen(
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
            title = "Network Configuration",
            subtitle = "Hostname, gateways, DNS and service announcements",
            isLoading = uiState.isLoading,
            isRefreshing = false,
            error = uiState.error,
            onDismissError = { vm.clearError() },
            manager = manager,
            onBackPressed = onNavigateBack
        )

        when {
            uiState.isLoading -> LoadingScreen("Loading network configuration...")
            uiState.config != null -> {
                val config = uiState.config!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        SectionHeader("Identity", Icons.Default.Public)
                    }
                    item {
                        DetailCard(
                            items = buildList {
                                add(Pair("Hostname", config.hostname))
                                config.hostnameLocal?.ifEmpty { "None Configured" }.let { add(Pair("Hostname (local)", it!!)) }
                                add(Pair("Domain", config.domain))
                            }
                        )
                    }
                    item {
                        SectionHeader("Gateways", Icons.Default.Router)
                    }
                    item {
                        DetailCard(
                            items = listOf(
                                Pair("IPv4 Gateway", config.ipv4Gateway ?: "Not set"),
                                Pair("IPv6 Gateway", config.ipv6Gateway ?: "Not set")
                            )
                        )
                    }

                    // ── DNS ──
                    item {
                        SectionHeader("DNS Servers", Icons.Default.Language)
                    }
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
                        item {
                            SectionHeader("HTTP Proxy", Icons.Default.Visibility)
                        }
                        item {
                            DetailCard(items = listOf(Pair("Proxy URL", config.httpProxy ?: "")))
                        }
                    }

                    // ── Domains ──
                    if (config.domains.isNotEmpty()) {
                        item {
                            SectionHeader("Search Domains", Icons.Default.Language)
                        }
                        item {
                            DetailCard(
                                items = config.domains.map { Pair("Domain", it) }
                            )
                        }
                    }

                    // ── Hosts ──
                    if (config.hosts.isNotEmpty()) {
                        item {
                            SectionHeader("Static Hosts", Icons.Default.Shield)
                        }
                        item {
                            DetailCard(
                                items = config.hosts.map { Pair("Entry", it) }
                            )
                        }
                    }

                    // ── Service Announcement ──
                    item {
                        SectionHeader("Service Announcement", Icons.Default.Visibility)
                    }
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

                    // ── Activity ──
                    item {
                        SectionHeader("Activity Filtering", Icons.Default.Shield)
                    }
                    item {
                        val act = config.activity
                        DetailCard(
                            items = listOf(
                                Pair("Type", act.type),
                                Pair("Activities", if (act.activities.isEmpty()) "None" else act.activities.joinToString(", "))
                            )
                        )
                    }

                    // ── HA hostnames (only if set) ──
                    if (config.hostnameB != null || config.hostnameVirtual != null) {
                        item {
                            SectionHeader("HA Hostnames", Icons.Default.Public)
                        }
                        item {
                            val haItems: List<Pair<String, String>?> = listOf(
                                config.hostnameB?.let { Pair("Hostname B", it) },
                                config.hostnameVirtual?.let { Pair("Virtual Hostname", it) }
                            )
                            DetailCard(items = haItems.filterNotNull())
                        }
                    }

                    // ── State info ──
                    item {
                        SectionHeader("Current State", Icons.Default.CheckCircle)
                    }
                    item {
                        val state = config.state
                        val stateItems: List<Pair<String, String>?> = listOf(
                            Pair("IPv4 Gateway", state.ipv4Gateway ?: "Not set"),
                            Pair("IPv6 Gateway", state.ipv6Gateway ?: "Not set"),
                            Pair("Nameserver 1", state.nameserver1 ?: "Not set"),
                            Pair("Nameserver 2", state.nameserver2 ?: "Not set"),
                            Pair("Nameserver 3", state.nameserver3 ?: "Not set"),
                            if (state.hosts.isNotEmpty()) Pair("Hosts", "\${state.hosts.size} entries") else null
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
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.primary
        )
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
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurface
                    )
                }
                if (idx < items.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}
