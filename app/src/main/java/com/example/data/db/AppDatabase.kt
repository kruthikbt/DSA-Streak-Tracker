package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DailyLogEntity
import com.example.data.model.ProblemEntity
import com.example.data.model.StreakFreezeEntity
import com.example.data.model.TopicEntity
import com.example.data.model.UserSettingsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TopicEntity::class,
        ProblemEntity::class,
        DailyLogEntity::class,
        StreakFreezeEntity::class,
        UserSettingsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun topicDao(): TopicDao
    abstract fun problemDao(): ProblemDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun streakFreezeDao(): StreakFreezeDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE problems ADD COLUMN lastRevisedDate TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE problems ADD COLUMN revisionNotes TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dsa_streak_tracker.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialDatabase(database)
                    }
                }
            }
        }

        suspend fun populateInitialDatabase(database: AppDatabase) {
            val topics = DefaultRoadmapData.createInitialTopics()
            database.topicDao().insertTopics(topics)
            val problems = DefaultRoadmapData.createInitialProblems()
            database.problemDao().insertProblems(problems)
            database.userSettingsDao().insertOrUpdateSettings(UserSettingsEntity())
        }
    }
}
