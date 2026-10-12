package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.critique.*
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.SearchResultItem
import com.example.data.repository.StillframeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppDestination {
    UNIVERSE,
    WORLD,
    DRAFTS,
    REVIEW
}

enum class WorldTab(val label: String) {
    CHARACTERS("Characters"),
    RELATIONSHIPS("Relationships"),
    LOCATIONS("Locations"),
    LORE("Lore & Rules"),
    TIMELINE("Timeline"),
    NOTES("Creative Notes")
}

class StillframeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val repository = StillframeRepository(db)
    val localModelManager = LocalModelManager(application)

    // Current Navigation Destination
    private val _currentDestination = MutableStateFlow(AppDestination.UNIVERSE)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    // World Sub-tab
    private val _currentWorldTab = MutableStateFlow(WorldTab.CHARACTERS)
    val currentWorldTab: StateFlow<WorldTab> = _currentWorldTab.asStateFlow()

    // Projects
    val allProjects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Project
    private val _activeProjectId = MutableStateFlow<String?>(null)
    val activeProjectId: StateFlow<String?> = _activeProjectId.asStateFlow()

    val activeProject: StateFlow<ProjectEntity?> = _activeProjectId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.getProject(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Active Project Sub-Entities
    val characters: StateFlow<List<CharacterEntity>> = _activeProjectId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getCharacters(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val relationships: StateFlow<List<RelationshipEntity>> = _activeProjectId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getRelationships(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val locations: StateFlow<List<LocationEntity>> = _activeProjectId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getLocations(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val loreList: StateFlow<List<LoreEntity>> = _activeProjectId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getLore(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timelineEvents: StateFlow<List<TimelineEventEntity>> = _activeProjectId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getTimeline(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chapters: StateFlow<List<ChapterEntity>> = _activeProjectId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getChapters(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scenes: StateFlow<List<SceneEntity>> = _activeProjectId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getScenes(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val creativeNotes: StateFlow<List<CreativeNoteEntity>> = _activeProjectId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getCreativeNotes(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Scene in Drafts / Editor
    private val _activeSceneId = MutableStateFlow<String?>(null)
    val activeSceneId: StateFlow<String?> = _activeSceneId.asStateFlow()

    val activeScene: StateFlow<SceneEntity?> = _activeSceneId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.getScene(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeSceneRevisions: StateFlow<List<SceneRevisionEntity>> = _activeSceneId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getRevisions(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResultItem>>(emptyList())
    val searchResults: StateFlow<List<SearchResultItem>> = _searchResults.asStateFlow()

    // Focus mode in writing editor
    private val _isFocusMode = MutableStateFlow(false)
    val isFocusMode: StateFlow<Boolean> = _isFocusMode.asStateFlow()

    // Autosave status text
    private val _saveStatusText = MutableStateFlow("All changes saved locally")
    val saveStatusText: StateFlow<String> = _saveStatusText.asStateFlow()

    // Review / Editorial Critic
    private val _selectedReviewSceneId = MutableStateFlow<String?>(null)
    val selectedReviewSceneId: StateFlow<String?> = _selectedReviewSceneId.asStateFlow()

    private val _selectedCritiqueMode = MutableStateFlow(CritiqueMode.DIALOGUE)
    val selectedCritiqueMode: StateFlow<CritiqueMode> = _selectedCritiqueMode.asStateFlow()

    private val _critiqueFindings = MutableStateFlow<List<CritiqueFinding>>(emptyList())
    val critiqueFindings: StateFlow<List<CritiqueFinding>> = _critiqueFindings.asStateFlow()

    private val _sceneMetrics = MutableStateFlow<SceneMetrics?>(null)
    val sceneMetrics: StateFlow<SceneMetrics?> = _sceneMetrics.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private var autosaveJob: Job? = null

    init {
        viewModelScope.launch {
            repository.seedSampleIfEmpty()
            // Set first project as active if none selected
            allProjects.filter { it.isNotEmpty() }.first().let { list ->
                if (_activeProjectId.value == null) {
                    _activeProjectId.value = list.firstOrNull { !it.isArchived }?.id ?: list.first().id
                }
            }
        }
    }

    fun setDestination(dest: AppDestination) {
        _currentDestination.value = dest
    }

    fun setWorldTab(tab: WorldTab) {
        _currentWorldTab.value = tab
    }

    fun setActiveProject(projectId: String) {
        _activeProjectId.value = projectId
        _activeSceneId.value = null
        _selectedReviewSceneId.value = null
        _critiqueFindings.value = emptyList()
        _sceneMetrics.value = null
    }

    fun setActiveScene(sceneId: String?) {
        _activeSceneId.value = sceneId
        if (sceneId != null) {
            _selectedReviewSceneId.value = sceneId
        }
    }

    fun setFocusMode(enabled: Boolean) {
        _isFocusMode.value = enabled
    }

    // Search query update
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            val results = repository.searchUniverse(query, _activeProjectId.value)
            _searchResults.value = results
        }
    }

    // Autosave scene content
    fun updateSceneContent(newContent: String, immediateSnapshot: Boolean = false) {
        val current = activeScene.value ?: return
        val wordCount = newContent.split(Regex("\\s+")).count { it.isNotBlank() }
        _saveStatusText.value = "Saving..."

        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            delay(if (immediateSnapshot) 0 else 600) // Debounce autosave gracefully
            repository.saveScene(current.copy(content = newContent), createSnapshot = immediateSnapshot)
            _saveStatusText.value = "Saved locally (${wordCount} words)"
        }
    }

    fun updateSceneMetadata(title: String, objective: String, intent: String, format: String, charactersAttached: String) {
        val current = activeScene.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                title = title,
                sceneObjective = objective,
                emotionalIntent = intent,
                format = format,
                attachedCharacterIds = charactersAttached
            )
            repository.saveScene(updated, createSnapshot = false)
        }
    }

    fun restoreRevision(revision: SceneRevisionEntity) {
        val current = activeScene.value ?: return
        viewModelScope.launch {
            repository.saveScene(
                current.copy(
                    content = revision.content,
                    title = revision.title,
                    format = revision.format
                ),
                createSnapshot = true
            )
            _saveStatusText.value = "Restored snapshot from ${formatTimestamp(revision.savedAt)}"
        }
    }

    // Projects CRUD
    fun createProject(title: String, description: String, genre: String, tags: String, accentHex: String) {
        viewModelScope.launch {
            val newProject = ProjectEntity(
                id = UUID.randomUUID().toString(),
                title = title.ifBlank { "Untitled Universe" },
                description = description,
                genre = genre,
                tags = tags,
                accentColorHex = accentHex,
                isArchived = false,
                isSample = false
            )
            repository.saveProject(newProject)
            _activeProjectId.value = newProject.id
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            if (_activeProjectId.value == projectId) {
                val remaining = allProjects.value.filter { it.id != projectId }
                _activeProjectId.value = remaining.firstOrNull()?.id
            }
        }
    }

    fun loadSampleUniverse() {
        viewModelScope.launch {
            repository.seedSampleUniverse()
            _activeProjectId.value = com.example.data.sample.SampleUniverseSeeder.SAMPLE_PROJECT_ID
        }
    }

    fun removeSampleUniverse() {
        viewModelScope.launch {
            repository.removeSampleUniverse()
            if (_activeProjectId.value == com.example.data.sample.SampleUniverseSeeder.SAMPLE_PROJECT_ID) {
                _activeProjectId.value = allProjects.value.firstOrNull { !it.isSample }?.id
            }
        }
    }

    // World Entities CRUD
    fun saveCharacter(character: CharacterEntity) {
        viewModelScope.launch { repository.saveCharacter(character) }
    }

    fun deleteCharacter(characterId: String) {
        viewModelScope.launch { repository.deleteCharacter(characterId) }
    }

    fun saveRelationship(rel: RelationshipEntity) {
        viewModelScope.launch { repository.saveRelationship(rel) }
    }

    fun deleteRelationship(relId: String) {
        viewModelScope.launch { repository.deleteRelationship(relId) }
    }

    fun saveLocation(loc: LocationEntity) {
        viewModelScope.launch { repository.saveLocation(loc) }
    }

    fun deleteLocation(locId: String) {
        viewModelScope.launch { repository.deleteLocation(locId) }
    }

    fun saveLore(lore: LoreEntity) {
        viewModelScope.launch { repository.saveLore(lore) }
    }

    fun deleteLore(loreId: String) {
        viewModelScope.launch { repository.deleteLore(loreId) }
    }

    fun saveTimelineEvent(event: TimelineEventEntity) {
        viewModelScope.launch { repository.saveTimelineEvent(event) }
    }

    fun deleteTimelineEvent(eventId: String) {
        viewModelScope.launch { repository.deleteTimelineEvent(eventId) }
    }

    fun saveCreativeNote(note: CreativeNoteEntity) {
        viewModelScope.launch { repository.saveCreativeNote(note) }
    }

    fun deleteCreativeNote(noteId: String) {
        viewModelScope.launch { repository.deleteCreativeNote(noteId) }
    }

    // Drafts CRUD
    fun createChapter(title: String) {
        val projId = _activeProjectId.value ?: return
        viewModelScope.launch {
            val maxOrder = chapters.value.maxOfOrNull { it.orderIndex } ?: 0
            val chapter = ChapterEntity(
                id = UUID.randomUUID().toString(),
                projectId = projId,
                title = title.ifBlank { "Chapter ${chapters.value.size + 1}" },
                orderIndex = maxOrder + 1
            )
            repository.saveChapter(chapter)
        }
    }

    fun deleteChapter(chapterId: String) {
        viewModelScope.launch { repository.deleteChapter(chapterId) }
    }

    fun createScene(title: String, format: String, chapterId: String? = null) {
        val projId = _activeProjectId.value ?: return
        viewModelScope.launch {
            val maxOrder = scenes.value.maxOfOrNull { it.orderIndex } ?: 0
            val scene = SceneEntity(
                id = UUID.randomUUID().toString(),
                projectId = projId,
                chapterId = chapterId,
                title = title.ifBlank { "Scene ${scenes.value.size + 1}" },
                format = format,
                orderIndex = maxOrder + 1,
                content = if (format == "SCREENPLAY") "INT. LOCATION - NIGHT\n\nAction description goes here.\n\nCHARACTER\nDialogue goes here." else "The scene began with..."
            )
            repository.saveScene(scene, createSnapshot = true)
            _activeSceneId.value = scene.id
        }
    }

    fun deleteScene(sceneId: String) {
        viewModelScope.launch {
            repository.deleteScene(sceneId)
            if (_activeSceneId.value == sceneId) {
                _activeSceneId.value = null
            }
        }
    }

    // Review / Editorial Critic
    fun selectReviewScene(sceneId: String) {
        _selectedReviewSceneId.value = sceneId
        _critiqueFindings.value = emptyList()
        _sceneMetrics.value = null
    }

    fun selectCritiqueMode(mode: CritiqueMode) {
        _selectedCritiqueMode.value = mode
    }

    fun runEditorialAnalysis() {
        val sceneId = _selectedReviewSceneId.value ?: activeSceneId.value ?: scenes.value.firstOrNull()?.id ?: return
        val scene = scenes.value.firstOrNull { it.id == sceneId } ?: return
        val chars = characters.value
        val lore = loreList.value
        val mode = _selectedCritiqueMode.value

        viewModelScope.launch {
            _isAnalyzing.value = true
            delay(350) // Smooth progress state
            val (metrics, findings) = EditorialAnalysisEngine.analyzeScene(scene, chars, lore, mode)
            _sceneMetrics.value = metrics
            _critiqueFindings.value = findings
            _isAnalyzing.value = false
        }
    }

    fun dismissFinding(findingId: String) {
        _critiqueFindings.value = _critiqueFindings.value.filter { it.id != findingId }
    }

    fun copyFindingToNotes(finding: CritiqueFinding) {
        val projId = _activeProjectId.value ?: return
        viewModelScope.launch {
            val note = CreativeNoteEntity(
                id = UUID.randomUUID().toString(),
                projectId = projId,
                title = "Critique: ${finding.category}",
                content = "Finding (${finding.mode.label}):\n${finding.explanation}\n\nEvidence: \"${finding.evidenceExcerpt}\"\n${finding.suggestion?.let { "Suggestion: $it\n" } ?: ""}${finding.authorQuestion?.let { "Question: $it" } ?: ""}",
                category = "Observation"
            )
            repository.saveCreativeNote(note)
        }
    }

    private fun formatTimestamp(millis: Long): String {
        val sdf = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(millis))
    }
}
