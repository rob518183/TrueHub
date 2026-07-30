package com.imnotndesh.truehub.ui.homepage.instancesettings.audit

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.imnotndesh.truehub.data.api.TrueNASApiManager

/**
 * Displays the current audit configuration and allows editing the mutable fields
 * (retention, reservation, quota, quota_fill_warning, quota_fill_critical).
 *
 * Read-only fields (space, remote_logging_enabled, enabled_services) are shown
 * for information only.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditConfigScreen(
    manager: TrueNASApiManager,
    viewModel: AuditConfigViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Field state for edit mode, initialised from the loaded config
    var retention by remember { mutableStateOf("") }
    var reservation by remember { mutableStateOf("") }
    var quota by remember { mutableStateOf("") }
    var fillWarning by remember { mutableStateOf("") }
    var fillCritical by remember { mutableStateOf("") }

    // Once config loads, populate the fields
    LaunchedEffect(uiState.config) {
        uiState.config?.let { cfg ->
            retention = cfg.retention.toString()
            reservation = cfg.reservation.toString()
            quota = cfg.quota.toString()
            fillWarning = cfg.quotaFillWarning.toString()
            fillCritical = cfg.quotaFillCritical.toString()
        }
    }

    // Show snackbar on save result
    LaunchedEffect(uiState.saveResult) {
        when (val result = uiState.saveResult) {
            is AuditSaveResult.Success -> {
                snackbarHostState.showSnackbar("Audit settings saved")
            }
            is AuditSaveResult.Error -> {
                snackbarHostState.showSnackbar("Error: ${result.message}")
            }
            null -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audit Configuration") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (uiState.isLoading && uiState.config == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading audit configuration…", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Space usage card (read-only) ──
                uiState.config?.space?.let { space ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.SdStorage,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "ZFS Dataset Space",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            SpaceRow("Used", formatBytes(space.used))
                            SpaceRow("Used by dataset", formatBytes(space.usedByDataset))
                            SpaceRow("Used by reservation", formatBytes(space.usedByReservation))
                            SpaceRow("Used by snapshots", formatBytes(space.usedBySnapshots))
                            SpaceRow("Available", formatBytes(space.available))
                        }
                    }
                }

                // ── Read-only service info ──
                uiState.config?.let { cfg ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Cloud,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Remote logging: ${if (cfg.remoteLoggingEnabled) "Enabled" else "Disabled"}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Enabled services: ${cfg.enabledServices.middleware.size + cfg.enabledServices.smb.size + cfg.enabledServices.sudo.size} configured",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // ── Editable fields ──
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        HorizontalDivider()

                        EditableField(
                            value = retention,
                            onValueChange = { retention = it },
                            label = "Retention (days)",
                            icon = Icons.Default.Schedule,
                            supportingText = "1 – 30"
                        )
                        EditableField(
                            value = reservation,
                            onValueChange = { reservation = it },
                            label = "Reservation (GiB)",
                            icon = Icons.Default.Dataset,
                            supportingText = "0 – 100"
                        )
                        EditableField(
                            value = quota,
                            onValueChange = { quota = it },
                            label = "Quota (GiB)",
                            icon = Icons.Default.SdStorage,
                            supportingText = "0 – 100"
                        )
                        EditableField(
                            value = fillWarning,
                            onValueChange = { fillWarning = it },
                            label = "Quota fill warning (%)",
                            icon = Icons.Default.Warning,
                            supportingText = "5 – 80"
                        )
                        EditableField(
                            value = fillCritical,
                            onValueChange = { fillCritical = it },
                            label = "Quota fill critical (%)",
                            icon = Icons.Default.Warning,
                            supportingText = "50 – 95"
                        )
                    }
                }

                // ── Action buttons ──
                if (uiState.isSaving) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Discard")
                        }
                        Button(
                            onClick = {
                                viewModel.saveAuditConfig(
                                    retention = retention.toIntOrNull(),
                                    reservation = reservation.toIntOrNull(),
                                    quota = quota.toIntOrNull(),
                                    quotaFillWarning = fillWarning.toIntOrNull(),
                                    quotaFillCritical = fillCritical.toIntOrNull()
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Save")
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SpaceRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun EditableField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    supportingText: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp)) },
        supportingText = { Text(supportingText) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1.shl(30) -> "%.1f GiB".format(bytes.toDouble() / (1.shl(30)))
        bytes >= 1.shl(20) -> "%.1f MiB".format(bytes.toDouble() / (1.shl(20)))
        bytes >= 1.shl(10) -> "%.1f KiB".format(bytes.toDouble() / (1.shl(10)))
        else -> "$bytes B"
    }
}
