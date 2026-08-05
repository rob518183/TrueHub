package com.imnotndesh.truehub.ui.homepage.instancesettings.systeminformation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.homepage.instancesettings.general.ExpressiveSection

@Composable
fun SystemInformationScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {}
) {
    val vm: SystemInformationViewModel = viewModel(
        factory = SystemInformationViewModel.ViewModelFactory(manager)
    )
    val uiState by vm.uiState.collectAsState()

    LaunchedEffect(Unit) { vm.refresh() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            UnifiedScreenHeader(
                title = "System Information",
                subtitle = "Identifiers, state and features",
                isLoading = uiState.isLoading,
                isRefreshing = false,
                error = uiState.error,
                onDismissError = { vm.clearError() },
                manager = manager,
                onBackPressed = onNavigateBack
            )
        }
    ) { innerPadding ->
        PullToRefreshContent(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { vm.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    ExpressiveSection(title = "Operating System", icon = Icons.Default.Build) {
                        InfoCard {
                            InfoRow(label = "Version", value = uiState.versionShort ?: uiState.version ?: "—")
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            InfoRow(label = "Full Version", value = uiState.version ?: "—")
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            InfoRow(label = "Product Type", value = uiState.productType ?: "—")
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            InfoRow(label = "State", value = uiState.state ?: "—")
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            InfoRow(label = "Ready", value = booleanLabel(uiState.ready))
                        }
                    }
                }

                item {
                    ExpressiveSection(title = "System Identity", icon = Icons.Default.Fingerprint) {
                        InfoCard {
                            InfoRow(label = "Host ID", value = uiState.hostId ?: "—")
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            InfoRow(label = "Boot ID", value = uiState.bootId ?: "—")
                        }
                    }
                }

                item {
                    ExpressiveSection(title = "Features", icon = Icons.Default.Verified) {
                        InfoCard {
                            InfoRow(label = "Deduplication", value = booleanLabel(uiState.dedupEnabled))
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            InfoRow(label = "Fibre Channel", value = booleanLabel(uiState.fibreChannelEnabled))
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            InfoRow(label = "Virtual Machines", value = booleanLabel(uiState.vmEnabled))
                        }
                    }
                }

                item {
                    ExpressiveSection(title = "Release Notes", icon = Icons.Default.Link) {
                        InfoCard {
                            InfoText(uiState.releaseNotesUrl ?: "—")
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun InfoCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) { content() }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
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
            modifier = Modifier.weight(0.6f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun InfoText(value: String) {
    Text(
        text = value,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis
    )
}

private fun booleanLabel(value: Boolean?): String = when (value) {
    true -> "Yes"
    false -> "No"
    null -> "—"
}
