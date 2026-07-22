package com.imnotndesh.truehub.ui.homepage.instancesettings.alertsservice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Alerts
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertClassesConfigScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var config by remember { mutableStateOf<Alerts.AlertClassesEntry?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    val editedLevels = remember { mutableStateMapOf<String, Alerts.AlertLevels>() }
    val editedPolicies = remember { mutableStateMapOf<String, Alerts.AlertPolicies>() }
    val editedProactive = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(Unit) {
        isLoading = true
        when (val result = manager.alertsService.getAlertClassesConfigWithResult()) {
            is ApiResult.Success -> {
                config = result.data
                result.data.classes.forEach { (name, cls) ->
                    editedLevels[name] = cls.level
                    editedPolicies[name] = cls.policy
                    editedProactive[name] = cls.proactive_support
                }
            }
            is ApiResult.Error -> {
                snackbarHostState.showSnackbar("Error: ${result.message}")
            }
            else -> {}
        }
        isLoading = false
    }

    fun save() {
        scope.launch {
            isSaving = true
            val update = Alerts.AlertClassUpdate(
                classes = editedLevels.mapValues { (name, level) ->
                    Alerts.AlertClassConfiguration(
                        level = level,
                        policy = editedPolicies[name] ?: Alerts.AlertPolicies.IMMEDIATELY,
                        proactive_support = editedProactive[name] ?: true
                    )
                }
            )
            when (val result = manager.alertsService.updateAlertClassesWithResult(update)) {
                is ApiResult.Success -> {
                    snackbarHostState.showSnackbar("Configuration saved!")
                    config = result.data
                }
                is ApiResult.Error -> {
                    snackbarHostState.showSnackbar("Error: ${result.message}")
                }
                else -> {}
            }
            isSaving = false
        }
    }

    Scaffold(
        topBar = {
            UnifiedScreenHeader(
                title = "Alert Classes",
                subtitle = "Configure alert classes and notification policies",
                isLoading = isLoading,
                isRefreshing = false,
                error = null,
                onDismissError = {},
                manager = manager,
                onBackPressed = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(snackbarData = it) } },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (isLoading) {
            LoadingScreen("Loading Alert Classes")
        } else if (config != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(config!!.classes.keys.sorted(), key = { it }) { className ->
                        AlertClassConfigCard(
                            className = className,
                            level = editedLevels[className] ?: Alerts.AlertLevels.INFO,
                            policy = editedPolicies[className] ?: Alerts.AlertPolicies.IMMEDIATELY,
                            proactiveSupport = editedProactive[className] ?: true,
                            onLevelChanged = { editedLevels[className] = it },
                            onPolicyChanged = { editedPolicies[className] = it },
                            onProactiveChanged = { editedProactive[className] = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { save() },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save Changes")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlertClassConfigCard(
    className: String,
    level: Alerts.AlertLevels,
    policy: Alerts.AlertPolicies,
    proactiveSupport: Boolean,
    onLevelChanged: (Alerts.AlertLevels) -> Unit,
    onPolicyChanged: (Alerts.AlertPolicies) -> Unit,
    onProactiveChanged: (Boolean) -> Unit
) {
    var levelExpanded by remember { mutableStateOf(false) }
    var policyExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = className,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            // Level selector
            ExposedDropdownMenuBox(
                expanded = levelExpanded,
                onExpandedChange = { levelExpanded = it }
            ) {
                OutlinedTextField(
                    value = level.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Level") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = levelExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = levelExpanded,
                    onDismissRequest = { levelExpanded = false }
                ) {
                    Alerts.AlertLevels.entries.forEach { lvl ->
                        DropdownMenuItem(
                            text = { Text(lvl.name) },
                            onClick = { onLevelChanged(lvl); levelExpanded = false }
                        )
                    }
                }
            }

            // Policy selector
            ExposedDropdownMenuBox(
                expanded = policyExpanded,
                onExpandedChange = { policyExpanded = it }
            ) {
                OutlinedTextField(
                    value = policy.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Policy") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = policyExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = policyExpanded,
                    onDismissRequest = { policyExpanded = false }
                ) {
                    Alerts.AlertPolicies.entries.forEach { pol ->
                        DropdownMenuItem(
                            text = { Text(pol.name) },
                            onClick = { onPolicyChanged(pol); policyExpanded = false }
                        )
                    }
                }
            }

            // Proactive support toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Proactive Support",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = proactiveSupport,
                    onCheckedChange = onProactiveChanged
                )
            }
        }
    }
}
