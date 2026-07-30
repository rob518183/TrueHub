package com.imnotndesh.truehub.ui.homepage.instancesettings.general

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSystemSettingsEditScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {},
    onCheckinNavigateBack: () -> Unit = {}
) {
    val vm: GeneralSystemSettingsViewModel = viewModel(
        factory = GeneralSystemSettingsViewModel.ViewModelFactory(manager)
    )
    val uiState by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { vm.loadAll() }

    // When save succeeds, navigate back
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Settings saved successfully")
            if (uiState.checkinWaiting != null) {
                vm.doCheckin()
            }
            onCheckinNavigateBack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); vm.clearError() }
    }

    val config = uiState.config

    // Local form state
    var uiPort by remember(config) { mutableStateOf(config?.uiPort?.toString() ?: "80") }
    var uiHttpsPort by remember(config) { mutableStateOf(config?.uiHttpsPort?.toString() ?: "443") }
    var uiHttpsRedirect by remember(config) { mutableStateOf(config?.uiHttpsRedirect ?: false) }
    var uiConsoleMsg by remember(config) { mutableStateOf(config?.uiConsoleMsg ?: false) }
    var uiXFrameOptions by remember(config) { mutableStateOf(config?.uiXFrameOptions ?: "SAMEORIGIN") }

    var selectedTimezone by remember(config) { mutableStateOf(config?.timezone ?: "") }
    var selectedKbdMap by remember(config) { mutableStateOf(config?.kbdMap ?: "") }

    var uiAddressText by remember(config) {
        mutableStateOf(config?.uiAddress?.joinToString(", ") ?: "0.0.0.0")
    }
    var uiV6AddressText by remember(config) {
        mutableStateOf(config?.uiV6Address?.joinToString(", ") ?: "::")
    }
    var uiAllowlistText by remember(config) {
        mutableStateOf(config?.uiAllowlist?.joinToString(", ") ?: "")
    }
    var selectedHttpsProtocols by remember(config) {
        mutableStateOf(config?.uiHttpsProtocols?.joinToString(", ") ?: "")
    }
    var dsAuth by remember(config) { mutableStateOf(config?.dsAuth ?: false) }

    var tzExpanded by remember { mutableStateOf(false) }
    var kbdExpanded by remember { mutableStateOf(false) }

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
            title = "Edit General Settings",
            subtitle = "Modify system general configuration",
            isLoading = uiState.isLoading,
            isRefreshing = false,
            error = null,
            onDismissError = {},
            manager = manager,
            onBackPressed = onNavigateBack
        )

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = Color.Transparent,
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(snackbarData = data)
                }
            }
        ) { innerPadding ->
            when {
                uiState.isLoading -> LoadingScreen("Loading settings...")
                config != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Rollback info card
                        RollbackInfoCard(uiState = uiState, vm = vm)

                        // ── Web Interface section ──
                        EditFormSection(title = "Web Interface", icon = Icons.Default.Visibility) {
                            OutlinedTextField(
                                value = uiPort,
                                onValueChange = { uiPort = it },
                                label = { Text("HTTP Port") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = uiHttpsPort,
                                onValueChange = { uiHttpsPort = it },
                                label = { Text("HTTPS Port") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ToggleRow(
                                label = "HTTPS Redirect",
                                description = "Redirect HTTP traffic to HTTPS",
                                checked = uiHttpsRedirect,
                                onCheckedChange = { uiHttpsRedirect = it }
                            )
                            OutlinedTextField(
                                value = selectedHttpsProtocols,
                                onValueChange = { selectedHttpsProtocols = it },
                                label = { Text("HTTPS Protocols") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                supportingText = { Text("e.g. TLSv1.2, TLSv1.3") }
                            )
                            ToggleRow(
                                label = "Console Messages",
                                description = "Show console messages on the UI",
                                checked = uiConsoleMsg,
                                onCheckedChange = { uiConsoleMsg = it }
                            )
                            OutlinedTextField(
                                value = uiXFrameOptions,
                                onValueChange = { uiXFrameOptions = it },
                                label = { Text("X-Frame-Options") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                supportingText = { Text("SAMEORIGIN, DENY, or ALLOWALL") }
                            )
                        }

                        // ── Network Addresses section ──
                        EditFormSection(title = "Network Addresses", icon = Icons.Default.SettingsEthernet) {
                            OutlinedTextField(
                                value = uiAddressText,
                                onValueChange = { uiAddressText = it },
                                label = { Text("IPv4 Addresses") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                supportingText = { Text("Comma separated. Leave empty for all (0.0.0.0)") }
                            )
                            OutlinedTextField(
                                value = uiV6AddressText,
                                onValueChange = { uiV6AddressText = it },
                                label = { Text("IPv6 Addresses") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                supportingText = { Text("Comma separated. Leave empty for all (::)") }
                            )
                            OutlinedTextField(
                                value = uiAllowlistText,
                                onValueChange = { uiAllowlistText = it },
                                label = { Text("Allow List") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                supportingText = { Text("IPs allowed to access the web interface") }
                            )
                        }

                        // ── Localization section ──
                        EditFormSection(title = "Localization", icon = Icons.Default.Language) {
                            // Timezone dropdown
                            ExposedDropdownMenuBox(
                                expanded = tzExpanded,
                                onExpandedChange = { tzExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedTimezone.ifEmpty {
                                        uiState.timezoneChoices.entries.firstOrNull { it.value == selectedTimezone }?.key
                                            ?: selectedTimezone
                                    },
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Timezone") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = tzExpanded)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = tzExpanded,
                                    onDismissRequest = { tzExpanded = false }
                                ) {
                                    uiState.timezoneChoices.forEach { (key, name) ->
                                        DropdownMenuItem(
                                            text = { Text("$key ($name)") },
                                            onClick = {
                                                selectedTimezone = key
                                                tzExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            // Keyboard map dropdown
                            ExposedDropdownMenuBox(
                                expanded = kbdExpanded,
                                onExpandedChange = { kbdExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedKbdMap.ifEmpty {
                                        uiState.kbdmapChoices.entries.firstOrNull { it.value == selectedKbdMap }?.key
                                            ?: selectedKbdMap
                                    },
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Keyboard Map") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = kbdExpanded)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = kbdExpanded,
                                    onDismissRequest = { kbdExpanded = false }
                                ) {
                                    uiState.kbdmapChoices.forEach { (key, name) ->
                                        DropdownMenuItem(
                                            text = { Text("$key ($name)") },
                                            onClick = {
                                                selectedKbdMap = key
                                                kbdExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // ── Other section ──
                        EditFormSection(title = "Other", icon = Icons.Default.Security) {
                            ToggleRow(
                                label = "DS Auth",
                                description = "Directory services authentication",
                                checked = dsAuth,
                                onCheckedChange = { dsAuth = it }
                            )
                        }

                        // ── Action buttons ──
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onNavigateBack,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Cancel", fontWeight = FontWeight.SemiBold)
                            }
                            Button(
                                onClick = {
                                    val settings = buildSettings(
                                        uiPort = uiPort,
                                        uiHttpsPort = uiHttpsPort,
                                        uiHttpsRedirect = uiHttpsRedirect,
                                        uiConsoleMsg = uiConsoleMsg,
                                        uiXFrameOptions = uiXFrameOptions,
                                        uiAddress = uiAddressText,
                                        uiV6Address = uiV6AddressText,
                                        uiAllowlist = uiAllowlistText,
                                        timezone = selectedTimezone,
                                        kbdMap = selectedKbdMap,
                                        httpsProtocols = selectedHttpsProtocols,
                                        dsAuth = dsAuth
                                    )
                                    vm.updateSettings(settings)
                                },
                                enabled = !uiState.isSaving,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                if (uiState.isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = colorScheme.onPrimary
                                    )
                                } else {
                                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                                }
                                Spacer(Modifier.width(8.dp))
                                Text("Save", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun RollbackInfoCard(
    uiState: GeneralSystemSettingsUiState,
    vm: GeneralSystemSettingsViewModel
) {
    if (uiState.checkinWaiting != null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.Top,
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
                        Icons.Default.Info, null,
                        tint = colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "UI changes require a restart",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onTertiaryContainer
                    )
                    Text(
                        "Changes to web interface settings require a UI restart to take effect. " +
                                "A rollback timeout can be set to automatically revert if the new config is unreachable.",
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

private fun buildSettings(
    uiPort: String,
    uiHttpsPort: String,
    uiHttpsRedirect: Boolean,
    uiConsoleMsg: Boolean,
    uiXFrameOptions: String,
    uiAddress: String,
    uiV6Address: String,
    uiAllowlist: String,
    timezone: String,
    kbdMap: String,
    httpsProtocols: String,
    dsAuth: Boolean
): System.SystemGeneralUpdateArgs {
    return System.SystemGeneralUpdateArgs(
        uiPort = uiPort.toIntOrNull(),
        uiHttpsPort = uiHttpsPort.toIntOrNull(),
        uiHttpsRedirect = uiHttpsRedirect,
        uiConsoleMsg = uiConsoleMsg,
        uiXFrameOptions = uiXFrameOptions.ifBlank { null },
        uiAddress = uiAddress.split(",").map { it.trim() }.filter { it.isNotBlank() }.ifEmpty { null },
        uiV6Address = uiV6Address.split(",").map { it.trim() }.filter { it.isNotBlank() }.ifEmpty { null },
        uiAllowlist = uiAllowlist.split(",").map { it.trim() }.filter { it.isNotBlank() }.ifEmpty { null },
        timezone = timezone.ifBlank { null },
        kbdMap = kbdMap.ifBlank { null },
        uiHttpsProtocols = httpsProtocols.split(",").map { it.trim() }.filter { it.isNotBlank() }.ifEmpty { null },
        dsAuth = dsAuth
    )
}

@Composable
private fun EditFormSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceContainerLow
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
                        .background(colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon, null,
                        tint = colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )
            }
            HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.3f))
            content()
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurface
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colorScheme.onPrimary,
                checkedTrackColor = colorScheme.primary
            )
        )
    }
}
