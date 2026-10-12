package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        ProjectEntity::class,
        CharacterEntity::class,
        RelationshipEntity::class,
        LocationEntity::class,
        LoreEntity::class,
        TimelineEventEntity::class,
        ChapterEntity::class,
        SceneEntity::class,
        SceneRevisionEntity::class,
        CreativeNoteEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun characterDao(): CharacterDao
    abstract fun relationshipDao(): RelationshipDao
    abstract fun locationDao(): LocationDao
    abstract fun loreDao(): LoreDao
    abstract fun timelineDao(): TimelineDao
    abstract fun chapterDao(): ChapterDao
    abstract fun sceneDao(): SceneDao
    abstract fun sceneRevisionDao(): SceneRevisionDao
    abstract fun creativeNoteDao(): CreativeNoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stillframe_universe.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
