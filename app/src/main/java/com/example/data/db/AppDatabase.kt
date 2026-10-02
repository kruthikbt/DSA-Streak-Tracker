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
    version = 5,
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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Reset legacy hardcoded sample solved problems to completely unsolved state
                db.execSQL("""
                    UPDATE problems 
                    SET solved = 0, solvedDate = NULL, lastRevisedDate = NULL, revisionNotes = ''
                    WHERE title IN ('Contains Duplicate', 'Two Sum II - Input Array Is Sorted', 'Valid Palindrome', 'Two Sum')
                      AND (revisionNotes LIKE '%Hash set for O(1)%' 
                        OR revisionNotes LIKE '%Opposite pointers%' 
                        OR revisionNotes LIKE '%Inward dual pointers%' 
                        OR revisionNotes LIKE '%One-pass hash map%')
                """.trimIndent())
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Delete legacy demo logs to guarantee a completely fresh zero-activity heatmap for new users
                db.execSQL("""
                    DELETE FROM daily_logs 
                    WHERE notes LIKE '%Mastered Sliding Window%'
                       OR notes LIKE '%Container With Most Water%'
                       OR notes LIKE '%3Sum using sorted array%'
                       OR notes LIKE '%HashMap frequency counting%'
                       OR notes LIKE '%Subarray Sum Equals K%'
                       OR notes LIKE '%Valid Palindrome with alphanumeric regex clean%'
                       OR notes LIKE '%Longest Common Prefix horizontal%'
                       OR notes LIKE '%Kadane''s algorithm for max subarray%'
                       OR notes LIKE '%Two Sum optimal O(n) map approach%'
                       OR notes LIKE '%Rotate array in-place reversing 3 sections%'
                       OR notes LIKE '%Big-O space complexity of recursive call stack%'
                       OR notes LIKE '%Time complexity comparison of binary search%'
                """.trimIndent())
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_settings ADD COLUMN reminderEnabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN lastReminderDate TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dsa_streak_tracker.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
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
