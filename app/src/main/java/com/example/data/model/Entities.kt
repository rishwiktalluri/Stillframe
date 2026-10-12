package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String = "",
    val genre: String = "",
    val tags: String = "",
    val accentColorHex: String = "#E8B058", // Default warm amber
    val coverUri: String? = null,
    val isArchived: Boolean = false,
    val isSample: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val name: String,
    val aliases: String = "",
    val shortSummary: String = "",
    val bio: String = "",
    val goalsMotivations: String = "",
    val personalityTraits: String = "",
    val strengthsFlaws: String = "",
    val fearsConflicts: String = "",
    val appearance: String = "",
    val possessions: String = "",
    val status: String = "Active", // Active, Missing, Deceased, Unknown, In Disguise
    val secretsKnowledge: String = "",
    val notes: String = "",
    val imageUri: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "relationships")
data class RelationshipEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val characterId1: String,
    val characterId2: String,
    val characterName1: String,
    val characterName2: String,
    val relationType: String = "Friendship", // Friendship, Family, Romance, Rivalry, Professional, Custom
    val description: String = "",
    val currentState: String = "Intact",
    val historyNotes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val name: String,
    val description: String = "",
    val atmosphere: String = "",
    val sensoryDetails: String = "",
    val associatedCharacters: String = "",
    val notes: String = "",
    val imageUri: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "lore")
data class LoreEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val title: String,
    val content: String = "",
    val category: String = "History", // History, Technology/Magic, Culture, Factions, Laws/Rules, Artifacts
    val status: String = "Confirmed", // Confirmed, Tentative, Deprecated
    val relatedEntities: String = "",
    val authorNotes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "timeline_events")
data class TimelineEventEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val title: String,
    val summary: String = "",
    val chronologicalOrder: Int = 0,
    val storyDateString: String = "",
    val isTentative: Boolean = false,
    val relatedCharacters: String = "",
    val relatedLocations: String = "",
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val title: String,
    val orderIndex: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "scenes")
data class SceneEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val chapterId: String? = null,
    val title: String,
    val format: String = "PROSE", // "PROSE" or "SCREENPLAY"
    val orderIndex: Int = 0,
    val content: String = "",
    val sceneObjective: String = "",
    val emotionalIntent: String = "",
    val notes: String = "",
    val attachedCharacterIds: String = "",
    val attachedLocationIds: String = "",
    val attachedTimelineIds: String = "",
    val wordCount: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "scene_revisions")
data class SceneRevisionEntity(
    @PrimaryKey val id: String,
    val sceneId: String,
    val title: String,
    val content: String,
    val format: String,
    val savedAt: Long = System.currentTimeMillis(),
    val summary: String = "Autosaved snapshot"
)

@Entity(tableName = "creative_notes")
data class CreativeNoteEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val title: String,
    val content: String = "",
    val category: String = "Idea", // Idea, Dialogue Fragment, Research, Mood & Atmosphere, Observation
    val updatedAt: Long = System.currentTimeMillis()
)
