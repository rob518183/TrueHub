package com.imnotndesh.truehub.ui.services.apps.details.appdetails

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.services.apps.AppsScreenViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun AppConfigScreen(
    manager: TrueNASApiManager,
    appName: String,
    onNavigateBack: () -> Unit
) {
    val viewModel: AppsScreenViewModel = viewModel(
        factory = AppsScreenViewModel.AppsScreenViewModelFactory(manager)
    )
    val configState by viewModel.appConfigState.collectAsState()
    val scope = rememberCoroutineScope()
    val editedConfig = remember { mutableStateMapOf<String, Any?>() }

    LaunchedEffect(configState.config) {
        configState.config?.let { original ->
            editedConfig.clear()
            original.forEach { (key, value) ->
                editedConfig[key] = deepCopy(value)
            }
        }
    }

    LaunchedEffect(appName) {
        viewModel.loadAppConfig(appName)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        UnifiedScreenHeader(
            title = "App Configuration",
            subtitle = appName,
            isLoading = configState.isLoading,
            isRefreshing = false,
            error = configState.error,
            onDismissError = { viewModel.clearAppConfigError() },
            manager = manager,
            onBackPressed = onNavigateBack
        )

        when {
            configState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            configState.config == null && configState.error == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No configuration available")
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(editedConfig.keys.toList()) { key ->
                        ConfigEditorRow(
                            key = key,
                            value = editedConfig[key],
                            onValueChange = { newValue ->
                                editedConfig[key] = newValue
                            }
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    configState.config?.let { orig ->
                                        editedConfig.clear()
                                        orig.forEach { (k, v) -> editedConfig[k] = deepCopy(v) }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Cancel, contentDescription = "Cancel")
                                Spacer(modifier = Modifier.size(4.dp))
                                Text("Cancel changes")
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        viewModel.updateAppConfig(appName, editedConfig.toMap())
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !configState.isSaving
                            ) {
                                if (configState.isSaving) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                } else {
                                    Icon(Icons.Default.Save, contentDescription = "Save")
                                    Spacer(modifier = Modifier.size(4.dp))
                                    Text("Save changes")
                                }
                            }
                        }
                    }

                    if (configState.saveSuccess) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Text(
                                        text = if (configState.saveJobId != null) "Update started (Job ID: ${configState.saveJobId})" else "Update submitted",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}
// TODO: Figure out where else to obtain the values for config but possible areas might be from catalog
/**
 * Recursive composable that displays an editable field for any JSON-like value.
 */
@Composable
private fun ConfigEditorRow(
    key: String,
    value: Any?,
    onValueChange: (Any?) -> Unit,
    modifier: Modifier = Modifier,
    depth: Int = 0
) {
    val shape = RoundedCornerShape(16.dp)
    val backgroundColor = if (depth == 0) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Section header (key)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = key,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (depth > 0) {
                    // For nested fields, show a small indicator
                    Text(
                        text = "nested",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Value editor based on type
            when (value) {
                is Map<*, *> -> {
                    // Recursively edit each entry in the map
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        value.forEach { (subKey, subValue) ->
                            ConfigEditorRow(
                                key = subKey.toString(),
                                value = subValue,
                                onValueChange = { newSubValue ->
                                    // Update the nested map
                                    val updatedMap = (value as Map<String, Any?>).toMutableMap()
                                    updatedMap[subKey.toString()] = newSubValue
                                    onValueChange(updatedMap)
                                },
                                depth = depth + 1
                            )
                        }
                    }
                }
                is List<*> -> {
                    // For simplicity, treat lists as editable text (JSON representation)
                    var listText by remember(value) { mutableStateOf(value.joinToString(separator = ", ")) }
                    OutlinedTextField(
                        value = listText,
                        onValueChange = { newText ->
                            listText = newText
                            onValueChange(newText.split(",").map { it.trim() })
                        },
                        label = { Text("List (comma separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false
                    )
                }
                is Boolean -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enabled", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = value,
                            onCheckedChange = onValueChange
                        )
                    }
                }
                is Number -> {
                    OutlinedTextField(
                        value = value.toString(),
                        onValueChange = { newText ->
                            newText.toDoubleOrNull()?.let { num ->
                                // Preserve original type (Int/Long/Double)
                                when (value) {
                                    is Int -> onValueChange(num.toInt())
                                    is Long -> onValueChange(num.toLong())
                                    else -> onValueChange(num)
                                }
                            }
                        },
                        label = { Text(key) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                is String -> {
                    OutlinedTextField(
                        value = value,
                        onValueChange = onValueChange,
                        label = { Text(key) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                null -> {
                    OutlinedTextField(
                        value = "",
                        onValueChange = { newText -> onValueChange(newText.ifBlank { null }) },
                        label = { Text("$key (null)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                else -> {
                    // Fallback: show as string
                    var text by remember(value) { mutableStateOf(value.toString()) }
                    OutlinedTextField(
                        value = text,
                        onValueChange = { newText ->
                            text = newText
                            onValueChange(newText)
                        },
                        label = { Text(key) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun deepCopy(original: Any?): Any? {
    return when (original) {
        null -> null
        is Map<*, *> -> {
            original.mapValues { (_, v) -> deepCopy(v) }
        }
        is List<*> -> {
            original.map { deepCopy(it) }
        }
        else -> original
    }
}