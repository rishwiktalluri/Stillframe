package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.sample.SampleUniverseSeeder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

data class SearchResultItem(
    val entityType: String, // Project, Character, Lore, Timeline, Scene, Note
    val id: String,
    val projectId: String,
    val title: String,
    val snippet: String,
    val matchField: String
)

class StillframeRepository(private val db: AppDatabase) {

    val activeProjects: Flow<List<ProjectEntity>> = db.projectDao().getActiveProjects()
    val allProjects: Flow<List<ProjectEntity>> = db.projectDao().getAllProjects()

    fun getProject(id: String): Flow<ProjectEntity?> = db.projectDao().getProjectById(id)
    suspend fun getProjectOnce(id: String): ProjectEntity? = db.projectDao().getProjectByIdOnce(id)

    suspend fun saveProject(project: ProjectEntity) {
        db.projectDao().insertOrUpdate(project.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteProject(projectId: String) {
        db.characterDao().deleteAllForProject(projectId)
        db.relationshipDao().deleteAllForProject(projectId)
        db.locationDao().deleteAllForProject(projectId)
        db.loreDao().deleteAllForProject(projectId)
        db.timelineDao().deleteAllForProject(projectId)
        db.chapterDao().deleteAllForProject(projectId)
        db.sceneDao().deleteAllForProject(projectId)
        db.creativeNoteDao().deleteAllForProject(projectId)
        db.projectDao().deleteById(projectId)
    }

    suspend fun seedSampleIfEmpty() {
        val projects = db.projectDao().getAllProjects().firstOrNull() ?: emptyList()
        if (projects.isEmpty()) {
            SampleUniverseSeeder.seedSampleUniverse(db)
        }
    }

    suspend fun seedSampleUniverse() {
        SampleUniverseSeeder.seedSampleUniverse(db)
    }

    suspend fun removeSampleUniverse() {
        SampleUniverseSeeder.removeSampleUniverse(db)
    }

    // Characters
    fun getCharacters(projectId: String): Flow<List<CharacterEntity>> =
        db.characterDao().getCharactersForProject(projectId)

    suspend fun saveCharacter(character: CharacterEntity) =
        db.characterDao().insertOrUpdate(character.copy(updatedAt = System.currentTimeMillis()))

    suspend fun deleteCharacter(id: String) = db.characterDao().deleteById(id)

    // Relationships
    fun getRelationships(projectId: String): Flow<List<RelationshipEntity>> =
        db.relationshipDao().getRelationshipsForProject(projectId)

    suspend fun saveRelationship(rel: RelationshipEntity) =
        db.relationshipDao().insertOrUpdate(rel.copy(updatedAt = System.currentTimeMillis()))

    suspend fun deleteRelationship(id: String) = db.relationshipDao().deleteById(id)

    // Locations
    fun getLocations(projectId: String): Flow<List<LocationEntity>> =
        db.locationDao().getLocationsForProject(projectId)

    suspend fun saveLocation(loc: LocationEntity) =
        db.locationDao().insertOrUpdate(loc.copy(updatedAt = System.currentTimeMillis()))

    suspend fun deleteLocation(id: String) = db.locationDao().deleteById(id)

    // Lore
    fun getLore(projectId: String): Flow<List<LoreEntity>> =
        db.loreDao().getLoreForProject(projectId)

    suspend fun saveLore(lore: LoreEntity) =
        db.loreDao().insertOrUpdate(lore.copy(updatedAt = System.currentTimeMillis()))

    suspend fun deleteLore(id: String) = db.loreDao().deleteById(id)

    // Timeline
    fun getTimeline(projectId: String): Flow<List<TimelineEventEntity>> =
        db.timelineDao().getTimelineEventsForProject(projectId)

    suspend fun saveTimelineEvent(event: TimelineEventEntity) =
        db.timelineDao().insertOrUpdate(event.copy(updatedAt = System.currentTimeMillis()))

    suspend fun deleteTimelineEvent(id: String) = db.timelineDao().deleteById(id)

    // Chapters
    fun getChapters(projectId: String): Flow<List<ChapterEntity>> =
        db.chapterDao().getChaptersForProject(projectId)

    suspend fun saveChapter(chapter: ChapterEntity) =
        db.chapterDao().insertOrUpdate(chapter.copy(updatedAt = System.currentTimeMillis()))

    suspend fun deleteChapter(id: String) = db.chapterDao().deleteById(id)

    // Scenes
    fun getScenes(projectId: String): Flow<List<SceneEntity>> =
        db.sceneDao().getScenesForProject(projectId)

    fun getScene(id: String): Flow<SceneEntity?> = db.sceneDao().getSceneById(id)
    suspend fun getSceneOnce(id: String): SceneEntity? = db.sceneDao().getSceneByIdOnce(id)

    suspend fun saveScene(scene: SceneEntity, createSnapshot: Boolean = false) {
        val wordCount = scene.content.split(Regex("\\s+")).count { it.isNotBlank() }
        val updated = scene.copy(wordCount = wordCount, updatedAt = System.currentTimeMillis())
        db.sceneDao().insertOrUpdate(updated)

        if (createSnapshot) {
            val rev = SceneRevisionEntity(
                id = UUID.randomUUID().toString(),
                sceneId = scene.id,
                title = scene.title,
                content = scene.content,
                format = scene.format,
                savedAt = System.currentTimeMillis(),
                summary = "Snapshot at $wordCount words"
            )
            db.sceneRevisionDao().insert(rev)
        }
    }

    suspend fun deleteScene(id: String) = db.sceneDao().deleteById(id)

    // Revisions
    fun getRevisions(sceneId: String): Flow<List<SceneRevisionEntity>> =
        db.sceneRevisionDao().getRevisionsForScene(sceneId)

    // Creative Notes
    fun getCreativeNotes(projectId: String): Flow<List<CreativeNoteEntity>> =
        db.creativeNoteDao().getNotesForProject(projectId)

    suspend fun saveCreativeNote(note: CreativeNoteEntity) =
        db.creativeNoteDao().insertOrUpdate(note.copy(updatedAt = System.currentTimeMillis()))

    suspend fun deleteCreativeNote(id: String) = db.creativeNoteDao().deleteById(id)

    // Universal Offline Search
    suspend fun searchUniverse(query: String, projectId: String? = null): List<SearchResultItem> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()

        val results = mutableListOf<SearchResultItem>()

        val projects = db.projectDao().getAllProjects().firstOrNull() ?: emptyList()
        val targetProjects = if (projectId != null) projects.filter { it.id == projectId } else projects

        for (p in targetProjects) {
            if (p.title.lowercase().contains(q) || p.description.lowercase().contains(q) || p.genre.lowercase().contains(q)) {
                results.add(
                    SearchResultItem(
                        entityType = "Project",
                        id = p.id,
                        projectId = p.id,
                        title = p.title,
                        snippet = p.description.take(120),
                        matchField = if (p.title.lowercase().contains(q)) "Title" else "Description"
                    )
                )
            }

            // Characters
            val chars = db.characterDao().getCharactersForProject(p.id).firstOrNull() ?: emptyList()
            for (c in chars) {
                if (c.name.lowercase().contains(q) || c.bio.lowercase().contains(q) || c.shortSummary.lowercase().contains(q) || c.personalityTraits.lowercase().contains(q)) {
                    results.add(
                        SearchResultItem(
                            entityType = "Character",
                            id = c.id,
                            projectId = p.id,
                            title = c.name,
                            snippet = (if (c.shortSummary.isNotBlank()) c.shortSummary else c.bio).take(120),
                            matchField = if (c.name.lowercase().contains(q)) "Name" else "Bio/Summary"
                        )
                    )
                }
            }

            // Lore
            val lore = db.loreDao().getLoreForProject(p.id).firstOrNull() ?: emptyList()
            for (l in lore) {
                if (l.title.lowercase().contains(q) || l.content.lowercase().contains(q) || l.category.lowercase().contains(q)) {
                    results.add(
                        SearchResultItem(
                            entityType = "Lore",
                            id = l.id,
                            projectId = p.id,
                            title = l.title,
                            snippet = l.content.take(120),
                            matchField = if (l.title.lowercase().contains(q)) "Title" else "Content"
                        )
                    )
                }
            }

            // Timeline
            val timeline = db.timelineDao().getTimelineEventsForProject(p.id).firstOrNull() ?: emptyList()
            for (t in timeline) {
                if (t.title.lowercase().contains(q) || t.summary.lowercase().contains(q)) {
                    results.add(
                        SearchResultItem(
                            entityType = "Timeline",
                            id = t.id,
                            projectId = p.id,
                            title = t.title,
                            snippet = t.summary.take(120),
                            matchField = if (t.title.lowercase().contains(q)) "Title" else "Summary"
                        )
                    )
                }
            }

            // Scenes
            val scenes = db.sceneDao().getScenesForProject(p.id).firstOrNull() ?: emptyList()
            for (s in scenes) {
                if (s.title.lowercase().contains(q) || s.content.lowercase().contains(q)) {
                    val idx = s.content.lowercase().indexOf(q)
                    val snippet = if (idx >= 0) {
                        val start = (idx - 30).coerceAtLeast(0)
                        val end = (idx + 70).coerceAtMost(s.content.length)
                        "..." + s.content.substring(start, end).replace("\n", " ") + "..."
                    } else {
                        s.content.take(120).replace("\n", " ")
                    }
                    results.add(
                        SearchResultItem(
                            entityType = "Scene (${s.format})",
                            id = s.id,
                            projectId = p.id,
                            title = s.title,
                            snippet = snippet,
                            matchField = if (s.title.lowercase().contains(q)) "Title" else "Manuscript Content"
                        )
                    )
                }
            }

            // Notes
            val notes = db.creativeNoteDao().getNotesForProject(p.id).firstOrNull() ?: emptyList()
            for (n in notes) {
                if (n.title.lowercase().contains(q) || n.content.lowercase().contains(q)) {
                    results.add(
                        SearchResultItem(
                            entityType = "Creative Note",
                            id = n.id,
                            projectId = p.id,
                            title = n.title,
                            snippet = n.content.take(120),
                            matchField = if (n.title.lowercase().contains(q)) "Title" else "Note"
                        )
                    )
                }
            }
        }

        return results
    }
}
