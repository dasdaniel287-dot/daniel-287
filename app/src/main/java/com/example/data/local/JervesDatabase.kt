package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TaskEntity::class, MemoryEntity::class, ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class JervesDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun memoryDao(): MemoryDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: JervesDatabase? = null

        fun getDatabase(context: Context): JervesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JervesDatabase::class.java,
                    "jerves_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(db: JervesDatabase) {
                // Initial directives
                db.taskDao().insertTask(
                    TaskEntity(
                        title = "Calibrate Neural Architecture",
                        description = "Verify Gemini 3.8 Flash core response latency and memory matrix coherence.",
                        category = TaskCategory.SYSTEM.name,
                        priority = TaskPriority.HIGH.name,
                        isCompleted = false,
                        dueDate = "Today"
                    )
                )
                db.taskDao().insertTask(
                    TaskEntity(
                        title = "Review Daily Briefing Protocol",
                        description = "Inspect morning directives, scheduled milestones, and priority tasks.",
                        category = TaskCategory.DIRECTIVE.name,
                        priority = TaskPriority.CRITICAL.name,
                        isCompleted = false,
                        dueDate = "09:00 AM"
                    )
                )
                db.taskDao().insertTask(
                    TaskEntity(
                        title = "System Diagnostics Check",
                        description = "Audit core subroutines and speech synthesizer telemetry.",
                        category = TaskCategory.RESEARCH.name,
                        priority = TaskPriority.NORMAL.name,
                        isCompleted = true,
                        dueDate = "Completed"
                    )
                )

                // Initial memories
                db.memoryDao().insertMemory(
                    MemoryEntity(
                        title = "User Identity Protocol",
                        content = "Primary user designated as Commander / Sir. Prefers crisp, concise, witty responses with actionable execution steps.",
                        tag = "USER_INFO"
                    )
                )
                db.memoryDao().insertMemory(
                    MemoryEntity(
                        title = "Project Arc Sentinel",
                        content = "Ongoing development of intelligent mobile interface utilizing Gemini 3.8 and Android Compose.",
                        tag = "PROJECT"
                    )
                )

                // Initial chat greeting
                db.chatDao().insertMessage(
                    ChatMessageEntity(
                        sender = "jerves",
                        message = "Good day, sir. All core systems are online and functioning within nominal parameters. How may I assist you today?",
                        isSystemProtocol = false
                    )
                )
            }
        }
    }
}
