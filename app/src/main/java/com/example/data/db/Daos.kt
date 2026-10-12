package com.example.data.db

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects WHERE isArchived = 0 ORDER BY updatedAt DESC")
    fun getActiveProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    fun getProjectById(id: String): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectByIdOnce(id: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(project: ProjectEntity)

    @Delete
    suspend fun delete(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters WHERE projectId = :projectId ORDER BY name ASC")
    fun getCharactersForProject(projectId: String): Flow<List<CharacterEntity>>

    @Query("SELECT * FROM characters WHERE id = :id LIMIT 1")
    suspend fun getCharacterById(id: String): CharacterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(character: CharacterEntity)

    @Delete
    suspend fun delete(character: CharacterEntity)

    @Query("DELETE FROM characters WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM characters WHERE projectId = :projectId")
    suspend fun deleteAllForProject(projectId: String)
}

@Dao
interface RelationshipDao {
    @Query("SELECT * FROM relationships WHERE projectId = :projectId ORDER BY updatedAt DESC")
    fun getRelationshipsForProject(projectId: String): Flow<List<RelationshipEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(relationship: RelationshipEntity)

    @Delete
    suspend fun delete(relationship: RelationshipEntity)

    @Query("DELETE FROM relationships WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM relationships WHERE projectId = :projectId")
    suspend fun deleteAllForProject(projectId: String)
}

@Dao
interface LocationDao {
    @Query("SELECT * FROM locations WHERE projectId = :projectId ORDER BY name ASC")
    fun getLocationsForProject(projectId: String): Flow<List<LocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(location: LocationEntity)

    @Delete
    suspend fun delete(location: LocationEntity)

    @Query("DELETE FROM locations WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM locations WHERE projectId = :projectId")
    suspend fun deleteAllForProject(projectId: String)
}

@Dao
interface LoreDao {
    @Query("SELECT * FROM lore WHERE projectId = :projectId ORDER BY title ASC")
    fun getLoreForProject(projectId: String): Flow<List<LoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(lore: LoreEntity)

    @Delete
    suspend fun delete(lore: LoreEntity)

    @Query("DELETE FROM lore WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM lore WHERE projectId = :projectId")
    suspend fun deleteAllForProject(projectId: String)
}

@Dao
interface TimelineDao {
    @Query("SELECT * FROM timeline_events WHERE projectId = :projectId ORDER BY chronologicalOrder ASC")
    fun getTimelineEventsForProject(projectId: String): Flow<List<TimelineEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(event: TimelineEventEntity)

    @Delete
    suspend fun delete(event: TimelineEventEntity)

    @Query("DELETE FROM timeline_events WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM timeline_events WHERE projectId = :projectId")
    suspend fun deleteAllForProject(projectId: String)
}

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE projectId = :projectId ORDER BY orderIndex ASC")
    fun getChaptersForProject(projectId: String): Flow<List<ChapterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(chapter: ChapterEntity)

    @Delete
    suspend fun delete(chapter: ChapterEntity)

    @Query("DELETE FROM chapters WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM chapters WHERE projectId = :projectId")
    suspend fun deleteAllForProject(projectId: String)
}

@Dao
interface SceneDao {
    @Query("SELECT * FROM scenes WHERE projectId = :projectId ORDER BY orderIndex ASC")
    fun getScenesForProject(projectId: String): Flow<List<SceneEntity>>

    @Query("SELECT * FROM scenes WHERE id = :id LIMIT 1")
    fun getSceneById(id: String): Flow<SceneEntity?>

    @Query("SELECT * FROM scenes WHERE id = :id LIMIT 1")
    suspend fun getSceneByIdOnce(id: String): SceneEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(scene: SceneEntity)

    @Delete
    suspend fun delete(scene: SceneEntity)

    @Query("DELETE FROM scenes WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM scenes WHERE projectId = :projectId")
    suspend fun deleteAllForProject(projectId: String)
}

@Dao
interface SceneRevisionDao {
    @Query("SELECT * FROM scene_revisions WHERE sceneId = :sceneId ORDER BY savedAt DESC")
    fun getRevisionsForScene(sceneId: String): Flow<List<SceneRevisionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(revision: SceneRevisionEntity)

    @Query("DELETE FROM scene_revisions WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface CreativeNoteDao {
    @Query("SELECT * FROM creative_notes WHERE projectId = :projectId ORDER BY updatedAt DESC")
    fun getNotesForProject(projectId: String): Flow<List<CreativeNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(note: CreativeNoteEntity)

    @Delete
    suspend fun delete(note: CreativeNoteEntity)

    @Query("DELETE FROM creative_notes WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM creative_notes WHERE projectId = :projectId")
    suspend fun deleteAllForProject(projectId: String)
}
