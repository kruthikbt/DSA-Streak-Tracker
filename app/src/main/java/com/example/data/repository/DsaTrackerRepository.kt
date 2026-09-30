package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.db.DefaultRoadmapData
import com.example.data.model.DailyLogEntity
import com.example.data.model.ProblemEntity
import com.example.data.model.StreakFreezeEntity
import com.example.data.model.SubtopicItem
import com.example.data.model.TopicEntity
import com.example.data.model.UserSettingsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID

class DsaTrackerRepository(private val database: AppDatabase) {

    val allTopics: Flow<List<TopicEntity>> = database.topicDao().getAllTopics()
    val allProblems: Flow<List<ProblemEntity>> = database.problemDao().getAllProblems()
    val allLogs: Flow<List<DailyLogEntity>> = database.dailyLogDao().getAllLogs()
    val allFreezes: Flow<List<StreakFreezeEntity>> = database.streakFreezeDao().getAllFreezes()
    val userSettings: Flow<UserSettingsEntity?> = database.userSettingsDao().getSettings()

    suspend fun ensureInitialized() = withContext(Dispatchers.IO) {
        val existingTopics = database.topicDao().getAllTopics().firstOrNull()
        if (existingTopics.isNullOrEmpty()) {
            AppDatabase.populateInitialDatabase(database)
        }
        val settings = database.userSettingsDao().getSettingsSync()
        if (settings == null) {
            database.userSettingsDao().insertOrUpdateSettings(UserSettingsEntity())
        }

        // Clean up any legacy hardcoded sample solved problems
        val allProblems = database.problemDao().getAllProblems().firstOrNull() ?: emptyList()
        val sampleSolved = allProblems.filter {
            it.solved && (
                it.revisionNotes.contains("Hash set for O(1)") ||
                it.revisionNotes.contains("Opposite pointers converging") ||
                it.revisionNotes.contains("Inward dual pointers") ||
                it.revisionNotes.contains("One-pass hash map") ||
                (it.title in listOf("Contains Duplicate", "Two Sum II - Input Array Is Sorted", "Valid Palindrome", "Two Sum") &&
                 it.solvedDate != null && it.solvedDate != LocalDate.now().toString())
            )
        }
        if (sampleSolved.isNotEmpty()) {
            sampleSolved.forEach { problem ->
                database.problemDao().updateProblem(
                    problem.copy(
                        solved = false,
                        solvedDate = null,
                        lastRevisedDate = null,
                        revisionNotes = ""
                    )
                )
            }
        }

        // Clean up any legacy demo logs so new users have zero activity on the heatmap
        val allLogs = database.dailyLogDao().getAllLogs().firstOrNull() ?: emptyList()
        val demoLogs = allLogs.filter { log ->
            log.notes.contains("Mastered Sliding Window") ||
            log.notes.contains("Container With Most Water") ||
            log.notes.contains("3Sum using sorted array") ||
            log.notes.contains("HashMap frequency counting") ||
            log.notes.contains("Subarray Sum Equals K") ||
            log.notes.contains("Valid Palindrome with alphanumeric regex clean") ||
            log.notes.contains("Longest Common Prefix horizontal") ||
            log.notes.contains("Kadane's algorithm for max subarray") ||
            log.notes.contains("Two Sum optimal O(n) map approach") ||
            log.notes.contains("Rotate array in-place reversing 3 sections") ||
            log.notes.contains("Big-O space complexity of recursive call stack") ||
            log.notes.contains("Time complexity comparison of binary search")
        }
        if (demoLogs.isNotEmpty()) {
            demoLogs.forEach { log ->
                database.dailyLogDao().deleteLog(log.date)
            }
            if (demoLogs.size == allLogs.size) {
                // If only demo logs were present, reset streak and milestones
                val currentSettings = database.userSettingsDao().getSettingsSync() ?: UserSettingsEntity()
                database.userSettingsDao().insertOrUpdateSettings(
                    currentSettings.copy(longestStreak = 0, lastCelebratedMilestone = 0)
                )
            }
        }
    }

    suspend fun updateTopic(topic: TopicEntity) = withContext(Dispatchers.IO) {
        database.topicDao().updateTopic(topic)
    }

    suspend fun addSubtopic(topicId: Int, title: String) = withContext(Dispatchers.IO) {
        val topics = database.topicDao().getAllTopics().firstOrNull() ?: return@withContext
        val topic = topics.find { it.id == topicId } ?: return@withContext
        val currentSubtopics = topic.getSubtopics().toMutableList()
        currentSubtopics.add(SubtopicItem(title = title.trim(), completed = false))
        database.topicDao().updateTopic(topic.withSubtopics(currentSubtopics))
    }

    suspend fun toggleSubtopic(topicId: Int, index: Int) = withContext(Dispatchers.IO) {
        val topics = database.topicDao().getAllTopics().firstOrNull() ?: return@withContext
        val topic = topics.find { it.id == topicId } ?: return@withContext
        val currentSubtopics = topic.getSubtopics().toMutableList()
        if (index in currentSubtopics.indices) {
            val item = currentSubtopics[index]
            currentSubtopics[index] = item.copy(completed = !item.completed)
            database.topicDao().updateTopic(topic.withSubtopics(currentSubtopics))
        }
    }

    suspend fun deleteSubtopic(topicId: Int, index: Int) = withContext(Dispatchers.IO) {
        val topics = database.topicDao().getAllTopics().firstOrNull() ?: return@withContext
        val topic = topics.find { it.id == topicId } ?: return@withContext
        val currentSubtopics = topic.getSubtopics().toMutableList()
        if (index in currentSubtopics.indices) {
            currentSubtopics.removeAt(index)
            database.topicDao().updateTopic(topic.withSubtopics(currentSubtopics))
        }
    }

    suspend fun insertProblem(problem: ProblemEntity) = withContext(Dispatchers.IO) {
        database.problemDao().insertProblem(problem)
    }

    suspend fun insertProblems(problems: List<ProblemEntity>) = withContext(Dispatchers.IO) {
        database.problemDao().insertProblems(problems)
    }

    suspend fun updateProblem(problem: ProblemEntity) = withContext(Dispatchers.IO) {
        database.problemDao().updateProblem(problem)
    }

    suspend fun toggleProblemSolved(problem: ProblemEntity) = withContext(Dispatchers.IO) {
        val newSolved = !problem.solved
        val todayStr = LocalDate.now().toString()
        val updated = problem.copy(
            solved = newSolved,
            solvedDate = if (newSolved) todayStr else null
        )
        database.problemDao().updateProblem(updated)
    }

    suspend fun markProblemRevised(problemId: String, notes: String? = null) = withContext(Dispatchers.IO) {
        val problems = database.problemDao().getAllProblems().firstOrNull() ?: return@withContext
        val problem = problems.find { it.id == problemId } ?: return@withContext
        val todayStr = LocalDate.now().toString()
        val updatedNotes = if (!notes.isNullOrBlank()) {
            if (problem.revisionNotes.isNotBlank()) "${problem.revisionNotes}\n[$todayStr] $notes" else "[$todayStr] $notes"
        } else {
            problem.revisionNotes
        }
        database.problemDao().updateProblem(
            problem.copy(
                lastRevisedDate = todayStr,
                revisionNotes = updatedNotes
            )
        )
    }

    suspend fun updateProblemSolvedDate(problemId: String, date: String?) = withContext(Dispatchers.IO) {
        val problems = database.problemDao().getAllProblems().firstOrNull() ?: return@withContext
        val problem = problems.find { it.id == problemId } ?: return@withContext
        database.problemDao().updateProblem(
            problem.copy(
                solved = !date.isNullOrBlank(),
                solvedDate = date
            )
        )
    }

    suspend fun updateProblemRevisionNotes(problemId: String, notes: String) = withContext(Dispatchers.IO) {
        val problems = database.problemDao().getAllProblems().firstOrNull() ?: return@withContext
        val problem = problems.find { it.id == problemId } ?: return@withContext
        database.problemDao().updateProblem(problem.copy(revisionNotes = notes))
    }

    suspend fun seedSampleRevisions() = withContext(Dispatchers.IO) {
        val problems = database.problemDao().getAllProblems().firstOrNull() ?: return@withContext
        val today = LocalDate.now()
        val date3 = today.minusDays(3).toString()
        val date7 = today.minusDays(7).toString()
        val date30 = today.minusDays(30).toString()

        problems.forEach { problem ->
            when (problem.title) {
                "Contains Duplicate" -> database.problemDao().updateProblem(
                    problem.copy(
                        solved = true,
                        solvedDate = date3,
                        revisionNotes = "Hash set for O(1) membership lookup. Invariant: seen elements are unique."
                    )
                )
                "Two Sum II - Input Array Is Sorted" -> database.problemDao().updateProblem(
                    problem.copy(
                        solved = true,
                        solvedDate = date3,
                        revisionNotes = "Opposite pointers converging. Sorted monotonic property."
                    )
                )
                "Valid Palindrome" -> database.problemDao().updateProblem(
                    problem.copy(
                        solved = true,
                        solvedDate = date7,
                        revisionNotes = "Inward dual pointers skipping non-alphanumerics. Symmetrical equality check."
                    )
                )
                "Two Sum" -> database.problemDao().updateProblem(
                    problem.copy(
                        solved = true,
                        solvedDate = date30,
                        revisionNotes = "One-pass hash map caching target - num complement. O(n) time and O(n) space."
                    )
                )
            }
        }
    }

    suspend fun deleteProblem(id: String) = withContext(Dispatchers.IO) {
        database.problemDao().deleteProblemById(id)
    }

    suspend fun logPractice(
        date: String,
        topicId: Int,
        problemsSolved: Int,
        minutes: Int,
        easy: Int,
        med: Int,
        hard: Int,
        notes: String
    ) = withContext(Dispatchers.IO) {
        val existing = database.dailyLogDao().getLogForDate(date)
        val finalLog = if (existing != null) {
            // Merge duplicate day log as specified in prompt
            DailyLogEntity(
                date = date,
                topicId = topicId,
                problemsSolved = existing.problemsSolved + problemsSolved,
                minutes = existing.minutes + minutes,
                easyCount = existing.easyCount + easy,
                mediumCount = existing.mediumCount + med,
                hardCount = existing.hardCount + hard,
                notes = if (existing.notes.isNotBlank() && notes.isNotBlank()) {
                    "${existing.notes}\n---\n$notes"
                } else if (notes.isNotBlank()) {
                    notes
                } else {
                    existing.notes
                }
            )
        } else {
            DailyLogEntity(
                date = date,
                topicId = topicId,
                problemsSolved = problemsSolved,
                minutes = minutes,
                easyCount = easy,
                mediumCount = med,
                hardCount = hard,
                notes = notes
            )
        }
        database.dailyLogDao().insertOrUpdateLog(finalLog)
    }

    suspend fun overwriteLog(log: DailyLogEntity) = withContext(Dispatchers.IO) {
        database.dailyLogDao().insertOrUpdateLog(log)
    }

    suspend fun deleteLog(date: String) = withContext(Dispatchers.IO) {
        database.dailyLogDao().deleteLog(date)
    }

    suspend fun useStreakFreeze(date: String): Boolean = withContext(Dispatchers.IO) {
        val monthKey = date.substring(0, 7)
        val settings = database.userSettingsDao().getSettingsSync() ?: UserSettingsEntity()
        if (!settings.streakFreezeEnabled) return@withContext false

        val used = database.streakFreezeDao().getFreezesForMonth(monthKey)
        if (used.size < settings.freezesPerMonth) {
            database.streakFreezeDao().insertFreeze(StreakFreezeEntity(date = date, monthKey = monthKey))
            true
        } else {
            false
        }
    }

    suspend fun removeStreakFreeze(date: String) = withContext(Dispatchers.IO) {
        database.streakFreezeDao().deleteFreeze(date)
    }

    suspend fun updateSettings(settings: UserSettingsEntity) = withContext(Dispatchers.IO) {
        database.userSettingsDao().insertOrUpdateSettings(settings)
    }

    suspend fun loadDemoData() = withContext(Dispatchers.IO) {
        val today = LocalDate.now()
        // Create 14 days of realistic logs to demonstrate streaks & heatmap
        val demoLogs = listOf(
            DailyLogEntity(today.toString(), 6, 3, 65, 1, 2, 0, "Mastered Sliding Window with Deque for max in window!"),
            DailyLogEntity(today.minusDays(1).toString(), 5, 2, 45, 1, 1, 0, "Container With Most Water two-pointer proof."),
            DailyLogEntity(today.minusDays(2).toString(), 5, 3, 50, 2, 1, 0, "3Sum using sorted array and dual pointers."),
            DailyLogEntity(today.minusDays(3).toString(), 4, 4, 70, 2, 2, 0, "HashMap frequency counting and Group Anagrams."),
            DailyLogEntity(today.minusDays(4).toString(), 4, 2, 40, 1, 1, 0, "Subarray Sum Equals K with prefix hash table."),
            DailyLogEntity(today.minusDays(5).toString(), 3, 3, 55, 2, 1, 0, "Valid Palindrome with alphanumeric regex clean."),
            DailyLogEntity(today.minusDays(6).toString(), 3, 2, 35, 2, 0, 0, "Longest Common Prefix horizontal and vertical scan."),
            DailyLogEntity(today.minusDays(7).toString(), 2, 4, 80, 2, 2, 0, "Kadane's algorithm for max subarray and Dutch flag."),
            DailyLogEntity(today.minusDays(8).toString(), 2, 3, 60, 2, 1, 0, "Two Sum optimal O(n) map approach."),
            DailyLogEntity(today.minusDays(9).toString(), 2, 2, 40, 1, 1, 0, "Rotate array in-place reversing 3 sections."),
            DailyLogEntity(today.minusDays(10).toString(), 1, 1, 35, 1, 0, 0, "Big-O space complexity of recursive call stack."),
            DailyLogEntity(today.minusDays(11).toString(), 1, 2, 50, 1, 1, 0, "Time complexity comparison of binary search vs linear.")
        )
        for (log in demoLogs) {
            database.dailyLogDao().insertOrUpdateLog(log)
        }

        // Update topics progress for demo
        val currentTopics = database.topicDao().getAllTopics().firstOrNull() ?: DefaultRoadmapData.createInitialTopics()
        val updatedTopics = currentTopics.map { t ->
            when (t.order) {
                1 -> t.copy(status = TopicEntity.STATUS_COMPLETED).withSubtopics(t.getSubtopics().map { it.copy(completed = true) })
                2 -> t.copy(status = TopicEntity.STATUS_COMPLETED).withSubtopics(t.getSubtopics().map { it.copy(completed = true) })
                3 -> t.copy(status = TopicEntity.STATUS_COMPLETED).withSubtopics(t.getSubtopics().map { it.copy(completed = true) })
                4 -> t.copy(status = TopicEntity.STATUS_COMPLETED).withSubtopics(t.getSubtopics().map { it.copy(completed = true) })
                5 -> t.copy(status = TopicEntity.STATUS_COMPLETED).withSubtopics(t.getSubtopics().map { it.copy(completed = true) })
                6 -> t.copy(status = TopicEntity.STATUS_IN_PROGRESS).withSubtopics(
                    t.getSubtopics().mapIndexed { i, s -> s.copy(completed = i < 3) }
                )
                else -> t.copy(status = TopicEntity.STATUS_NOT_STARTED)
            }
        }
        database.topicDao().insertTopics(updatedTopics)

        // Mark corresponding problems as solved
        val problems = database.problemDao().getAllProblems().firstOrNull() ?: emptyList()
        val updatedProblems = problems.map { p ->
            if (p.topicId in 1..5) {
                p.copy(solved = true, solvedDate = today.minusDays(p.topicId.toLong()).toString())
            } else if (p.topicId == 6 && p.difficulty == "Medium") {
                p.copy(solved = true, solvedDate = today.toString())
            } else {
                p
            }
        }
        database.problemDao().insertProblems(updatedProblems)

        // Update settings longest streak
        val currentSettings = database.userSettingsDao().getSettingsSync() ?: UserSettingsEntity()
        database.userSettingsDao().insertOrUpdateSettings(currentSettings.copy(longestStreak = 12))
    }

    suspend fun exportJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()

        val topics = database.topicDao().getAllTopics().firstOrNull() ?: emptyList()
        val topicsArray = JSONArray()
        for (t in topics) {
            val tObj = JSONObject()
            tObj.put("id", t.id)
            tObj.put("order", t.order)
            tObj.put("name", t.name)
            tObj.put("status", t.status)
            tObj.put("subtopicsJson", t.subtopicsJson)
            topicsArray.put(tObj)
        }
        root.put("topics", topicsArray)

        val problems = database.problemDao().getAllProblems().firstOrNull() ?: emptyList()
        val problemsArray = JSONArray()
        for (p in problems) {
            val pObj = JSONObject()
            pObj.put("id", p.id)
            pObj.put("topicId", p.topicId)
            pObj.put("title", p.title)
            pObj.put("difficulty", p.difficulty)
            pObj.put("platform", p.platform)
            pObj.put("link", p.link)
            pObj.put("solved", p.solved)
            pObj.put("solvedDate", p.solvedDate ?: "")
            problemsArray.put(pObj)
        }
        root.put("problems", problemsArray)

        val logs = database.dailyLogDao().getAllLogs().firstOrNull() ?: emptyList()
        val logsArray = JSONArray()
        for (l in logs) {
            val lObj = JSONObject()
            lObj.put("date", l.date)
            lObj.put("topicId", l.topicId)
            lObj.put("problemsSolved", l.problemsSolved)
            lObj.put("minutes", l.minutes)
            lObj.put("easyCount", l.easyCount)
            lObj.put("mediumCount", l.mediumCount)
            lObj.put("hardCount", l.hardCount)
            lObj.put("notes", l.notes)
            logsArray.put(lObj)
        }
        root.put("logs", logsArray)

        val freezes = database.streakFreezeDao().getAllFreezes().firstOrNull() ?: emptyList()
        val freezesArray = JSONArray()
        for (f in freezes) {
            val fObj = JSONObject()
            fObj.put("date", f.date)
            fObj.put("monthKey", f.monthKey)
            freezesArray.put(fObj)
        }
        root.put("streakFreezes", freezesArray)

        val settings = database.userSettingsDao().getSettingsSync() ?: UserSettingsEntity()
        val sObj = JSONObject()
        sObj.put("dailyGoal", settings.dailyGoal)
        sObj.put("minMinutes", settings.minMinutes)
        sObj.put("reminderTime", settings.reminderTime)
        sObj.put("isDarkMode", settings.isDarkMode)
        sObj.put("streakFreezeEnabled", settings.streakFreezeEnabled)
        sObj.put("freezesPerMonth", settings.freezesPerMonth)
        sObj.put("longestStreak", settings.longestStreak)
        sObj.put("lastCelebratedMilestone", settings.lastCelebratedMilestone)
        root.put("settings", sObj)

        root.toString(2)
    }

    suspend fun importJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)

            if (root.has("topics")) {
                val topicsArray = root.getJSONArray("topics")
                val topics = mutableListOf<TopicEntity>()
                for (i in 0 until topicsArray.length()) {
                    val obj = topicsArray.getJSONObject(i)
                    topics.add(
                        TopicEntity(
                            id = obj.getInt("id"),
                            order = obj.getInt("order"),
                            name = obj.getString("name"),
                            status = obj.optString("status", TopicEntity.STATUS_NOT_STARTED),
                            subtopicsJson = obj.optString("subtopicsJson", "[]")
                        )
                    )
                }
                if (topics.isNotEmpty()) {
                    database.topicDao().deleteAllTopics()
                    database.topicDao().insertTopics(topics)
                }
            }

            if (root.has("problems")) {
                val problemsArray = root.getJSONArray("problems")
                val problems = mutableListOf<ProblemEntity>()
                for (i in 0 until problemsArray.length()) {
                    val obj = problemsArray.getJSONObject(i)
                    problems.add(
                        ProblemEntity(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            topicId = obj.getInt("topicId"),
                            title = obj.getString("title"),
                            difficulty = obj.optString("difficulty", "Medium"),
                            platform = obj.optString("platform", "LeetCode"),
                            link = obj.optString("link", ""),
                            solved = obj.optBoolean("solved", false),
                            solvedDate = obj.optString("solvedDate").ifBlank { null }
                        )
                    )
                }
                database.problemDao().deleteAllProblems()
                database.problemDao().insertProblems(problems)
            }

            if (root.has("logs")) {
                val logsArray = root.getJSONArray("logs")
                val logs = mutableListOf<DailyLogEntity>()
                for (i in 0 until logsArray.length()) {
                    val obj = logsArray.getJSONObject(i)
                    logs.add(
                        DailyLogEntity(
                            date = obj.getString("date"),
                            topicId = obj.getInt("topicId"),
                            problemsSolved = obj.optInt("problemsSolved", 0),
                            minutes = obj.optInt("minutes", 0),
                            easyCount = obj.optInt("easyCount", 0),
                            mediumCount = obj.optInt("mediumCount", 0),
                            hardCount = obj.optInt("hardCount", 0),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
                database.dailyLogDao().deleteAllLogs()
                for (log in logs) {
                    database.dailyLogDao().insertOrUpdateLog(log)
                }
            }

            if (root.has("streakFreezes")) {
                val freezesArray = root.getJSONArray("streakFreezes")
                val freezes = mutableListOf<StreakFreezeEntity>()
                for (i in 0 until freezesArray.length()) {
                    val obj = freezesArray.getJSONObject(i)
                    freezes.add(
                        StreakFreezeEntity(
                            date = obj.getString("date"),
                            monthKey = obj.optString("monthKey", obj.getString("date").substring(0, 7))
                        )
                    )
                }
                database.streakFreezeDao().deleteAllFreezes()
                for (f in freezes) {
                    database.streakFreezeDao().insertFreeze(f)
                }
            }

            if (root.has("settings")) {
                val sObj = root.getJSONObject("settings")
                val settings = UserSettingsEntity(
                    id = 1,
                    dailyGoal = sObj.optInt("dailyGoal", 2),
                    minMinutes = sObj.optInt("minMinutes", 30),
                    reminderTime = sObj.optString("reminderTime", "20:00"),
                    isDarkMode = sObj.optBoolean("isDarkMode", true),
                    streakFreezeEnabled = sObj.optBoolean("streakFreezeEnabled", true),
                    freezesPerMonth = sObj.optInt("freezesPerMonth", 2),
                    longestStreak = sObj.optInt("longestStreak", 0),
                    lastCelebratedMilestone = sObj.optInt("lastCelebratedMilestone", 0)
                )
                database.userSettingsDao().insertOrUpdateSettings(settings)
            }

            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun resetAllData() = withContext(Dispatchers.IO) {
        database.dailyLogDao().deleteAllLogs()
        database.streakFreezeDao().deleteAllFreezes()
        database.topicDao().deleteAllTopics()
        database.problemDao().deleteAllProblems()
        AppDatabase.populateInitialDatabase(database)
        database.userSettingsDao().insertOrUpdateSettings(UserSettingsEntity())
    }
}
