package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey val id: Int,
    val order: Int,
    val name: String,
    val status: String = STATUS_NOT_STARTED, // NOT_STARTED, IN_PROGRESS, COMPLETED
    val subtopicsJson: String = "[]"
) {
    companion object {
        const val STATUS_NOT_STARTED = "Not Started"
        const val STATUS_IN_PROGRESS = "In Progress"
        const val STATUS_COMPLETED = "Completed"
    }

    fun getSubtopics(): List<SubtopicItem> {
        val list = mutableListOf<SubtopicItem>()
        try {
            val jsonArray = JSONArray(subtopicsJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    SubtopicItem(
                        title = obj.optString("title", ""),
                        completed = obj.optBoolean("completed", false)
                    )
                )
            }
        } catch (_: Exception) {
            // fallback
        }
        return list
    }

    fun withSubtopics(items: List<SubtopicItem>): TopicEntity {
        val jsonArray = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("title", item.title)
            obj.put("completed", item.completed)
            jsonArray.put(obj)
        }
        return copy(subtopicsJson = jsonArray.toString())
    }
}

data class SubtopicItem(
    val title: String,
    val completed: Boolean
)

@Entity(tableName = "problems")
data class ProblemEntity(
    @PrimaryKey val id: String,
    val topicId: Int,
    val title: String,
    val difficulty: String = "Medium", // Easy, Medium, Hard
    val platform: String = "LeetCode", // LeetCode, GFG, Codeforces, Other
    val link: String = "",
    val solved: Boolean = false,
    val solvedDate: String? = null,
    val lastRevisedDate: String? = null,
    val revisionNotes: String = ""
)

@Entity(tableName = "daily_logs")
data class DailyLogEntity(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val topicId: Int,
    val problemsSolved: Int = 0,
    val minutes: Int = 0,
    val easyCount: Int = 0,
    val mediumCount: Int = 0,
    val hardCount: Int = 0,
    val notes: String = ""
)

@Entity(tableName = "streak_freezes")
data class StreakFreezeEntity(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val monthKey: String // YYYY-MM
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val dailyGoal: Int = 2,
    val minMinutes: Int = 30,
    val reminderTime: String = "20:00",
    val reminderEnabled: Boolean = true,
    val lastReminderDate: String = "",
    val isDarkMode: Boolean = true,
    val streakFreezeEnabled: Boolean = true,
    val freezesPerMonth: Int = 2,
    val longestStreak: Int = 0,
    val lastCelebratedMilestone: Int = 0
)

data class StreakInfo(
    val currentStreak: Int,
    val longestStreak: Int,
    val isTodayCompleted: Boolean,
    val freezesUsedThisMonth: Int,
    val freezesRemainingThisMonth: Int,
    val todayWarning: Boolean
)
