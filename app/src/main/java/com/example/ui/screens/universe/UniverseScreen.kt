package com.example.ui.screens.universe

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.ProjectEntity
import com.example.data.repository.SearchResultItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.StillframeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniverseScreen(
    viewModel: StillframeViewModel,
    modifier: Modifier = Modifier
) {
    val projects by viewModel.allProjects.collectAsState()
    val activeProjectId by viewModel.activeProjectId.collectAsState()
    val activeProject by viewModel.activeProject.collectAsState()
    val scenes by viewModel.scenes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var projectToDelete by remember { mutableStateOf<ProjectEntity?>(null) }
    var projectToExport by remember { mutableStateOf<ProjectEntity?>(null) }

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
                            text = "STILLFRAME",
                            style = StillframeTypography.titleLarge.copy(
                                letterSpacing = 3.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = "Private Creative Universe Archive",
                            style = StillframeTypography.labelSmall.copy(letterSpacing = 1.sp),
                            color = TextTertiary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { showSearchDialog = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(StillframeSurface1)
                                .testTag("universe_search_button")
                        ) {
                            Icon(
                                Icons.Outlined.Search,
                                contentDescription = "Search archive",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { showCreateDialog = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AccentAmber)
                                .testTag("create_project_button")
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Create story universe",
                                tint = StillframeBlack,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
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
            // "Resume Project / Continue Writing" Hero Banner if active project exists
            if (activeProject != null) {
                item {
                    val latestScene = scenes.maxByOrNull { it.updatedAt }
                    ResumeHeroCard(
                        project = activeProject!!,
                        latestSceneTitle = latestScene?.title,
                        sceneCount = scenes.size,
                        onResumeClick = {
                            if (latestScene != null) {
                                viewModel.setActiveScene(latestScene.id)
                                viewModel.setDestination(AppDestination.DRAFTS)
                            } else {
                                viewModel.setDestination(AppDestination.WORLD)
                            }
                        }
                    )
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STORY ARCHIVES (${projects.size})",
                        style = StillframeTypography.labelMedium.copy(letterSpacing = 1.5.sp),
                        color = TextTertiary
                    )

                    // Sample universe quick button
                    val hasSample = projects.any { it.isSample }
                    if (!hasSample) {
                        TextButton(
                            onClick = { viewModel.loadSampleUniverse() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("load_sample_button")
                        ) {
                            Icon(
                                Icons.Outlined.AutoStories,
                                contentDescription = null,
                                tint = AccentAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Load Sample Universe",
                                style = StillframeTypography.labelSmall,
                                color = AccentAmber
                            )
                        }
                    }
                }
            }

            // Empty State
            if (projects.isEmpty()) {
                item {
                    EmptyUniverseCard(
                        onCreateClick = { showCreateDialog = true },
                        onLoadSampleClick = { viewModel.loadSampleUniverse() }
                    )
                }
            }

            // Project List
            items(projects, key = { it.id }) { project ->
                ProjectCard(
                    project = project,
                    isActive = project.id == activeProjectId,
                    onSelect = {
                        viewModel.setActiveProject(project.id)
                    },
                    onOpenWorld = {
                        viewModel.setActiveProject(project.id)
                        viewModel.setDestination(AppDestination.WORLD)
                    },
                    onOpenDrafts = {
                        viewModel.setActiveProject(project.id)
                        viewModel.setDestination(AppDestination.DRAFTS)
                    },
                    onDelete = {
                        projectToDelete = project
                    },
                    onExport = {
                        projectToExport = project
                    }
                )
            }

            item {
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    // Create Project Dialog
    if (showCreateDialog) {
        CreateProjectDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, desc, genre, tags, accentHex ->
                viewModel.createProject(title, desc, genre, tags, accentHex)
                showCreateDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    if (projectToDelete != null) {
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            containerColor = StillframeSurface2,
            title = {
                Text(
                    text = "Delete Story Universe?",
                    style = StillframeTypography.titleLarge,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "This will permanently delete \"${projectToDelete?.title}\" along with all associated characters, lore rules, timelines, and scenes. This action is offline and cannot be undone.",
                    style = StillframeTypography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        projectToDelete?.let { viewModel.deleteProject(it.id) }
                        projectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCrimson),
                    modifier = Modifier.testTag("confirm_delete_project_button")
                ) {
                    Text("Delete Permanently", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Export Dialog
    if (projectToExport != null) {
        ExportProjectDialog(
            project = projectToExport!!,
            viewModel = viewModel,
            onDismiss = { projectToExport = null }
        )
    }

    // Offline Search Dialog
    if (showSearchDialog) {
        OfflineSearchDialog(
            searchQuery = searchQuery,
            searchResults = searchResults,
            onQueryChange = { viewModel.onSearchQueryChanged(it) },
            onSelectResult = { item ->
                viewModel.setActiveProject(item.projectId)
                showSearchDialog = false
                when {
                    item.entityType.startsWith("Scene") -> {
                        viewModel.setActiveScene(item.id)
                        viewModel.setDestination(AppDestination.DRAFTS)
                    }
                    else -> viewModel.setDestination(AppDestination.WORLD)
                }
            },
            onDismiss = { showSearchDialog = false }
        )
    }
}

@Composable
fun ResumeHeroCard(
    project: ProjectEntity,
    latestSceneTitle: String?,
    sceneCount: Int,
    onResumeClick: () -> Unit
) {
    val accentColor = remember(project.accentColorHex) {
        try { Color(android.graphics.Color.parseColor(project.accentColorHex)) } catch (e: Exception) { AccentAmber }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .clickable { onResumeClick() }
            .testTag("resume_hero_card"),
        colors = CardDefaults.cardColors(containerColor = StillframeSurface1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "ACTIVE UNIVERSE",
                        style = StillframeTypography.labelSmall.copy(letterSpacing = 1.sp),
                        color = accentColor
                    )
                }

                if (project.isSample) {
                    Text(
                        text = "SAMPLE ARCHIVE",
                        style = StillframeTypography.labelSmall,
                        color = TextTertiary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StillframeSurface2)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = project.title,
                style = StillframeTypography.headlineMedium,
                color = TextPrimary
            )

            if (project.description.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = project.description,
                    style = StillframeTypography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.EditNote,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (latestSceneTitle != null) "Resume: \"$latestSceneTitle\"" else "$sceneCount scenes written",
                        style = StillframeTypography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                FilledTonalButton(
                    onClick = onResumeClick,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = accentColor.copy(alpha = 0.15f),
                        contentColor = accentColor
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Open Workspace",
                        style = StillframeTypography.labelSmall
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
fun ProjectCard(
    project: ProjectEntity,
    isActive: Boolean,
    onSelect: () -> Unit,
    onOpenWorld: () -> Unit,
    onOpenDrafts: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }
    val accentColor = remember(project.accentColorHex) {
        try { Color(android.graphics.Color.parseColor(project.accentColorHex)) } catch (e: Exception) { AccentAmber }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(
                1.dp,
                if (isActive) accentColor.copy(alpha = 0.5f) else StillframeBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable { onSelect() }
            .testTag("project_card_${project.id}"),
        colors = CardDefaults.cardColors(containerColor = StillframeSurface1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (project.genre.isNotBlank()) project.genre.uppercase() else "STORY UNIVERSE",
                            style = StillframeTypography.labelSmall,
                            color = accentColor
                        )
                        if (project.isSample) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "SAMPLE",
                                style = StillframeTypography.labelSmall,
                                color = TextTertiary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(StillframeSurface2)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = project.title,
                        style = StillframeTypography.titleLarge,
                        color = TextPrimary
                    )
                }

                Box {
                    IconButton(
                        onClick = { expandedMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Project options",
                            tint = TextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false },
                        modifier = Modifier.background(StillframeSurface2)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Export Universe (Markdown)", color = TextPrimary) },
                            onClick = {
                                expandedMenu = false
                                onExport()
                            },
                            leadingIcon = { Icon(Icons.Outlined.FileDownload, contentDescription = null, tint = TextPrimary) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Universe", color = AccentCrimson) },
                            onClick = {
                                expandedMenu = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = AccentCrimson) }
                        )
                    }
                }
            }

            if (project.description.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = project.description,
                    style = StillframeTypography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edited ${formatDate(project.updatedAt)}",
                    style = StillframeTypography.labelSmall,
                    color = TextTertiary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onOpenWorld,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StillframeBorder),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Outlined.Public, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                        Spacer(Modifier.width(4.dp))
                        Text("World", style = StillframeTypography.labelSmall)
                    }

                    FilledTonalButton(
                        onClick = onOpenDrafts,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = StillframeSurface3,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Outlined.DriveFileRenameOutline, contentDescription = null, modifier = Modifier.size(14.dp), tint = accentColor)
                        Spacer(Modifier.width(4.dp))
                        Text("Drafts", style = StillframeTypography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyUniverseCard(
    onCreateClick: () -> Unit,
    onLoadSampleClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StillframeBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = StillframeSurface1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Outlined.AutoStories,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(48.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "No Creative Universes Yet",
                style = StillframeTypography.titleLarge,
                color = TextPrimary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Stillframe is your private fictional universe archive. Create a new story world or explore the sample neo-noir universe.",
                style = StillframeTypography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 12.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onCreateClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Create Story Universe", color = StillframeBlack)
                }

                OutlinedButton(
                    onClick = onLoadSampleClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StillframeBorder),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Load Sample")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, desc: String, genre: String, tags: String, accentHex: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var selectedAccent by remember { mutableStateOf("#E8B058") }

    val accentOptions = listOf(
        Pair("#E8B058", "Amber"),
        Pair("#6B9BE8", "Steel Blue"),
        Pair("#78B88E", "Sage"),
        Pair("#E06D75", "Crimson"),
        Pair("#D6D6DB", "Monochrome")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StillframeSurface2,
        title = {
            Text(
                text = "New Story Universe",
                style = StillframeTypography.headlineMedium,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Universe Title", color = TextSecondary) },
                    placeholder = { Text("e.g. The Glass Metropolis", color = TextTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentAmber,
                        unfocusedBorderColor = StillframeBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Premise or Core Theme (Optional)", color = TextSecondary) },
                    placeholder = { Text("Brief concept, logline, or setting atmosphere...", color = TextTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentAmber,
                        unfocusedBorderColor = StillframeBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = genre,
                        onValueChange = { genre = it },
                        label = { Text("Genre", color = TextSecondary) },
                        placeholder = { Text("e.g. Sci-Fi Noir", color = TextTertiary) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentAmber,
                            unfocusedBorderColor = StillframeBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags", color = TextSecondary) },
                        placeholder = { Text("Mystery, Cyber", color = TextTertiary) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentAmber,
                            unfocusedBorderColor = StillframeBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Text(
                    text = "ACCENT COLOR",
                    style = StillframeTypography.labelSmall,
                    color = TextTertiary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    accentOptions.forEach { (hex, name) ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        val isSelected = selectedAccent == hex
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) TextPrimary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedAccent = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreate(title, description, genre, tags, selectedAccent)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                modifier = Modifier.testTag("submit_create_project_button")
            ) {
                Text("Initialize Universe", color = StillframeBlack)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun ExportProjectDialog(
    project: ProjectEntity,
    viewModel: StillframeViewModel,
    onDismiss: () -> Unit
) {
    val scenes by viewModel.scenes.collectAsState()
    val characters by viewModel.characters.collectAsState()
    val loreList by viewModel.loreList.collectAsState()

    val markdownContent = remember(project, scenes, characters, loreList) {
        buildString {
            appendLine("# ${project.title}")
            if (project.description.isNotBlank()) appendLine("> ${project.description}\n")
            appendLine("**Genre:** ${project.genre} | **Tags:** ${project.tags}\n")
            appendLine("---")
            appendLine("## Characters (${characters.size})")
            characters.forEach {
                appendLine("### ${it.name} (${it.status})")
                if (it.shortSummary.isNotBlank()) appendLine(it.shortSummary)
                if (it.bio.isNotBlank()) appendLine(it.bio)
                appendLine()
            }
            appendLine("---")
            appendLine("## World Lore & Rules (${loreList.size})")
            loreList.forEach {
                appendLine("### [${it.status.uppercase()}] ${it.title} (${it.category})")
                appendLine(it.content)
                appendLine()
            }
            appendLine("---")
            appendLine("## Manuscript Drafts (${scenes.size} scenes)")
            scenes.sortedBy { it.orderIndex }.forEach { s ->
                appendLine("### ${s.title} [${s.format}]")
                if (s.sceneObjective.isNotBlank()) appendLine("*Objective: ${s.sceneObjective}*")
                appendLine()
                appendLine(s.content)
                appendLine()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StillframeSurface2,
        title = {
            Text("Export: ${project.title}", style = StillframeTypography.titleLarge, color = TextPrimary)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Complete private markdown compilation of world bible and manuscript drafts:",
                    style = StillframeTypography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(StillframeBlack, RoundedCornerShape(6.dp))
                        .border(1.dp, StillframeBorder, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    androidx.compose.foundation.text.selection.SelectionContainer {
                        Text(
                            text = markdownContent,
                            style = StillframeTypography.bodySmall.copy(fontFamily = ScreenplayMono),
                            color = TextPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AccentAmber)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineSearchDialog(
    searchQuery: String,
    searchResults: List<SearchResultItem>,
    onQueryChange: (String) -> Unit,
    onSelectResult: (SearchResultItem) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StillframeSurface2,
        title = {
            Text("Search Universe Archive", style = StillframeTypography.titleLarge, color = TextPrimary)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Search characters, lore, scenes...", color = TextTertiary) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentAmber,
                        unfocusedBorderColor = StillframeBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(Modifier.height(12.dp))

                if (searchResults.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (searchQuery.isBlank()) "Type to search offline story data" else "No matching story records found",
                            style = StillframeTypography.bodySmall,
                            color = TextTertiary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(searchResults) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectResult(item) },
                                colors = CardDefaults.cardColors(containerColor = StillframeSurface1)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = item.title,
                                            style = StillframeTypography.titleMedium,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = item.entityType,
                                            style = StillframeTypography.labelSmall,
                                            color = AccentAmber
                                        )
                                    }
                                    if (item.snippet.isNotBlank()) {
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = item.snippet,
                                            style = StillframeTypography.bodySmall,
                                            color = TextSecondary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = TextSecondary)
            }
        }
    )
}

private fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
    return sdf.format(Date(millis))
}
