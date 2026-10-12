package com.example.ui.screens.world

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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.StillframeViewModel
import com.example.ui.viewmodel.WorldTab
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldScreen(
    viewModel: StillframeViewModel,
    modifier: Modifier = Modifier
) {
    val activeProject by viewModel.activeProject.collectAsState()
    val currentTab by viewModel.currentWorldTab.collectAsState()

    val characters by viewModel.characters.collectAsState()
    val relationships by viewModel.relationships.collectAsState()
    val locations by viewModel.locations.collectAsState()
    val loreList by viewModel.loreList.collectAsState()
    val timelineEvents by viewModel.timelineEvents.collectAsState()
    val creativeNotes by viewModel.creativeNotes.collectAsState()

    var editingCharacter by remember { mutableStateOf<CharacterEntity?>(null) }
    var isNewCharacter by remember { mutableStateOf(false) }

    var editingRelationship by remember { mutableStateOf<RelationshipEntity?>(null) }
    var isNewRelationship by remember { mutableStateOf(false) }

    var editingLocation by remember { mutableStateOf<LocationEntity?>(null) }
    var isNewLocation by remember { mutableStateOf(false) }

    var editingLore by remember { mutableStateOf<LoreEntity?>(null) }
    var isNewLore by remember { mutableStateOf(false) }

    var editingTimeline by remember { mutableStateOf<TimelineEventEntity?>(null) }
    var isNewTimeline by remember { mutableStateOf(false) }

    var editingNote by remember { mutableStateOf<CreativeNoteEntity?>(null) }
    var isNewNote by remember { mutableStateOf(false) }

    val accentColor = remember(activeProject?.accentColorHex) {
        try { Color(android.graphics.Color.parseColor(activeProject?.accentColorHex ?: "#E8B058")) } catch (e: Exception) { AccentAmber }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = StillframeBlack,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StillframeBlack)
                    .padding(top = 12.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = activeProject?.title?.uppercase() ?: "NO ACTIVE UNIVERSE",
                            style = StillframeTypography.titleMedium.copy(letterSpacing = 1.sp),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "World Knowledge & Bible",
                            style = StillframeTypography.labelSmall,
                            color = TextTertiary
                        )
                    }

                    // Floating Create Action in top bar
                    IconButton(
                        onClick = {
                            val projId = activeProject?.id ?: return@IconButton
                            when (currentTab) {
                                WorldTab.CHARACTERS -> {
                                    editingCharacter = CharacterEntity(id = UUID.randomUUID().toString(), projectId = projId, name = "")
                                    isNewCharacter = true
                                }
                                WorldTab.RELATIONSHIPS -> {
                                    if (characters.size >= 2) {
                                        editingRelationship = RelationshipEntity(
                                            id = UUID.randomUUID().toString(),
                                            projectId = projId,
                                            characterId1 = characters[0].id,
                                            characterId2 = characters[1].id,
                                            characterName1 = characters[0].name,
                                            characterName2 = characters[1].name
                                        )
                                        isNewRelationship = true
                                    }
                                }
                                WorldTab.LOCATIONS -> {
                                    editingLocation = LocationEntity(id = UUID.randomUUID().toString(), projectId = projId, name = "")
                                    isNewLocation = true
                                }
                                WorldTab.LORE -> {
                                    editingLore = LoreEntity(id = UUID.randomUUID().toString(), projectId = projId, title = "")
                                    isNewLore = true
                                }
                                WorldTab.TIMELINE -> {
                                    editingTimeline = TimelineEventEntity(id = UUID.randomUUID().toString(), projectId = projId, title = "", chronologicalOrder = timelineEvents.size + 1)
                                    isNewTimeline = true
                                }
                                WorldTab.NOTES -> {
                                    editingNote = CreativeNoteEntity(id = UUID.randomUUID().toString(), projectId = projId, title = "")
                                    isNewNote = true
                                }
                            }
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                            .testTag("world_add_entity_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add entity", tint = StillframeBlack, modifier = Modifier.size(20.dp))
                    }
                }

                // Sub-Tabs Scrollable Row
                ScrollableTabRow(
                    selectedTabIndex = currentTab.ordinal,
                    containerColor = StillframeBlack,
                    contentColor = TextPrimary,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        if (currentTab.ordinal < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[currentTab.ordinal]),
                                color = accentColor
                            )
                        }
                    },
                    divider = { HorizontalDivider(color = StillframeBorder) }
                ) {
                    WorldTab.values().forEach { tab ->
                        val count = when (tab) {
                            WorldTab.CHARACTERS -> characters.size
                            WorldTab.RELATIONSHIPS -> relationships.size
                            WorldTab.LOCATIONS -> locations.size
                            WorldTab.LORE -> loreList.size
                            WorldTab.TIMELINE -> timelineEvents.size
                            WorldTab.NOTES -> creativeNotes.size
                        }
                        Tab(
                            selected = currentTab == tab,
                            onClick = { viewModel.setWorldTab(tab) },
                            text = {
                                Text(
                                    text = "${tab.label} ($count)",
                                    style = StillframeTypography.labelMedium,
                                    color = if (currentTab == tab) TextPrimary else TextSecondary
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                WorldTab.CHARACTERS -> {
                    CharactersSection(
                        characters = characters,
                        accentColor = accentColor,
                        onCharacterClick = {
                            editingCharacter = it
                            isNewCharacter = false
                        },
                        onDeleteCharacter = { viewModel.deleteCharacter(it.id) }
                    )
                }
                WorldTab.RELATIONSHIPS -> {
                    RelationshipsSection(
                        relationships = relationships,
                        characters = characters,
                        accentColor = accentColor,
                        onRelationshipClick = {
                            editingRelationship = it
                            isNewRelationship = false
                        },
                        onDeleteRelationship = { viewModel.deleteRelationship(it.id) },
                        onCreatePrompt = {
                            if (characters.size >= 2) {
                                editingRelationship = RelationshipEntity(
                                    id = UUID.randomUUID().toString(),
                                    projectId = activeProject?.id ?: "",
                                    characterId1 = characters[0].id,
                                    characterId2 = characters[1].id,
                                    characterName1 = characters[0].name,
                                    characterName2 = characters[1].name
                                )
                                isNewRelationship = true
                            }
                        }
                    )
                }
                WorldTab.LOCATIONS -> {
                    LocationsSection(
                        locations = locations,
                        accentColor = accentColor,
                        onLocationClick = {
                            editingLocation = it
                            isNewLocation = false
                        },
                        onDeleteLocation = { viewModel.deleteLocation(it.id) }
                    )
                }
                WorldTab.LORE -> {
                    LoreSection(
                        loreList = loreList,
                        accentColor = accentColor,
                        onLoreClick = {
                            editingLore = it
                            isNewLore = false
                        },
                        onDeleteLore = { viewModel.deleteLore(it.id) }
                    )
                }
                WorldTab.TIMELINE -> {
                    TimelineSection(
                        timelineEvents = timelineEvents,
                        accentColor = accentColor,
                        onEventClick = {
                            editingTimeline = it
                            isNewTimeline = false
                        },
                        onDeleteEvent = { viewModel.deleteTimelineEvent(it.id) }
                    )
                }
                WorldTab.NOTES -> {
                    CreativeNotesSection(
                        notes = creativeNotes,
                        accentColor = accentColor,
                        onNoteClick = {
                            editingNote = it
                            isNewNote = false
                        },
                        onDeleteNote = { viewModel.deleteCreativeNote(it.id) }
                    )
                }
            }
        }
    }

    // Character Edit Sheet/Dialog
    if (editingCharacter != null) {
        CharacterEditDialog(
            character = editingCharacter!!,
            isNew = isNewCharacter,
            onDismiss = { editingCharacter = null },
            onSave = { updated ->
                viewModel.saveCharacter(updated)
                editingCharacter = null
            }
        )
    }

    // Relationship Edit Dialog
    if (editingRelationship != null) {
        RelationshipEditDialog(
            relationship = editingRelationship!!,
            characters = characters,
            onDismiss = { editingRelationship = null },
            onSave = { updated ->
                viewModel.saveRelationship(updated)
                editingRelationship = null
            }
        )
    }

    // Location Edit Dialog
    if (editingLocation != null) {
        LocationEditDialog(
            location = editingLocation!!,
            onDismiss = { editingLocation = null },
            onSave = { updated ->
                viewModel.saveLocation(updated)
                editingLocation = null
            }
        )
    }

    // Lore Edit Dialog
    if (editingLore != null) {
        LoreEditDialog(
            lore = editingLore!!,
            onDismiss = { editingLore = null },
            onSave = { updated ->
                viewModel.saveLore(updated)
                editingLore = null
            }
        )
    }

    // Timeline Edit Dialog
    if (editingTimeline != null) {
        TimelineEditDialog(
            event = editingTimeline!!,
            onDismiss = { editingTimeline = null },
            onSave = { updated ->
                viewModel.saveTimelineEvent(updated)
                editingTimeline = null
            }
        )
    }

    // Creative Note Edit Dialog
    if (editingNote != null) {
        CreativeNoteEditDialog(
            note = editingNote!!,
            onDismiss = { editingNote = null },
            onSave = { updated ->
                viewModel.saveCreativeNote(updated)
                editingNote = null
            }
        )
    }
}

// ----------------------------------------------------
// CHARACTERS SECTION
// ----------------------------------------------------
@Composable
fun CharactersSection(
    characters: List<CharacterEntity>,
    accentColor: Color,
    onCharacterClick: (CharacterEntity) -> Unit,
    onDeleteCharacter: (CharacterEntity) -> Unit
) {
    if (characters.isEmpty()) {
        EmptySectionCard("No characters added yet. Tap + to introduce a protagonist or antagonist.")
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(characters, key = { it.id }) { char ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, StillframeBorder, RoundedCornerShape(8.dp))
                    .clickable { onCharacterClick(char) }
                    .testTag("character_card_${char.name}"),
                colors = CardDefaults.cardColors(containerColor = StillframeSurface1)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = char.name, style = StillframeTypography.titleLarge, color = TextPrimary)
                            if (char.aliases.isNotBlank()) {
                                Text(text = "a.k.a. ${char.aliases}", style = StillframeTypography.labelSmall, color = TextTertiary)
                            }
                        }

                        StatusBadge(status = char.status)
                    }

                    if (char.shortSummary.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = char.shortSummary,
                            style = StillframeTypography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (char.goalsMotivations.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Goal:", style = StillframeTypography.labelSmall, color = accentColor)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = char.goalsMotivations,
                                style = StillframeTypography.bodySmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// RELATIONSHIPS SECTION
// ----------------------------------------------------
@Composable
fun RelationshipsSection(
    relationships: List<RelationshipEntity>,
    characters: List<CharacterEntity>,
    accentColor: Color,
    onRelationshipClick: (RelationshipEntity) -> Unit,
    onDeleteRelationship: (RelationshipEntity) -> Unit,
    onCreatePrompt: () -> Unit
) {
    if (relationships.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("No Relationships Mapped", style = StillframeTypography.titleLarge, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            Text(
                "Connect two characters to track bonds, rivalries, family ties, and how their connection evolves.",
                style = StillframeTypography.bodyMedium,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (characters.size >= 2) {
                Spacer(Modifier.height(16.dp))
                Button(onClick = onCreatePrompt, colors = ButtonDefaults.buttonColors(containerColor = accentColor)) {
                    Text("Map First Relationship", color = StillframeBlack)
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(relationships, key = { it.id }) { rel ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, StillframeBorder, RoundedCornerShape(8.dp))
                    .clickable { onRelationshipClick(rel) },
                colors = CardDefaults.cardColors(containerColor = StillframeSurface1)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${rel.characterName1}  ↔  ${rel.characterName2}",
                            style = StillframeTypography.titleMedium,
                            color = TextPrimary
                        )

                        Text(
                            text = rel.relationType.uppercase(),
                            style = StillframeTypography.labelSmall,
                            color = accentColor,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(StillframeSurface2)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (rel.description.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = rel.description,
                            style = StillframeTypography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    if (rel.currentState.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "State: ${rel.currentState}",
                            style = StillframeTypography.labelSmall,
                            color = TextTertiary
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// LOCATIONS SECTION
// ----------------------------------------------------
@Composable
fun LocationsSection(
    locations: List<LocationEntity>,
    accentColor: Color,
    onLocationClick: (LocationEntity) -> Unit,
    onDeleteLocation: (LocationEntity) -> Unit
) {
    if (locations.isEmpty()) {
        EmptySectionCard("No locations documented. Add memorable environments, architectures, and sensory textures.")
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(locations, key = { it.id }) { loc ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, StillframeBorder, RoundedCornerShape(8.dp))
                    .clickable { onLocationClick(loc) },
                colors = CardDefaults.cardColors(containerColor = StillframeSurface1)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = loc.name, style = StillframeTypography.titleLarge, color = TextPrimary)

                    if (loc.atmosphere.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Atmosphere: ${loc.atmosphere}",
                            style = StillframeTypography.bodySmall,
                            color = accentColor
                        )
                    }

                    if (loc.description.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = loc.description,
                            style = StillframeTypography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (loc.sensoryDetails.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Sensory: ${loc.sensoryDetails}",
                            style = StillframeTypography.labelSmall,
                            color = TextTertiary
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// LORE SECTION
// ----------------------------------------------------
@Composable
fun LoreSection(
    loreList: List<LoreEntity>,
    accentColor: Color,
    onLoreClick: (LoreEntity) -> Unit,
    onDeleteLore: (LoreEntity) -> Unit
) {
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    val categories = remember(loreList) { loreList.map { it.category }.distinct() }

    val filtered = remember(loreList, selectedCategoryFilter) {
        if (selectedCategoryFilter == null) loreList else loreList.filter { it.category == selectedCategoryFilter }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter chips
        if (categories.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("All (${loreList.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StillframeSurface3,
                            selectedLabelColor = TextPrimary
                        )
                    )
                }
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StillframeSurface3,
                            selectedLabelColor = TextPrimary
                        )
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            EmptySectionCard("No lore entries recorded. Codify rules, technologies, magic, factions, and world history.")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { lore ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, StillframeBorder, RoundedCornerShape(8.dp))
                            .clickable { onLoreClick(lore) },
                        colors = CardDefaults.cardColors(containerColor = StillframeSurface1)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = lore.title, style = StillframeTypography.titleLarge, color = TextPrimary)
                                LoreStatusBadge(status = lore.status)
                            }

                            Text(
                                text = lore.category.uppercase(),
                                style = StillframeTypography.labelSmall,
                                color = accentColor
                            )

                            if (lore.content.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = lore.content,
                                    style = StillframeTypography.bodyMedium,
                                    color = TextSecondary,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(20.dp)) }
            }
        }
    }
}

// ----------------------------------------------------
// TIMELINE SECTION
// ----------------------------------------------------
@Composable
fun TimelineSection(
    timelineEvents: List<TimelineEventEntity>,
    accentColor: Color,
    onEventClick: (TimelineEventEntity) -> Unit,
    onDeleteEvent: (TimelineEventEntity) -> Unit
) {
    if (timelineEvents.isEmpty()) {
        EmptySectionCard("No timeline events yet. Construct chronological historical points and story events.")
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(timelineEvents, key = { it.id }) { event ->
            Row(modifier = Modifier.fillMaxWidth()) {
                // Chronological line node
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 12.dp, top = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (event.isTentative) AccentAmber else AccentSteelBlue)
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(60.dp)
                            .background(StillframeBorder)
                    )
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, StillframeBorder, RoundedCornerShape(8.dp))
                        .clickable { onEventClick(event) },
                    colors = CardDefaults.cardColors(containerColor = StillframeSurface1)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = event.title, style = StillframeTypography.titleMedium, color = TextPrimary)

                            if (event.isTentative) {
                                Text(
                                    text = "TENTATIVE",
                                    style = StillframeTypography.labelSmall,
                                    color = AccentAmber,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(StillframeSurface2)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (event.storyDateString.isNotBlank()) {
                            Text(text = event.storyDateString, style = StillframeTypography.labelSmall, color = TextTertiary)
                        }

                        if (event.summary.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(text = event.summary, style = StillframeTypography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// CREATIVE NOTES SECTION
// ----------------------------------------------------
@Composable
fun CreativeNotesSection(
    notes: List<CreativeNoteEntity>,
    accentColor: Color,
    onNoteClick: (CreativeNoteEntity) -> Unit,
    onDeleteNote: (CreativeNoteEntity) -> Unit
) {
    if (notes.isEmpty()) {
        EmptySectionCard("No creative notes. Jot down loose dialogue fragments, research tidbits, and atmospheric music.")
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(notes, key = { it.id }) { note ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, StillframeBorder, RoundedCornerShape(8.dp))
                    .clickable { onNoteClick(note) },
                colors = CardDefaults.cardColors(containerColor = StillframeSurface1)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = note.title, style = StillframeTypography.titleMedium, color = TextPrimary)
                        Text(
                            text = note.category.uppercase(),
                            style = StillframeTypography.labelSmall,
                            color = accentColor,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(StillframeSurface2)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (note.content.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = note.content,
                            style = StillframeTypography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// SHARED DIALOGS & BADGES
// ----------------------------------------------------
@Composable
fun StatusBadge(status: String) {
    val (color, label) = when (status.lowercase()) {
        "active" -> Pair(StatusConfirmed, "Active")
        "missing" -> Pair(StatusTentative, "Missing")
        "deceased" -> Pair(StatusDeprecated, "Deceased")
        else -> Pair(TextTertiary, status)
    }

    Text(
        text = label.uppercase(),
        style = StillframeTypography.labelSmall,
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(StillframeSurface2)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
fun LoreStatusBadge(status: String) {
    val color = when (status.lowercase()) {
        "confirmed" -> StatusConfirmed
        "tentative" -> StatusTentative
        "deprecated" -> StatusDeprecated
        else -> TextTertiary
    }
    Text(
        text = status.uppercase(),
        style = StillframeTypography.labelSmall,
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
fun EmptySectionCard(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = StillframeTypography.bodyMedium,
            color = TextTertiary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// ----------------------------------------------------
// FULL ENTITY EDITORS
// ----------------------------------------------------
@Composable
fun CharacterEditDialog(
    character: CharacterEntity,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (CharacterEntity) -> Unit
) {
    var name by remember { mutableStateOf(character.name) }
    var aliases by remember { mutableStateOf(character.aliases) }
    var summary by remember { mutableStateOf(character.shortSummary) }
    var bio by remember { mutableStateOf(character.bio) }
    var goals by remember { mutableStateOf(character.goalsMotivations) }
    var traits by remember { mutableStateOf(character.personalityTraits) }
    var strengthsFlaws by remember { mutableStateOf(character.strengthsFlaws) }
    var fears by remember { mutableStateOf(character.fearsConflicts) }
    var appearance by remember { mutableStateOf(character.appearance) }
    var possessions by remember { mutableStateOf(character.possessions) }
    var status by remember { mutableStateOf(character.status) }
    var secrets by remember { mutableStateOf(character.secretsKnowledge) }
    var notes by remember { mutableStateOf(character.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StillframeSurface2,
        title = {
            Text(if (isNew) "Add Character" else "Edit Character: ${character.name}", style = StillframeTypography.titleLarge, color = TextPrimary)
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name *", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = aliases,
                        onValueChange = { aliases = it },
                        label = { Text("Aliases / Titles", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = status,
                        onValueChange = { status = it },
                        label = { Text("Status (Active, Missing, Deceased, Unknown)", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = summary,
                        onValueChange = { summary = it },
                        label = { Text("Short Summary", color = TextSecondary) },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = goals,
                        onValueChange = { goals = it },
                        label = { Text("Goals & Core Motivation", color = TextSecondary) },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = traits,
                        onValueChange = { traits = it },
                        label = { Text("Personality & Voice", color = TextSecondary) },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = fears,
                        onValueChange = { fears = it },
                        label = { Text("Fears & Internal Conflict", color = TextSecondary) },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = appearance,
                        onValueChange = { appearance = it },
                        label = { Text("Appearance & Identifiers", color = TextSecondary) },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = secrets,
                        onValueChange = { secrets = it },
                        label = { Text("Secrets & Critical Knowledge", color = TextSecondary) },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Background & History", color = TextSecondary) },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            character.copy(
                                name = name,
                                aliases = aliases,
                                shortSummary = summary,
                                bio = bio,
                                goalsMotivations = goals,
                                personalityTraits = traits,
                                strengthsFlaws = strengthsFlaws,
                                fearsConflicts = fears,
                                appearance = appearance,
                                possessions = possessions,
                                status = status,
                                secretsKnowledge = secrets,
                                notes = notes
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)
            ) {
                Text("Save Character", color = StillframeBlack)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
fun RelationshipEditDialog(
    relationship: RelationshipEntity,
    characters: List<CharacterEntity>,
    onDismiss: () -> Unit,
    onSave: (RelationshipEntity) -> Unit
) {
    var char1Id by remember { mutableStateOf(relationship.characterId1) }
    var char2Id by remember { mutableStateOf(relationship.characterId2) }
    var relType by remember { mutableStateOf(relationship.relationType) }
    var desc by remember { mutableStateOf(relationship.description) }
    var state by remember { mutableStateOf(relationship.currentState) }
    var history by remember { mutableStateOf(relationship.historyNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StillframeSurface2,
        title = { Text("Character Relationship", style = StillframeTypography.titleLarge, color = TextPrimary) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = relType,
                    onValueChange = { relType = it },
                    label = { Text("Relationship Type", color = TextSecondary) },
                    placeholder = { Text("e.g. Rivalry, Alliance, Family, Romance", color = TextTertiary) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description & Dynamic", color = TextSecondary) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state,
                    onValueChange = { state = it },
                    label = { Text("Current Evolution / State", color = TextSecondary) },
                    placeholder = { Text("e.g. Fragile mutual trust", color = TextTertiary) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = history,
                    onValueChange = { history = it },
                    label = { Text("Relationship History / Evolution Notes", color = TextSecondary) },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val c1Name = characters.firstOrNull { it.id == char1Id }?.name ?: relationship.characterName1
                    val c2Name = characters.firstOrNull { it.id == char2Id }?.name ?: relationship.characterName2
                    onSave(
                        relationship.copy(
                            characterName1 = c1Name,
                            characterName2 = c2Name,
                            relationType = relType,
                            description = desc,
                            currentState = state,
                            historyNotes = history
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)
            ) { Text("Save Relationship", color = StillframeBlack) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) } }
    )
}

@Composable
fun LocationEditDialog(
    location: LocationEntity,
    onDismiss: () -> Unit,
    onSave: (LocationEntity) -> Unit
) {
    var name by remember { mutableStateOf(location.name) }
    var atmosphere by remember { mutableStateOf(location.atmosphere) }
    var description by remember { mutableStateOf(location.description) }
    var sensory by remember { mutableStateOf(location.sensoryDetails) }
    var associated by remember { mutableStateOf(location.associatedCharacters) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StillframeSurface2,
        title = { Text("Location", style = StillframeTypography.titleLarge, color = TextPrimary) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Location Name *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = atmosphere, onValueChange = { atmosphere = it }, label = { Text("Atmosphere / Mood") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = sensory, onValueChange = { sensory = it }, label = { Text("Sensory Details (Sound, Smell, Light)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description & Geography") }, maxLines = 3, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) onSave(location.copy(name = name, atmosphere = atmosphere, sensoryDetails = sensory, description = description))
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)
            ) { Text("Save Location", color = StillframeBlack) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun LoreEditDialog(
    lore: LoreEntity,
    onDismiss: () -> Unit,
    onSave: (LoreEntity) -> Unit
) {
    var title by remember { mutableStateOf(lore.title) }
    var content by remember { mutableStateOf(lore.content) }
    var category by remember { mutableStateOf(lore.category) }
    var status by remember { mutableStateOf(lore.status) }
    var notes by remember { mutableStateOf(lore.authorNotes) }

    val statusChoices = listOf("Confirmed", "Tentative", "Deprecated")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StillframeSurface2,
        title = { Text("Lore & World Rule", style = StillframeTypography.titleLarge, color = TextPrimary) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Rule / Lore Title *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (History, Tech/Magic, Faction, Law)") }, modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    statusChoices.forEach { st ->
                        val isSelected = status == st
                        FilterChip(
                            selected = isSelected,
                            onClick = { status = st },
                            label = { Text(st) }
                        )
                    }
                }

                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Rule Content / Established Fact") }, maxLines = 4, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Author Notes / Creative Questions") }, maxLines = 2, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) onSave(lore.copy(title = title, category = category, status = status, content = content, authorNotes = notes))
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)
            ) { Text("Save Lore Rule", color = StillframeBlack) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun TimelineEditDialog(
    event: TimelineEventEntity,
    onDismiss: () -> Unit,
    onSave: (TimelineEventEntity) -> Unit
) {
    var title by remember { mutableStateOf(event.title) }
    var dateStr by remember { mutableStateOf(event.storyDateString) }
    var summary by remember { mutableStateOf(event.summary) }
    var isTentative by remember { mutableStateOf(event.isTentative) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StillframeSurface2,
        title = { Text("Timeline Event", style = StillframeTypography.titleLarge, color = TextPrimary) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Event Title *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = dateStr, onValueChange = { dateStr = it }, label = { Text("Story Date (e.g. Year 41, Post-Severance)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = summary, onValueChange = { summary = it }, label = { Text("Event Summary") }, maxLines = 3, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isTentative, onCheckedChange = { isTentative = it })
                    Text("Tentative Event (Unconfirmed in story canon)", style = StillframeTypography.bodyMedium, color = TextSecondary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) onSave(event.copy(title = title, storyDateString = dateStr, summary = summary, isTentative = isTentative))
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)
            ) { Text("Save Event", color = StillframeBlack) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun CreativeNoteEditDialog(
    note: CreativeNoteEntity,
    onDismiss: () -> Unit,
    onSave: (CreativeNoteEntity) -> Unit
) {
    var title by remember { mutableStateOf(note.title) }
    var category by remember { mutableStateOf(note.category) }
    var content by remember { mutableStateOf(note.content) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StillframeSurface2,
        title = { Text("Creative Note", style = StillframeTypography.titleLarge, color = TextPrimary) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Note Title *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (Idea, Dialogue Fragment, Mood, Research)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Note Content") }, maxLines = 5, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) onSave(note.copy(title = title, category = category, content = content))
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)
            ) { Text("Save Note", color = StillframeBlack) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
