package com.example.ui.screens.drafts

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
import com.example.data.model.ChapterEntity
import com.example.data.model.SceneEntity
import com.example.data.model.SceneRevisionEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.StillframeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraftsScreen(
    viewModel: StillframeViewModel,
    modifier: Modifier = Modifier
) {
    val activeProject by viewModel.activeProject.collectAsState()
    val scenes by viewModel.scenes.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val activeSceneId by viewModel.activeSceneId.collectAsState()
    val activeScene by viewModel.activeScene.collectAsState()
    val isFocusMode by viewModel.isFocusMode.collectAsState()

    var showCreateSceneDialog by remember { mutableStateOf(false) }
    var showCreateChapterDialog by remember { mutableStateOf(false) }

    val accentColor = remember(activeProject?.accentColorHex) {
        try { Color(android.graphics.Color.parseColor(activeProject?.accentColorHex ?: "#E8B058")) } catch (e: Exception) { AccentAmber }
    }

    if (isFocusMode && activeScene != null) {
        FocusModeEditor(
            scene = activeScene!!,
            viewModel = viewModel,
            accentColor = accentColor,
            onExitFocus = { viewModel.setFocusMode(false) }
        )
        return
    }

    if (activeScene != null) {
        SceneWorkspaceEditor(
            scene = activeScene!!,
            viewModel = viewModel,
            accentColor = accentColor,
            onBackToList = { viewModel.setActiveScene(null) }
        )
        return
    }

    // Scene & Chapter List Overview
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
                            text = "MANUSCRIPT DRAFTS",
                            style = StillframeTypography.titleLarge.copy(letterSpacing = 2.sp),
                            color = TextPrimary
                        )
                        val totalWords = scenes.sumOf { it.wordCount }
                        Text(
                            text = "${scenes.size} scenes | $totalWords words total",
                            style = StillframeTypography.labelSmall,
                            color = TextTertiary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showCreateChapterDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StillframeBorder),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("+ Act/Chapter", style = StillframeTypography.labelSmall)
                        }

                        Button(
                            onClick = { showCreateSceneDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("create_scene_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = StillframeBlack)
                            Spacer(Modifier.width(4.dp))
                            Text("New Scene", style = StillframeTypography.labelSmall, color = StillframeBlack)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (scenes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.DriveFileRenameOutline, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No Scenes In This Universe", style = StillframeTypography.titleLarge, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Create a screenplay sequence or prose chapter to begin writing. Stillframe autosaves all drafts locally.",
                        style = StillframeTypography.bodyMedium,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { showCreateSceneDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text("Draft First Scene", color = StillframeBlack)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(scenes.sortedBy { it.orderIndex }, key = { it.id }) { scene ->
                    SceneOverviewCard(
                        scene = scene,
                        accentColor = accentColor,
                        onClick = { viewModel.setActiveScene(scene.id) },
                        onDelete = { viewModel.deleteScene(scene.id) }
                    )
                }

                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }

    if (showCreateSceneDialog) {
        CreateSceneDialog(
            chapters = chapters,
            onDismiss = { showCreateSceneDialog = false },
            onCreate = { title, format, chapterId ->
                viewModel.createScene(title, format, chapterId)
                showCreateSceneDialog = false
            }
        )
    }

    if (showCreateChapterDialog) {
        CreateChapterDialog(
            onDismiss = { showCreateChapterDialog = false },
            onCreate = { title ->
                viewModel.createChapter(title)
                showCreateChapterDialog = false
            }
        )
    }
}

@Composable
fun SceneOverviewCard(
    scene: SceneEntity,
    accentColor: Color,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, StillframeBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .testTag("scene_card_${scene.id}"),
        colors = CardDefaults.cardColors(containerColor = StillframeSurface1)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = scene.format.uppercase(),
                        style = StillframeTypography.labelSmall,
                        color = if (scene.format == "SCREENPLAY") AccentAmber else AccentSteelBlue,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StillframeSurface2)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${scene.wordCount} words",
                        style = StillframeTypography.labelSmall,
                        color = TextTertiary
                    )
                }

                Box {
                    IconButton(onClick = { expandedMenu = true }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextTertiary, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false },
                        modifier = Modifier.background(StillframeSurface2)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete Scene", color = AccentCrimson) },
                            onClick = {
                                expandedMenu = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = AccentCrimson) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = scene.title,
                style = StillframeTypography.titleLarge,
                color = TextPrimary
            )

            if (scene.sceneObjective.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Objective: ${scene.sceneObjective}",
                    style = StillframeTypography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = scene.content.take(120).replace("\n", " ") + "...",
                style = StillframeTypography.bodySmall.copy(
                    fontFamily = if (scene.format == "SCREENPLAY") ScreenplayMono else InterfaceSans
                ),
                color = TextTertiary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ----------------------------------------------------
// FULL SCENE WORKSPACE EDITOR
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SceneWorkspaceEditor(
    scene: SceneEntity,
    viewModel: StillframeViewModel,
    accentColor: Color,
    onBackToList: () -> Unit
) {
    var title by remember(scene.id) { mutableStateOf(scene.title) }
    var content by remember(scene.id) { mutableStateOf(scene.content) }
    var objective by remember(scene.id) { mutableStateOf(scene.sceneObjective) }
    var intent by remember(scene.id) { mutableStateOf(scene.emotionalIntent) }
    var format by remember(scene.id) { mutableStateOf(scene.format) }
    var fontSizeSp by remember { mutableStateOf(16f) }

    val saveStatus by viewModel.saveStatusText.collectAsState()
    val revisions by viewModel.activeSceneRevisions.collectAsState()
    val characters by viewModel.characters.collectAsState()

    var showMetadataDrawer by remember { mutableStateOf(false) }
    var showRevisionsDialog by remember { mutableStateOf(false) }
    var showFormatConvertDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = StillframeBlack,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StillframeSurface0)
                    .border(androidx.compose.foundation.BorderStroke(1.dp, StillframeBorder))
            ) {
                // Top control bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBackToList) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back to scenes", tint = TextPrimary)
                        }

                        Text(
                            text = format.uppercase(),
                            style = StillframeTypography.labelSmall,
                            color = accentColor,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(StillframeSurface2)
                                .clickable { showFormatConvertDialog = true }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = saveStatus,
                            style = StillframeTypography.labelSmall,
                            color = TextTertiary,
                            modifier = Modifier.padding(end = 4.dp)
                        )

                        // Collapsible Scene Objective / Metadata toggle
                        IconButton(
                            onClick = { showMetadataDrawer = !showMetadataDrawer },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                if (showMetadataDrawer) Icons.Filled.Info else Icons.Outlined.Info,
                                contentDescription = "Scene metadata",
                                tint = if (showMetadataDrawer) accentColor else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Revisions history
                        IconButton(
                            onClick = { showRevisionsDialog = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Outlined.History, contentDescription = "Revisions", tint = TextSecondary, modifier = Modifier.size(20.dp))
                        }

                        // Focus Mode
                        IconButton(
                            onClick = { viewModel.setFocusMode(true) },
                            modifier = Modifier.size(36.dp),
                            colors = IconButtonDefaults.iconButtonColors(contentColor = accentColor)
                        ) {
                            Icon(Icons.Outlined.Fullscreen, contentDescription = "Focus Mode", modifier = Modifier.size(22.dp))
                        }
                    }
                }

                // Scene Title Inline Field
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        viewModel.updateSceneMetadata(title, objective, intent, format, scene.attachedCharacterIds)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    textStyle = StillframeTypography.titleLarge.copy(color = TextPrimary),
                    placeholder = { Text("Scene Title...", color = TextTertiary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )

                // Optional Collapsible Metadata Panel
                AnimatedVisibility(visible = showMetadataDrawer) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StillframeSurface1)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = objective,
                            onValueChange = {
                                objective = it
                                viewModel.updateSceneMetadata(title, objective, intent, format, scene.attachedCharacterIds)
                            },
                            label = { Text("Scene Objective / Dramatic Goal", color = TextSecondary) },
                            placeholder = { Text("What must the character achieve before leaving this space?", color = TextTertiary) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = StillframeBorder
                            )
                        )

                        OutlinedTextField(
                            value = intent,
                            onValueChange = {
                                intent = it
                                viewModel.updateSceneMetadata(title, objective, intent, format, scene.attachedCharacterIds)
                            },
                            label = { Text("Emotional Intent & Tone", color = TextSecondary) },
                            placeholder = { Text("e.g. Paranoia escalating into frantic bargaining", color = TextTertiary) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = StillframeBorder
                            )
                        )

                        // Attached Characters Chips
                        Text("ATTACHED CHARACTERS TO ROSTER", style = StillframeTypography.labelSmall, color = TextTertiary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(characters) { char ->
                                val attachedIds = scene.attachedCharacterIds.split(",").filter { it.isNotBlank() }
                                val isAttached = char.id in attachedIds
                                FilterChip(
                                    selected = isAttached,
                                    onClick = {
                                        val newIds = if (isAttached) attachedIds - char.id else attachedIds + char.id
                                        viewModel.updateSceneMetadata(title, objective, intent, format, newIds.joinToString(","))
                                    },
                                    label = { Text(char.name) }
                                )
                            }
                        }
                    }
                }

                // Screenplay Formatting Toolstrip
                if (format == "SCREENPLAY") {
                    ScreenplayToolstrip(
                        onInsert = { insertion ->
                            val updated = if (content.isEmpty()) insertion else "$content\n\n$insertion"
                            content = updated
                            viewModel.updateSceneContent(updated)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Text Editor Area
            OutlinedTextField(
                value = content,
                onValueChange = {
                    content = it
                    viewModel.updateSceneContent(it)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("manuscript_text_editor"),
                textStyle = StillframeTypography.bodyLarge.copy(
                    fontFamily = if (format == "SCREENPLAY") ScreenplayMono else InterfaceSans,
                    fontSize = fontSizeSp.sp,
                    lineHeight = (fontSizeSp * 1.55f).sp,
                    color = TextPrimary
                ),
                placeholder = {
                    Text(
                        if (format == "SCREENPLAY") "INT. SCENE HEADING - TIME\n\nAction begins here..." else "Write long-form prose narrative here...",
                        color = TextTertiary,
                        fontFamily = if (format == "SCREENPLAY") ScreenplayMono else InterfaceSans
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = StillframeBlack,
                    unfocusedContainerColor = StillframeBlack
                )
            )
        }
    }

    // Revisions Dialog
    if (showRevisionsDialog) {
        RevisionsHistoryDialog(
            revisions = revisions,
            onRestore = { rev ->
                viewModel.restoreRevision(rev)
                content = rev.content
                title = rev.title
                format = rev.format
                showRevisionsDialog = false
            },
            onSaveManualSnapshot = {
                viewModel.updateSceneContent(content, immediateSnapshot = true)
            },
            onDismiss = { showRevisionsDialog = false }
        )
    }

    // Format Conversion Dialog
    if (showFormatConvertDialog) {
        AlertDialog(
            onDismissRequest = { showFormatConvertDialog = false },
            containerColor = StillframeSurface2,
            title = { Text("Switch Scene Format", style = StillframeTypography.titleLarge, color = TextPrimary) },
            text = {
                Text(
                    "Currently: $format\n\nSwitching format adapts font rendering and editing toolbars. Your existing text content is completely preserved.",
                    style = StillframeTypography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newFmt = if (format == "SCREENPLAY") "PROSE" else "SCREENPLAY"
                        format = newFmt
                        viewModel.updateSceneMetadata(title, objective, intent, newFmt, scene.attachedCharacterIds)
                        showFormatConvertDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("Switch to ${if (format == "SCREENPLAY") "Prose" else "Screenplay"}", color = StillframeBlack)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFormatConvertDialog = false }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }
}

// ----------------------------------------------------
// SCREENPLAY QUICK FORMATTING TOOLSTRIP
// ----------------------------------------------------
@Composable
fun ScreenplayToolstrip(
    onInsert: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(StillframeSurface1)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SuggestionChip(
                onClick = { onInsert("INT. LOCATION - NIGHT") },
                label = { Text("INT. HEADING", fontFamily = ScreenplayMono, fontSize = 11.sp) }
            )
        }
        item {
            SuggestionChip(
                onClick = { onInsert("EXT. LOCATION - DAY") },
                label = { Text("EXT. HEADING", fontFamily = ScreenplayMono, fontSize = 11.sp) }
            )
        }
        item {
            SuggestionChip(
                onClick = { onInsert("CHARACTER NAME\n(whispering)\nDialogue line goes here.") },
                label = { Text("CHAR & DIALOGUE", fontFamily = ScreenplayMono, fontSize = 11.sp) }
            )
        }
        item {
            SuggestionChip(
                onClick = { onInsert("CUT TO:") },
                label = { Text("CUT TO:", fontFamily = ScreenplayMono, fontSize = 11.sp) }
            )
        }
        item {
            SuggestionChip(
                onClick = { onInsert("FADE OUT.") },
                label = { Text("FADE OUT.", fontFamily = ScreenplayMono, fontSize = 11.sp) }
            )
        }
    }
}

// ----------------------------------------------------
// FULLSCREEN FOCUS MODE
// ----------------------------------------------------
@Composable
fun FocusModeEditor(
    scene: SceneEntity,
    viewModel: StillframeViewModel,
    accentColor: Color,
    onExitFocus: () -> Unit
) {
    var content by remember(scene.id) { mutableStateOf(scene.content) }
    var fontSizeSp by remember { mutableStateOf(18f) }
    val wordCount = remember(content) { content.split(Regex("\\s+")).count { it.isNotBlank() } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StillframeBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Minimal Focus Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${scene.title.uppercase()} • $wordCount words",
                    style = StillframeTypography.labelSmall.copy(letterSpacing = 1.sp),
                    color = TextTertiary
                )

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Font size adjuster
                    IconButton(
                        onClick = { if (fontSizeSp > 14f) fontSizeSp -= 2f },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("A-", color = TextSecondary, style = StillframeTypography.labelSmall)
                    }

                    IconButton(
                        onClick = { if (fontSizeSp < 28f) fontSizeSp += 2f },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("A+", color = TextSecondary, style = StillframeTypography.labelSmall)
                    }

                    IconButton(
                        onClick = onExitFocus,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(StillframeSurface2)
                    ) {
                        Icon(Icons.Outlined.FullscreenExit, contentDescription = "Exit focus mode", tint = TextPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Clean Distraction-Free Text Area
            OutlinedTextField(
                value = content,
                onValueChange = {
                    content = it
                    viewModel.updateSceneContent(it)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                textStyle = StillframeTypography.bodyLarge.copy(
                    fontFamily = if (scene.format == "SCREENPLAY") ScreenplayMono else EditorialSerif,
                    fontSize = fontSizeSp.sp,
                    lineHeight = (fontSizeSp * 1.6f).sp,
                    color = TextPrimary
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = StillframeBlack,
                    unfocusedContainerColor = StillframeBlack
                )
            )
        }
    }
}

// ----------------------------------------------------
// REVISIONS HISTORY DIALOG
// ----------------------------------------------------
@Composable
fun RevisionsHistoryDialog(
    revisions: List<SceneRevisionEntity>,
    onRestore: (SceneRevisionEntity) -> Unit,
    onSaveManualSnapshot: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StillframeSurface2,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Revision History", style = StillframeTypography.titleLarge, color = TextPrimary)
                TextButton(onClick = onSaveManualSnapshot) {
                    Text("+ Save Snapshot", color = AccentAmber)
                }
            }
        },
        text = {
            if (revisions.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    Text("No past snapshots stored yet. Revisions are created as you draft.", style = StillframeTypography.bodySmall, color = TextTertiary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(revisions) { rev ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, StillframeBorder, RoundedCornerShape(6.dp)),
                            colors = CardDefaults.cardColors(containerColor = StillframeSurface1)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = rev.summary, style = StillframeTypography.titleMedium, color = TextPrimary)
                                    Text(
                                        text = formatTime(rev.savedAt),
                                        style = StillframeTypography.labelSmall,
                                        color = TextTertiary
                                    )
                                }

                                Button(
                                    onClick = { onRestore(rev) },
                                    colors = ButtonDefaults.buttonColors(containerColor = StillframeSurface3),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Restore", style = StillframeTypography.labelSmall, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = TextSecondary) }
        }
    )
}

@Composable
fun CreateSceneDialog(
    chapters: List<ChapterEntity>,
    onDismiss: () -> Unit,
    onCreate: (title: String, format: String, chapterId: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedFormat by remember { mutableStateOf("PROSE") }
    var selectedChapterId by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StillframeSurface2,
        title = { Text("New Scene", style = StillframeTypography.titleLarge, color = TextPrimary) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Scene Title", color = TextSecondary) },
                    placeholder = { Text("e.g. Descent to Sub-Relay", color = TextTertiary) },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("FORMAT", style = StillframeTypography.labelSmall, color = TextTertiary)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = selectedFormat == "PROSE",
                        onClick = { selectedFormat = "PROSE" },
                        label = { Text("Prose Narrative") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedFormat == "SCREENPLAY",
                        onClick = { selectedFormat = "SCREENPLAY" },
                        label = { Text("Screenplay Script") },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (chapters.isNotEmpty()) {
                    Text("CHAPTER / ACT (OPTIONAL)", style = StillframeTypography.labelSmall, color = TextTertiary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(chapters) { ch ->
                            FilterChip(
                                selected = selectedChapterId == ch.id,
                                onClick = { selectedChapterId = if (selectedChapterId == ch.id) null else ch.id },
                                label = { Text(ch.title) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(title, selectedFormat, selectedChapterId) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)
            ) { Text("Create Scene", color = StillframeBlack) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun CreateChapterDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String) -> Unit
) {
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StillframeSurface2,
        title = { Text("New Act or Chapter", style = StillframeTypography.titleLarge, color = TextPrimary) },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Chapter Title", color = TextSecondary) },
                placeholder = { Text("e.g. Act I: The Low Frequencies", color = TextTertiary) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onCreate(title) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)
            ) { Text("Create", color = StillframeBlack) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun formatTime(millis: Long): String {
    val sdf = SimpleDateFormat("MMM d, HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(millis))
}
