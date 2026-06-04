package com.imnotndesh.truehub.ui.services.apps.details.rollback

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.star
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.JobRepository
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RollbackVersionScreen(
    appName: String,
    versions: List<String>,
    isLoadingVersions: Boolean,
    fetchError: String?,
    manager: TrueNASApiManager,
    onConfirmRollback: (version: String, rollbackSnapshot: Boolean) -> Unit,
    onNavigateBack: () -> Unit
) {
    val activeJobs by JobRepository.activeJobs.collectAsState()
    val liveJob = activeJobs.values.find { it.appName == appName }

    val isRollingBack = liveJob != null && liveJob.state !in listOf("SUCCESS", "FAILED", "ABORTED")
    val isDone = liveJob?.state == "SUCCESS"
    val isFailed = liveJob?.state in listOf("FAILED", "ABORTED")

    var selectedVersion by remember { mutableStateOf<String?>(null) }
    var rollbackSnapshot by remember { mutableStateOf(true) }

    LaunchedEffect(isDone) {
        if (isDone) {
            kotlinx.coroutines.delay(1800)
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            UnifiedScreenHeader(
                title = "Rollback $appName",
                subtitle = if (isRollingBack) "In Progress" else "Select Previous Version",
                isLoading = isLoadingVersions,
                isRefreshing = false,
                error = fetchError,
                onDismissError = {},
                manager = manager,
                onBackPressed = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        AnimatedContent(
            targetState = isRollingBack || isDone || isFailed,
            transitionSpec = {
                (fadeIn(tween(500)) + slideInVertically { it / 4 })
                    .togetherWith(fadeOut(tween(300)) + slideOutVertically { -it / 4 })
            },
            label = "rollback_screen_state"
        ) { showingProgress ->
            if (showingProgress) {
                RollingBackView(
                    appName = appName,
                    targetVersion = selectedVersion ?: "Unknown",
                    progress = liveJob?.progress ?: 0,
                    description = liveJob?.description,
                    isDone = isDone,
                    isFailed = isFailed,
                    onNavigateBack = onNavigateBack,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            } else {
                VersionSelectionView(
                    versions = versions,
                    isLoading = isLoadingVersions,
                    error = fetchError,
                    selectedVersion = selectedVersion,
                    rollbackSnapshot = rollbackSnapshot,
                    onVersionSelected = { selectedVersion = it },
                    onRollbackSnapshotChange = { rollbackSnapshot = it },
                    onConfirm = { selectedVersion?.let { onConfirmRollback(it, rollbackSnapshot) } },
                    onNavigateBack = onNavigateBack,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun VersionSelectionView(
    versions: List<String>,
    isLoading: Boolean,
    error: String?,
    selectedVersion: String?,
    rollbackSnapshot: Boolean,
    onVersionSelected: (String) -> Unit,
    onRollbackSnapshotChange: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        when {
            isLoading -> {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            error != null -> {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Failed to load versions:\n$error",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            versions.isEmpty() -> {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No previous versions available to rollback to.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(versions) { version ->
                        val isSelected = version == selectedVersion
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onVersionSelected(version) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.Check else Icons.Default.History,
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = version,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rollback options
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = "Rollback Snapshot",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Restore the dataset snapshot taken at the time of upgrade",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = rollbackSnapshot,
                            onCheckedChange = onRollbackSnapshotChange
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancel")
            }
            Button(
                onClick = onConfirm,
                enabled = selectedVersion != null && !isLoading && error == null,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Icon(Icons.Default.Restore, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Rollback")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RollingBackView(
    appName: String,
    targetVersion: String,
    progress: Int,
    description: String?,
    isDone: Boolean,
    isFailed: Boolean,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.toFloat() / 100f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "smoothProgress"
    )

    val secondaryColor = MaterialTheme.colorScheme.secondary
    val containerColor = MaterialTheme.colorScheme.primaryContainer

    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            MorphingM3Background(
                color = containerColor.copy(alpha = 0.6f),
                modifier = Modifier.size(140.dp),
                isClockwise = false
            )
            MorphingM3Background(
                color = secondaryColor.copy(alpha = 0.3f),
                modifier = Modifier.size(100.dp),
                isClockwise = true
            )
            AnimatedContent(
                targetState = when {
                    isDone -> "done"
                    isFailed -> "failed"
                    else -> "running"
                },
                transitionSpec = {
                    (scaleIn(tween(400)) + fadeIn(tween(400))) togetherWith
                            (scaleOut(tween(250)) + fadeOut(tween(250)))
                },
                label = "center_icon"
            ) { state ->
                when (state) {
                    "done" -> Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp)
                    )
                    "failed" -> Icon(
                        Icons.Default.Close,
                        contentDescription = "Failed",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(56.dp)
                    )
                    else -> CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp,
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedContent(
            targetState = when {
                isDone -> "done"
                isFailed -> "failed"
                else -> "running"
            },
            transitionSpec = {
                fadeIn(tween(400)) togetherWith fadeOut(tween(200))
            },
            label = "status_text"
        ) { state ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = when (state) {
                        "done" -> "Rollback Complete"
                        "failed" -> "Rollback Failed"
                        else -> "Rolling back $appName"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = when (state) {
                        "failed" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when (state) {
                        "done" -> "Successfully restored to $targetVersion"
                        "failed" -> "Something went wrong during the rollback."
                        else -> description ?: "Restoring to $targetVersion..."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        AnimatedVisibility(
            visible = !isDone && !isFailed,
            enter = fadeIn(tween(400)),
            exit = fadeOut(tween(300))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                LinearWavyProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$progress%",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        AnimatedVisibility(
            visible = isFailed,
            enter = fadeIn(tween(400)),
            exit = fadeOut(tween(300))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onNavigateBack,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Go Back")
                }
            }
        }
    }
}

private fun Morph.toComposePath(progress: Float, path: Path = Path()): Path {
    var first = true
    path.rewind()
    forEachCubic(progress) { bezier ->
        if (first) {
            path.moveTo(bezier.anchor0X, bezier.anchor0Y)
            first = false
        }
        path.cubicTo(
            bezier.control0X, bezier.control0Y,
            bezier.control1X, bezier.control1Y,
            bezier.anchor1X, bezier.anchor1Y
        )
    }
    path.close()
    return path
}

@Composable
private fun MorphingM3Background(
    color: Color,
    modifier: Modifier = Modifier,
    isClockwise: Boolean = true
) {
    val starPolygon = remember {
        RoundedPolygon.star(
            numVerticesPerRadius = 8,
            radius = 1f,
            innerRadius = 0.5f,
            rounding = CornerRounding(radius = 0.15f)
        )
    }
    val circlePolygon = remember { RoundedPolygon.circle(numVertices = 8) }
    val morph = remember { Morph(starPolygon, circlePolygon) }
    val path = remember { Path() }

    val infiniteTransition = rememberInfiniteTransition(label = "morph_transition")
    val morphProgress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "morph_progress"
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = if (isClockwise) 360f else -360f,
        animationSpec = infiniteRepeatable(tween(15000, easing = LinearEasing), RepeatMode.Restart),
        label = "slow_rotation"
    )

    Canvas(modifier = modifier) {                        // no graphicsLayer here
        val scaleFactor = size.minDimension / 2f         // was size.width/2f — wrong for non-square
        translate(left = size.width / 2f, top = size.height / 2f) {
            rotate(degrees = rotation) {                  // rotation inside draw scope only
                scale(scaleX = scaleFactor, scaleY = scaleFactor) {
                    drawPath(path = morph.toComposePath(morphProgress, path), color = color)
                }
            }
        }
    }
}