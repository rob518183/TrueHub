package com.imnotndesh.truehub.ui.homepage.instancesettings.network

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkEditScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {}
) {
    val vm: NetworkConfigViewModel = viewModel(
        factory = NetworkConfigViewModel.ViewModelFactory(manager)
    )
    val uiState by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { vm.loadAll() }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Network configuration saved")
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError()
        }
    }

    val config = uiState.config

    var hostname by remember(config) { mutableStateOf(config?.hostname ?: "") }
    var domain by remember(config) { mutableStateOf(config?.domain ?: "") }
    var ipv4Gateway by remember(config) { mutableStateOf(config?.ipv4Gateway ?: "") }
    var ipv6Gateway by remember(config) { mutableStateOf(config?.ipv6Gateway ?: "") }
    var nameserver1 by remember(config) { mutableStateOf(config?.nameserver1 ?: "") }
    var nameserver2 by remember(config) { mutableStateOf(config?.nameserver2 ?: "") }
    var nameserver3 by remember(config) { mutableStateOf(config?.nameserver3 ?: "") }
    var httpProxy by remember(config) { mutableStateOf(config?.httpProxy ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Network") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = hostname,
                onValueChange = { hostname = it },
                label = { Text("Hostname") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = domain,
                onValueChange = { domain = it },
                label = { Text("Domain") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = ipv4Gateway,
                onValueChange = { ipv4Gateway = it },
                label = { Text("IPv4 Gateway") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = ipv6Gateway,
                onValueChange = { ipv6Gateway = it },
                label = { Text("IPv6 Gateway") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = nameserver1,
                onValueChange = { nameserver1 = it },
                label = { Text("Nameserver 1") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = nameserver2,
                onValueChange = { nameserver2 = it },
                label = { Text("Nameserver 2") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = nameserver3,
                onValueChange = { nameserver3 = it },
                label = { Text("Nameserver 3") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = httpProxy,
                onValueChange = { httpProxy = it },
                label = { Text("HTTP Proxy") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                    Text("Discard")
                }
                Button(
                    onClick = {
                        vm.updateConfig(
                            System.NetworkConfigurationUpdateArgs(
                                hostname = hostname.ifBlank { null },
                                domain = domain.ifBlank { null },
                                ipv4Gateway = ipv4Gateway.ifBlank { null },
                                ipv6Gateway = ipv6Gateway.ifBlank { null },
                                nameserver1 = nameserver1.ifBlank { null },
                                nameserver2 = nameserver2.ifBlank { null },
                                nameserver3 = nameserver3.ifBlank { null },
                                httpProxy = httpProxy.ifBlank { null }
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                    Text("Save")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
