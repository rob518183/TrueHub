package com.imnotndesh.truehub.ui.homepage.instancesettings.general

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSystemSettingsScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {},
    onNavigateToEdit: () -> Unit = {}
) {
    val vm: GeneralSystemSettingsViewModel = viewModel(
        factory = GeneralSystemSettingsViewModel.ViewModelFactory(manager)
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
            title = "General Settings",
            subtitle = "System general configuration",
            isLoading = uiState.isLoading,
            isRefreshing = false,
            error = uiState.error,
            onDismissError = { vm.clearError() },
            manager = manager,
            onBackPressed = onNavigateBack
        )

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = Color.Transparent
        ) { innerPadding ->
            when {
                uiState.isLoading -> LoadingScreen("Loading system settings...")
                uiState.config != null -> {
                    val config = uiState.config!!
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // ── Rollback waiting banner ──
                        uiState.checkinWaiting?.let { seconds ->
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(colorScheme.tertiaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Timer, null,
                                                tint = colorScheme.onTertiaryContainer,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "Rollback pending",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = colorScheme.onTertiaryContainer
                                            )
                                            Text(
                                                "Automatic rollback in ${seconds}s. Check in to confirm changes.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { vm.doCheckin() },
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Check in", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }

                        // ── Local URL card ──
                        uiState.localUrl?.let { url ->
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = colorScheme.surfaceContainerLow
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Link, null,
                                                tint = colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                "Local URL",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                url,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium,
                                                color = colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ── Web Interface section ──
                        item {
                            SectionCard(
                                title = "Web Interface",
                                icon = Icons.Default.Visibility
                            ) {
                                DetailRow("HTTP Port", config.uiPort.toString())
                                DetailRow("HTTPS Port", config.uiHttpsPort.toString())
                                DetailRow("HTTPS Redirect", if (config.uiHttpsRedirect == true) "Enabled" else "Disabled")
                                DetailRow("HTTPS Protocols", config.uiHttpsProtocols?.joinToString(", ")
                                    .orEmpty().ifEmpty { "Default" })
                                DetailRow("Console Messages", if (config.uiConsoleMsg == true) "Enabled" else "Disabled")
                                DetailRow("X-Frame-Options", config.uiXFrameOptions ?: "SAMEORIGIN")
                            }
                        }

                        // ── Network Addresses section ──
                        item {
                            SectionCard(
                                title = "Network Addresses",
                                icon = Icons.Default.SettingsEthernet
                            ) {
                                DetailRow(
                                    "IPv4 Addresses",
                                    config.uiAddress?.joinToString(", ").orEmpty().ifEmpty { "All (0.0.0.0)" }
                                )
                                DetailRow(
                                    "IPv6 Addresses",
                                    config.uiV6Address?.joinToString(", ").orEmpty().ifEmpty { "All (::)" }
                                )
                                DetailRow(
                                    "Allow List",
                                    config.uiAllowlist?.joinToString(", ").orEmpty().ifEmpty { "None" }
                                )
                            }
                        }

                        // ── Localization section ──
                        item {
                            SectionCard(
                                title = "Localization",
                                icon = Icons.Default.Language
                            ) {
                                DetailRow("Timezone", config.timezone.orEmpty().ifEmpty { "Not Configured" })
                                DetailRow("Keyboard Map", config.kbdMap.orEmpty().ifEmpty { "Not Configured" })
                            }
                        }

                        // ── Other section ──
                        item {
                            SectionCard(
                                title = "Other",
                                icon = Icons.Default.Shield
                            ) {
                                DetailRow("Wizard Shown", if (config.wizardShown == true) "Yes" else "No")
                                DetailRow("DS Auth", if (config.dsAuth == true) "Enabled" else "Disabled")
                                DetailRow(
                                    "Usage Collection",
                                    if (config.usageCollectionIsSet == true) "Configured" else "Not configured"
                                )
                            }
                        }

                        // ── Edit button ──
                        item {
                            Button(
                                onClick = onNavigateToEdit,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Edit Settings", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
internal fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon, null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            content()
        }
    }
}

@Composable
internal fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f),
            maxLines = 1
        )
    }
}
