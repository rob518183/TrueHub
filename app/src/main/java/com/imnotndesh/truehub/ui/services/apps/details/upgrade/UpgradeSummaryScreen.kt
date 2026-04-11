package com.imnotndesh.truehub.ui.services.apps.details.upgrade

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.JobRepository
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UpgradeSummaryScreen(
    appName: String,
    summary: Apps.AppUpgradeSummaryResult,
    manager: TrueNASApiManager,
    onConfirmUpgrade: () -> Unit,
    onNavigateBack: () -> Unit
) {

    val activeJobs by JobRepository.activeJobs.collectAsState()
    val liveJob = activeJobs.values.find { it.appName == appName }

    val isUpgrading = liveJob != null && liveJob.state !in listOf("SUCCESS", "FAILED", "ABORTED")
    val isDone = liveJob?.state == "SUCCESS"
    val isFailed = liveJob?.state in listOf("FAILED", "ABORTED")

    LaunchedEffect(isDone) {
        if (isDone) {
            kotlinx.coroutines.delay(1800)
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            UnifiedScreenHeader(
                title = "Upgrade $appName",
                subtitle = if (isUpgrading) "In Progress" else "Review Changes",
                isLoading = false,
                isRefreshing = false,
                error = null,
                onDismissError = {},
                manager = manager,
                onBackPressed = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        AnimatedContent(
            targetState = isUpgrading || isDone || isFailed,
            transitionSpec = {
                (fadeIn(tween(500)) + slideInVertically { it / 4 })
                    .togetherWith(fadeOut(tween(300)) + slideOutVertically { -it / 4 })
            },
            label = "upgrade_screen_state"
        ) { showingProgress ->
            if (showingProgress) {
                UpgradingView(
                    appName = appName,
                    progress = liveJob?.progress ?: 0,
                    description = liveJob?.description,
                    isDone = isDone,
                    isFailed = isFailed,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            } else {
                ReviewView(
                    summary = summary,
                    onConfirmUpgrade = onConfirmUpgrade,
                    onNavigateBack = onNavigateBack,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun UpgradingView(
    appName: String,
    progress: Int,
    description: String?,
    isDone: Boolean,
    isFailed: Boolean,
    modifier: Modifier = Modifier
) {

    val infiniteTransition = rememberInfiniteTransition(label = "blob")
    val blob1Scale by infiniteTransition.animateFloat(
        initialValue = 0.85f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ), label = "b1"
    )
    val blob2Scale by infiniteTransition.animateFloat(
        initialValue = 1.1f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ), label = "b2"
    )
    val blob3Scale by infiniteTransition.animateFloat(
        initialValue = 0.9f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ), label = "b3"
    )
    val blobRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(12000, easing = LinearEasing), RepeatMode.Restart
        ), label = "rot"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val containerColor = MaterialTheme.colorScheme.primaryContainer

    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(blob1Scale)
                    .graphicsLayer { rotationZ = blobRotation * 0.3f }
                    .clip(RoundedCornerShape(60.dp))
                    .background(containerColor)
            )

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(blob2Scale)
                    .graphicsLayer { rotationZ = -blobRotation * 0.5f }
                    .clip(RoundedCornerShape(
                        topStart = 40.dp, topEnd = 20.dp,
                        bottomStart = 20.dp, bottomEnd = 40.dp
                    ))
                    .background(secondaryColor.copy(alpha = 0.25f))
            )

            Box(
                modifier = Modifier
                    .size(68.dp)
                    .scale(blob3Scale)
                    .graphicsLayer { rotationZ = blobRotation * 0.7f }
                    .clip(RoundedCornerShape(
                        topStart = 28.dp, topEnd = 12.dp,
                        bottomStart = 16.dp, bottomEnd = 28.dp
                    ))
                    .background(primaryColor.copy(alpha = 0.18f))
            )

            AnimatedContent(
                targetState = when {
                    isDone -> "done"
                    isFailed -> "failed"
                    else -> "running"
                },
                transitionSpec = {
                    scaleIn(tween(400)) + fadeIn(tween(400)) togetherWith
                            scaleOut(tween(250)) + fadeOut(tween(250))
                },
                label = "center_icon"
            ) { state ->
                when (state) {
                    "done" -> Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    "failed" -> Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(40.dp)
                    )
                    else -> CircularProgressIndicator(
                        modifier = Modifier.size(0.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp

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
                        "done" -> "Upgrade Complete"
                        "failed" -> "Upgrade Failed"
                        else -> "Upgrading $appName"
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
                        "done" -> "All done! Taking you back..."
                        "failed" -> "Something went wrong during the upgrade"
                        else -> description ?: "Please wait, this may take a moment"
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
                    progress = { progress.toFloat() / 100f },
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
        AnimatedVisibility(visible = isFailed) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { /* onNavigateBack passed via outer scope */ },
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Go Back")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ReviewView(
    summary: Apps.AppUpgradeSummaryResult,
    onConfirmUpgrade: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Current",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = summary.latest_human_version,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "New",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = summary.upgrade_human_version,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Changelog",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    if (!summary.changelog.isNullOrBlank()) {
                        val cachedMarkdown = remember(summary.changelog) { summary.changelog }
                        MarkdownText(
                            markdown = cachedMarkdown,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Text(
                            text = "No changelog available for this update.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onNavigateBack,
                modifier = Modifier.weight(1f).height(50.dp),
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
                onClick = onConfirmUpgrade,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upgrade")
            }
        }
    }
}