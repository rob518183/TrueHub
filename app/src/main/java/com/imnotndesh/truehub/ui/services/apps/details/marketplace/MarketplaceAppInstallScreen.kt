package com.imnotndesh.truehub.ui.services.apps.details.marketplace

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.JobRepository
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.ui.services.apps.AppsScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceAppInstallScreen(
    appName: String,
    train: String,
    viewModel: AppsScreenViewModel,
    onBack: () -> Unit,
    onInstallSuccess: () -> Unit,
    manager: TrueNASApiManager
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val isInstalling by uiState.isInstalling.collectAsStateWithLifecycle()
    val installError by uiState.installError.collectAsStateWithLifecycle()
    val installJobId by uiState.installJobId.collectAsStateWithLifecycle()

    val activeJobsMap by JobRepository.activeJobs.collectAsStateWithLifecycle()
    val trackedJob = installJobId?.let { gid -> activeJobsMap[gid] }

    LaunchedEffect(trackedJob) {
        if (trackedJob != null) {
            when (trackedJob.state.uppercase()) {
                "SUCCESS" -> {
                    viewModel.clearInstallState()
                    onInstallSuccess()
                }
                "FAILED", "ABORTED" -> {
                }
            }
        }
    }

    LaunchedEffect(appName) {
        viewModel.loadCatalogAppDetails(appName, train)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearCatalogAppDetails() }
    }

    val isLoadingDetails = uiState.isLoadingCatalogDetails && uiState.catalogAppDetails == null

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when {
                isLoadingDetails -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            "Loading configurations…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                uiState.catalogDetailsError != null && uiState.catalogAppDetails == null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text("Failed to load schema", style = MaterialTheme.typography.titleMedium)
                        Button(onClick = { viewModel.loadCatalogAppDetails(appName, train) }) {
                            Text("Retry")
                        }
                    }
                }

                uiState.catalogAppDetails != null -> {
                    val details = uiState.catalogAppDetails!!
                    var selectedVersionKey by remember(details) {
                        mutableStateOf(details.versions?.keys?.firstOrNull() ?: "")
                    }
                    val versionData = details.versions?.get(selectedVersionKey)

                    InstallOptionsContent(
                        details = details,
                        versionData = versionData,
                        versionsMap = details.versions ?: emptyMap(),
                        selectedVersionKey = selectedVersionKey,
                        onVersionSelected = { selectedVersionKey = it },
                        onBack = onBack,
                        onInstallAction = { targetedInstanceName, flatMapValues ->
                            viewModel.installMarketplaceApp(
                                context = context,
                                appName = targetedInstanceName,
                                catalogApp = details.name,
                                train = train,
                                version = selectedVersionKey,
                                flatValues = flatMapValues,
                                unflattenMapFunc = ::unflattenMap
                            )
                        }
                    )
                }
            }

            if (isInstalling || installError != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (installError != null) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Installation Failed",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = installError ?: "Unknown deployment exception occurred.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.clearInstallState() },
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Dismiss Layout")
                            }
                        } else {
                            val progressValue = (trackedJob?.progress ?: 0) / 100f
                            val progressPercent = trackedJob?.progress ?: 0
                            val currentStepDescription = trackedJob?.description ?: "Initializing system middleware provisioning..."

                            CircularProgressIndicator(
                                progress = { progressValue },
                                modifier = Modifier.size(84.dp),
                                strokeWidth = 8.dp,
                                trackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Deploying Application",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "$progressPercent%",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentStepDescription,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InstallOptionsContent(
    details: Apps.CatalogAppDetails,
    versionData: Apps.CatalogAppVersion?,
    versionsMap: Map<String, Apps.CatalogAppVersion>,
    selectedVersionKey: String,
    onVersionSelected: (String) -> Unit,
    onBack: () -> Unit,
    onInstallAction: (appName: String, values: Map<String, Any?>) -> Unit
) {
    val schema = versionData?.schema
    val defaults = versionData?.values ?: emptyMap()

    val formState = remember(versionData) {
        mutableStateMapOf<String, Any?>().also { map ->
            flattenDefaults(defaults, map)
        }
    }

    var targetedAppNameInstance by remember(details) { mutableStateOf(details.name) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            InstallHeroHeader(
                title = details.title ?: details.name,
                onBack = onBack
            )
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Application Instance Name",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = targetedAppNameInstance,
                    onValueChange = { targetedAppNameInstance = it.lowercase() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Label, null, modifier = Modifier.size(18.dp)) },
                    placeholder = { Text("e.g. wireguard") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                val categories = details.categories
                if (!categories.isNullOrEmpty()) {
                    InstallFlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categories.forEach { cat ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(cat.replaceFirstChar { it.uppercase() }) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                border = null,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (versionsMap.isNotEmpty()) {
                    Text(
                        text = "App Target Version",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    var versionDropdownExpanded by remember { mutableStateOf(false) }

                    @OptIn(ExperimentalMaterial3Api::class)
                    ExposedDropdownMenuBox(
                        expanded = versionDropdownExpanded,
                        onExpandedChange = { versionDropdownExpanded = !versionDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedVersionKey,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = versionDropdownExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            supportingText = versionData?.humanVersion?.let {
                                { Text("Build Config Version: $it") }
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = versionDropdownExpanded,
                            onDismissRequest = { versionDropdownExpanded = false }
                        ) {
                            versionsMap.keys.forEach { versionKey ->
                                val currentData = versionsMap[versionKey]
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(versionKey, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                            currentData?.humanVersion?.let {
                                                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    },
                                    onClick = {
                                        onVersionSelected(versionKey)
                                        versionDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        if (schema?.groups != null && schema.questions != null) {
            item {
                Text(
                    text = "Configuration Setup",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                Spacer(Modifier.height(4.dp))
            }

            schema.groups.forEach { group ->
                val groupQuestions = schema.questions.filter { it.group == group.name }
                if (groupQuestions.isNotEmpty()) {
                    item(key = group.name) {
                        SchemaGroupSection(
                            group = group,
                            questions = groupQuestions,
                            formState = formState,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            InstallBottomAction(
                title = details.title ?: details.name,
                onInstall = { onInstallAction(targetedAppNameInstance, formState.toMap()) }
            )
        }
    }
}

@Composable
private fun InstallHeroHeader(title: String, onBack: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Deployment Wizard",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// Replaces the previous SchemaGroupCard - no Card composable, just a styled Surface
@Composable
private fun SchemaGroupSection(
    group: Apps.SchemaGroup,
    questions: List<Apps.SchemaQuestion>,
    formState: MutableMap<String, Any?>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        group.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    group.description?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle"
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    questions.forEachIndexed { idx, question ->
                        QuestionRow(
                            question = question,
                            formState = formState,
                            pathPrefix = question.variable
                        )
                        if (idx < questions.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionRow(
    question: Apps.SchemaQuestion,
    formState: MutableMap<String, Any?>,
    pathPrefix: String
) {
    val schema = question.schema ?: return
    if (schema.hidden == true) return

    val label = question.label?.ifBlank { question.variable } ?: question.variable
    val isRequired = schema.required == true
    val isNullable = schema.`null` == true

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
            if (isRequired) {
                Text(" *", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
            }
            if (isNullable) {
                Spacer(Modifier.width(4.dp))
                Text(
                    "optional",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        question.description?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        when (schema.type) {
            "string"   -> StringField(schema = schema, path = pathPrefix, formState = formState)
            "int"      -> IntField(schema = schema, path = pathPrefix, formState = formState)
            "boolean"  -> BooleanField(schema = schema, path = pathPrefix, formState = formState)
            "dict"     -> DictField(schema = schema, pathPrefix = pathPrefix, formState = formState)
            "list"     -> ListFieldInfo()
            "path",
            "hostpath" -> PathField(path = pathPrefix, formState = formState)
            else       -> DefaultField(type = schema.type, path = pathPrefix, formState = formState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StringField(schema: Apps.SchemaDefinition, path: String, formState: MutableMap<String, Any?>) {
    val isPrivate = schema.private == true

    if (!schema.enum.isNullOrEmpty()) {
        var dropdownExpanded by remember { mutableStateOf(false) }
        val currentVal = (formState[path] ?: schema.default)?.toString() ?: schema.enum.firstOrNull()?.value?.toString() ?: ""
        val displayText = schema.enum.find { it.value?.toString() == currentVal }?.description ?: currentVal

        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = !dropdownExpanded }
        ) {
            OutlinedTextField(
                value = displayText,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                schema.enum.forEach { option ->
                    val optionValue = option.value?.toString() ?: ""
                    val optionLabel = option.description ?: optionValue
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(optionLabel, style = MaterialTheme.typography.bodyMedium)
                                if (optionLabel != optionValue && optionValue.isNotBlank()) {
                                    Text(optionValue, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        },
                        onClick = {
                            formState[path] = optionValue
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }
    } else {
        var text by remember { mutableStateOf((formState[path] ?: schema.default)?.toString() ?: "") }
        var passwordVisible by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = text,
            onValueChange = { text = it; formState[path] = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter token parameter...") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (isPrivate && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = if (isPrivate) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Visibility"
                        )
                    }
                }
            } else null
        )
    }
}

@Composable
private fun IntField(schema: Apps.SchemaDefinition, path: String, formState: MutableMap<String, Any?>) {
    var text by remember { mutableStateOf((formState[path] ?: schema.default)?.toString()?.takeIf { it != "null" } ?: "") }
    val supportText = when {
        schema.min != null && schema.max != null -> "Bounds: ${schema.min} – ${schema.max}"
        schema.min != null -> "Minimum required: ${schema.min}"
        schema.max != null -> "Maximum allocation: ${schema.max}"
        else -> null
    }
    OutlinedTextField(
        value = text,
        onValueChange = { v -> text = v; formState[path] = v.toIntOrNull() },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        placeholder = { Text("Enter dynamic port or capacity integer...") },
        supportingText = supportText?.let { msg -> { Text(msg, style = MaterialTheme.typography.labelSmall) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
private fun BooleanField(schema: Apps.SchemaDefinition, path: String, formState: MutableMap<String, Any?>) {
    var checked by remember { mutableStateOf((formState[path] ?: schema.default) as? Boolean ?: false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Switch(checked = checked, onCheckedChange = { checked = it; formState[path] = it })
        Text(if (checked) "Active" else "Inactive", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DictField(schema: Apps.SchemaDefinition, pathPrefix: String, formState: MutableMap<String, Any?>) {
    if (schema.attrs.isNullOrEmpty()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(16.dp))
            .padding(end = 12.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(IntrinsicSize.Max)
                .align(Alignment.CenterVertically)
                .background(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), shape = RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            schema.attrs.forEachIndexed { idx, attr ->
                if (attr.schema?.hidden != true) {
                    QuestionRow(question = attr, formState = formState, pathPrefix = "$pathPrefix.${attr.variable}")
                    if (idx < schema.attrs.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PathField(path: String, formState: MutableMap<String, Any?>) {
    var text by remember { mutableStateOf(formState[path]?.toString() ?: "") }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it; formState[path] = it },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        placeholder = { Text("/mnt/dataset/appdata") },
        leadingIcon = { Icon(Icons.Default.Folder, null, modifier = Modifier.size(18.dp)) }
    )
}

@Composable
private fun ListFieldInfo() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), shape = RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.AutoMirrored.Filled.List, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Column {
            Text("Collection Parameters Configuration", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Appends natively via backend blocks", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun DefaultField(type: String?, path: String, formState: MutableMap<String, Any?>) {
    var text by remember { mutableStateOf(formState[path]?.toString() ?: "") }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it; formState[path] = it },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        placeholder = { Text("($type) raw syntax entry...") }
    )
}

@Composable
private fun InstallBottomAction(title: String, onInstall: () -> Unit) {
    Surface(
        tonalElevation = 0.dp,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Button(
            onClick = onInstall,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Icon(Icons.Default.Download, null)
            Spacer(Modifier.width(8.dp))
            Text("Deploy instance: $title", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
        }
    }
}

private fun flattenDefaults(source: Map<String, Any?>, target: MutableMap<String, Any?>, prefix: String = "") {
    source.forEach { (key, value) ->
        val path = if (prefix.isBlank()) key else "$prefix.$key"
        @Suppress("UNCHECKED_CAST")
        if (value is Map<*, *>) {
            flattenDefaults(value as Map<String, Any?>, target, path)
        } else {
            target[path] = value
        }
    }
}

private fun unflattenMap(flatMap: Map<String, Any?>): Map<String, Any?> {
    val result = mutableMapOf<String, Any?>()
    for ((key, value) in flatMap) {
        if (value == null) continue
        val parts = key.split(".")
        var currentMap = result
        for (i in 0 until parts.lastIndex) {
            val part = parts[i]
            @Suppress("UNCHECKED_CAST")
            val nextMap = currentMap[part] as? MutableMap<String, Any?>
                ?: mutableMapOf<String, Any?>().also { currentMap[part] = it }
            currentMap = nextMap
        }
        currentMap[parts[parts.lastIndex]] = value
    }
    return result
}

@Composable
private fun InstallFlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(content = content, modifier = modifier) { measurables, constraints ->
        val rowItems = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentWidth = 0

        rowItems.forEach { item ->
            if (currentWidth + item.width > constraints.maxWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                currentWidth = 0
            }
            currentRow.add(item)
            currentWidth += item.width + horizontalArrangement.spacing.roundToPx()
        }
        if (currentRow.isNotEmpty()) rows.add(currentRow)

        val rowHeights = rows.map { row -> row.maxOfOrNull { it.height } ?: 0 }
        val totalHeight = rowHeights.sum() + verticalArrangement.spacing.roundToPx() * (rows.size - 1).coerceAtLeast(0)

        layout(constraints.maxWidth, totalHeight.coerceAtMost(constraints.maxHeight)) {
            var y = 0
            rows.forEachIndexed { index, row ->
                var x = 0
                row.forEach { item ->
                    item.placeRelative(x, y)
                    x += item.width + horizontalArrangement.spacing.roundToPx()
                }
                y += rowHeights[index] + verticalArrangement.spacing.roundToPx()
            }
        }
    }
}