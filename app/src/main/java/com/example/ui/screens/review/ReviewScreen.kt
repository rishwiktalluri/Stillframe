package com.example.ui.screens.review

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.critique.*
import com.example.data.model.SceneEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.StillframeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    viewModel: StillframeViewModel,
    modifier: Modifier = Modifier
) {
    val activeProject by viewModel.activeProject.collectAsState()
    val scenes by viewModel.scenes.collectAsState()
    val characters by viewModel.characters.collectAsState()
    val loreList by viewModel.loreList.collectAsState()
    val timelineEvents by viewModel.timelineEvents.collectAsState()

    val selectedSceneId by viewModel.selectedReviewSceneId.collectAsState()
    val selectedMode by viewModel.selectedCritiqueMode.collectAsState()
    val findings by viewModel.critiqueFindings.collectAsState()
    val metrics by viewModel.sceneMetrics.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()

    val modelState by viewModel.localModelManager.state.collectAsState()

    var showContextSheet by remember { mutableStateOf(false) }
    var showModelLifecycleDialog by remember { mutableStateOf(false) }

    val accentColor = remember(activeProject?.accentColorHex) {
        try { Color(android.graphics.Color.parseColor(activeProject?.accentColorHex ?: "#E8B058")) } catch (e: Exception) { AccentAmber }
    }

    val currentScene = remember(scenes, selectedSceneId) {
        scenes.firstOrNull { it.id == selectedSceneId } ?: scenes.firstOrNull()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = StillframeBlack,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StillframeBlack)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "EDITORIAL CRITIC",
                            style = StillframeTypography.titleLarge.copy(letterSpacing = 2.sp),
                            color = TextPrimary
                        )
                        Text(
                            text = "Scene analysis, character consistency, & continuity checks",
                            style = StillframeTypography.labelSmall,
                            color = TextTertiary
                        )
                    }

                    // Model Manager Status Chip
                    FilterChip(
                        selected = false,
                        onClick = { showModelLifecycleDialog = true },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (modelState.status) {
                                                ModelLifecycleStatus.READY -> StatusConfirmed
                                                ModelLifecycleStatus.NOT_INSTALLED -> AccentAmber
                                                else -> TextTertiary
                                            }
                                        )
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = modelState.status.label.uppercase(),
                                    style = StillframeTypography.labelSmall
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(containerColor = StillframeSurface2),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = false, borderColor = StillframeBorder)
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Target Scene Selector
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, StillframeBorder, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = StillframeSurface1)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("TARGET SCENE FOR REVIEW", style = StillframeTypography.labelSmall, color = TextTertiary)
                        Spacer(Modifier.height(8.dp))

                        if (scenes.isEmpty()) {
                            Text("No scenes written in this project yet.", style = StillframeTypography.bodyMedium, color = TextSecondary)
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(scenes) { scene ->
                                    val isSelected = (currentScene?.id == scene.id)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.selectReviewScene(scene.id) },
                                        label = {
                                            Text(
                                                text = "${scene.title} (${scene.format})",
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = accentColor.copy(alpha = 0.2f),
                                            selectedLabelColor = accentColor
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Critique Mode Selector
            item {
                Column {
                    Text(
                        text = "CRITIQUE FOCUS MODE",
                        style = StillframeTypography.labelSmall,
                        color = TextTertiary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(CritiqueMode.values()) { mode ->
                            val isSelected = selectedMode == mode
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectCritiqueMode(mode) },
                                label = { Text(mode.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StillframeSurface3,
                                    selectedLabelColor = TextPrimary
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = selectedMode.description,
                        style = StillframeTypography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // Context Inspector Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, StillframeBorder, RoundedCornerShape(8.dp))
                        .clickable { showContextSheet = !showContextSheet },
                    colors = CardDefaults.cardColors(containerColor = StillframeSurface1)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Visibility, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Context Window Included", style = StillframeTypography.titleMedium, color = TextPrimary)
                            }
                            Icon(
                                if (showContextSheet) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }

                        Text(
                            text = "${characters.size} Characters • ${loreList.size} Lore Rules • ${timelineEvents.size} Timeline Events",
                            style = StillframeTypography.labelSmall,
                            color = TextTertiary,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        AnimatedVisibility(visible = showContextSheet) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                HorizontalDivider(color = StillframeBorder)
                                Text(
                                    text = "Roster: ${characters.joinToString(", ") { it.name }}",
                                    style = StillframeTypography.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Canon Lore: ${loreList.filter { it.status == "Confirmed" }.joinToString(", ") { it.title }}",
                                    style = StillframeTypography.bodySmall,
                                    color = TextSecondary
                                )
                                if (loreList.any { it.status == "Deprecated" }) {
                                    Text(
                                        text = "Deprecated Lore (Continuity Alerts): ${loreList.filter { it.status == "Deprecated" }.joinToString(", ") { it.title }}",
                                        style = StillframeTypography.bodySmall,
                                        color = AccentCrimson
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Run Analysis Action Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = StillframeSurface1)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Editorial Analysis Engine", style = StillframeTypography.titleMedium, color = TextPrimary)
                            Text("Inspects text excerpts against world canon and cadence rules", style = StillframeTypography.labelSmall, color = TextTertiary)
                        }

                        Button(
                            onClick = { viewModel.runEditorialAnalysis() },
                            enabled = !isAnalyzing && currentScene != null,
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("run_analysis_button")
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = StillframeBlack, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = StillframeBlack, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Analyze", color = StillframeBlack, style = StillframeTypography.labelLarge)
                            }
                        }
                    }
                }
            }

            // Scene Metrics Dashboard (if calculated)
            if (metrics != null) {
                item {
                    SceneMetricsCard(metrics = metrics!!, accentColor = accentColor)
                }
            }

            // Findings List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EDITORIAL FINDINGS (${findings.size})",
                        style = StillframeTypography.labelMedium.copy(letterSpacing = 1.sp),
                        color = TextTertiary
                    )
                }
            }

            if (findings.isEmpty() && !isAnalyzing) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (metrics != null) "No critical findings detected for ${selectedMode.label}." else "Tap 'Analyze' above to run editorial critique on this scene.",
                            style = StillframeTypography.bodyMedium,
                            color = TextTertiary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            items(findings, key = { it.id }) { finding ->
                FindingCard(
                    finding = finding,
                    onDismiss = { viewModel.dismissFinding(finding.id) },
                    onCopyToNotes = { viewModel.copyFindingToNotes(finding) }
                )
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }

    // Model Lifecycle Information Dialog
    if (showModelLifecycleDialog) {
        ModelLifecycleDialog(
            manager = viewModel.localModelManager,
            state = modelState,
            onDismiss = { showModelLifecycleDialog = false }
        )
    }
}

// ----------------------------------------------------
// SCENE METRICS CARD
// ----------------------------------------------------
@Composable
fun SceneMetricsCard(metrics: SceneMetrics, accentColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StillframeBorder, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = StillframeSurface1)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("SCENE CADENCE & PROPORTIONS", style = StillframeTypography.labelSmall, color = TextTertiary)
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MetricColumn(title = "Total Words", value = "${metrics.totalWords}")
                MetricColumn(title = "Dialogue", value = "${metrics.dialoguePercentage}%")
                MetricColumn(title = "Avg Sentence", value = "${String.format("%.1f", metrics.avgSentenceLength)} w")
                MetricColumn(title = "Read Time", value = "${String.format("%.1f", metrics.readingTimeMinutes)} min")
            }

            Spacer(Modifier.height(14.dp))

            // Proportions bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(StillframeSurface3)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight((metrics.dialoguePercentage.coerceAtLeast(1)).toFloat())
                        .background(accentColor)
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(((100 - metrics.dialoguePercentage).coerceAtLeast(1)).toFloat())
                        .background(AccentSteelBlue)
                )
            }

            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("■ Dialogue: ${metrics.dialogueWords} w", style = StillframeTypography.labelSmall, color = accentColor)
                Text("■ Narrative: ${metrics.narrativeWords} w", style = StillframeTypography.labelSmall, color = AccentSteelBlue)
            }

            if (metrics.detectedSpeakers.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Speaking Cast: ${metrics.detectedSpeakers.joinToString(", ")}",
                    style = StillframeTypography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun MetricColumn(title: String, value: String) {
    Column {
        Text(text = title, style = StillframeTypography.labelSmall, color = TextTertiary)
        Spacer(Modifier.height(2.dp))
        Text(text = value, style = StillframeTypography.titleLarge, color = TextPrimary)
    }
}

// ----------------------------------------------------
// FINDING CARD
// ----------------------------------------------------
@Composable
fun FindingCard(
    finding: CritiqueFinding,
    onDismiss: () -> Unit,
    onCopyToNotes: () -> Unit
) {
    var copiedNotice by remember { mutableStateOf(false) }

    val (severityColor, severityLabel) = when (finding.severity) {
        FindingSeverity.OBSERVATION -> Pair(TextSecondary, "OBSERVATION")
        FindingSeverity.SUGGESTION -> Pair(AccentAmber, "EDITORIAL ADVICE")
        FindingSeverity.CONTINUITY_ALERT -> Pair(AccentCrimson, "CONTINUITY ALERT")
        FindingSeverity.STRONG_CONCERN -> Pair(AccentCrimson, "CRITICAL CONCERN")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, severityColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = StillframeSurface1)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = severityLabel,
                    style = StillframeTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = severityColor
                )

                Text(
                    text = finding.category,
                    style = StillframeTypography.labelSmall,
                    color = TextTertiary
                )
            }

            Spacer(Modifier.height(8.dp))

            // Evidence Box
            if (finding.evidenceExcerpt.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(StillframeSurface2)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = finding.evidenceExcerpt,
                        style = StillframeTypography.bodySmall.copy(fontFamily = ScreenplayMono),
                        color = TextPrimary
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            Text(
                text = finding.explanation,
                style = StillframeTypography.bodyMedium,
                color = TextSecondary
            )

            if (finding.suggestion != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Suggestion: ${finding.suggestion}",
                    style = StillframeTypography.bodySmall,
                    color = TextPrimary
                )
            }

            if (finding.authorQuestion != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Question for Author: ${finding.authorQuestion}",
                    style = StillframeTypography.bodySmall,
                    color = AccentAmber
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (copiedNotice) {
                    Text("Saved to notes", style = StillframeTypography.labelSmall, color = StatusConfirmed)
                    Spacer(Modifier.width(8.dp))
                }

                TextButton(
                    onClick = {
                        onCopyToNotes()
                        copiedNotice = true
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Outlined.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                    Spacer(Modifier.width(4.dp))
                    Text("Copy to Notes", style = StillframeTypography.labelSmall, color = TextSecondary)
                }

                Spacer(Modifier.width(8.dp))

                TextButton(
                    onClick = onDismiss,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Dismiss", style = StillframeTypography.labelSmall, color = TextTertiary)
                }
            }
        }
    }
}

// ----------------------------------------------------
// LOCAL MODEL LIFECYCLE & DIAGNOSTICS DIALOG
// ----------------------------------------------------
@Composable
fun ModelLifecycleDialog(
    manager: LocalModelManager,
    state: ModelManagerState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StillframeSurface2,
        title = {
            Text("On-Device Local AI Critic (LiteRT-LM)", style = StillframeTypography.titleLarge, color = TextPrimary)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "LIFECYCLE STATUS: ${state.status.label.uppercase()}",
                    style = StillframeTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = when (state.status) {
                        ModelLifecycleStatus.READY -> StatusConfirmed
                        ModelLifecycleStatus.NOT_INSTALLED -> AccentAmber
                        else -> TextTertiary
                    }
                )

                Text(
                    text = state.status.description,
                    style = StillframeTypography.bodySmall,
                    color = TextSecondary
                )

                HorizontalDivider(color = StillframeBorder)

                Text("TARGET MODEL SPECIFICATION", style = StillframeTypography.labelSmall, color = TextTertiary)
                Text("• Model: ${manager.modelSpec.name}", style = StillframeTypography.bodySmall, color = TextPrimary)
                Text("• Architecture: ${manager.modelSpec.architecture}", style = StillframeTypography.bodySmall, color = TextPrimary)
                Text("• Package Download Size: ~2.6 GB", style = StillframeTypography.bodySmall, color = TextPrimary)
                Text("• Required Free Storage: ~3.5 GB", style = StillframeTypography.bodySmall, color = TextPrimary)
                Text("• Runtime: ${manager.modelSpec.targetRuntime}", style = StillframeTypography.bodySmall, color = TextPrimary)

                HorizontalDivider(color = StillframeBorder)

                Text("DEVICE PREREQUISITES & DIAGNOSTICS", style = StillframeTypography.labelSmall, color = TextTertiary)
                Text(state.compatibilityDetails, style = StillframeTypography.bodySmall, color = TextSecondary)
                Text("Free Internal Storage: ${state.freeStorageBytes / (1024 * 1024)} MB", style = StillframeTypography.bodySmall, color = TextSecondary)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(StillframeSurface1)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Note: True offline local LLM weights (Gemma 4 E4B-it) require physical arm64 Android hardware with native LiteRT binaries. Stillframe NEVER fakes AI results or silently uses cloud services. The rule-based Editorial Analysis Engine is active and running.",
                        style = StillframeTypography.labelSmall,
                        color = TextTertiary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Understood", color = AccentAmber)
            }
        }
    )
}
