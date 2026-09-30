package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.DailyPlanResult
import com.example.data.ai.GeminiPlanService
import com.example.data.ai.SuggestedProblem
import com.example.data.db.AppDatabase
import com.example.data.model.DailyLogEntity
import com.example.data.model.ProblemEntity
import com.example.data.model.RevisionProblemItem
import com.example.data.model.StreakFreezeEntity
import com.example.data.model.StreakInfo
import com.example.data.model.TopicEntity
import com.example.data.model.UserSettingsEntity
import com.example.data.repository.DsaTrackerRepository
import com.example.util.StreakUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class DsaTrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = DsaTrackerRepository(database)

    val topics: StateFlow<List<TopicEntity>> = repository.allTopics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val problems: StateFlow<List<ProblemEntity>> = repository.allProblems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyLogs: StateFlow<List<DailyLogEntity>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val freezes: StateFlow<List<StreakFreezeEntity>> = repository.allFreezes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _rawSettings = repository.userSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val settings: StateFlow<UserSettingsEntity> = combine(_rawSettings) { s ->
        s[0] ?: UserSettingsEntity()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettingsEntity())

    val streakInfo: StateFlow<StreakInfo> = combine(
        dailyLogs,
        freezes,
        settings
    ) { logs, freezesList, currentSettings ->
        val info = StreakUtils.calculateStreak(logs, freezesList, currentSettings)

        // Check if new longest streak needs persisting
        if (info.longestStreak > currentSettings.longestStreak) {
            viewModelScope.launch {
                repository.updateSettings(currentSettings.copy(longestStreak = info.longestStreak))
            }
        }

        // Check milestone celebration
        val milestone = StreakUtils.checkNewMilestone(info.currentStreak, currentSettings.lastCelebratedMilestone)
        if (milestone != null && _milestoneToCelebrate.value == null) {
            _milestoneToCelebrate.value = milestone
        }

        info
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        StreakInfo(0, 0, false, 0, 2, false)
    )

    private val _selectedTab = MutableStateFlow(0) // 0: Dashboard, 1: Roadmap, 2: Revision, 3: Daily Log, 4: Stats & Settings
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _milestoneToCelebrate = MutableStateFlow<Int?>(null)
    val milestoneToCelebrate: StateFlow<Int?> = _milestoneToCelebrate.asStateFlow()

    private val _activeTopicId = MutableStateFlow<Int?>(null)
    val activeTopicId: StateFlow<Int?> = _activeTopicId.asStateFlow()

    // For editing a past log
    private val _editingLog = MutableStateFlow<DailyLogEntity?>(null)
    val editingLog: StateFlow<DailyLogEntity?> = _editingLog.asStateFlow()

    // Spaced Revision State
    private val _selectedRevisionFilter = MutableStateFlow("ALL_DUE") // ALL_DUE, 3_DAYS, 7_DAYS, 30_DAYS, ALL_SOLVED
    val selectedRevisionFilter: StateFlow<String> = _selectedRevisionFilter.asStateFlow()

    val revisionItems: StateFlow<List<RevisionProblemItem>> = combine(problems, topics) { problemList, topicList ->
        val topicMap = topicList.associateBy { it.id }
        val today = LocalDate.now()
        problemList.filter { it.solved && !it.solvedDate.isNullOrBlank() }.mapNotNull { problem ->
            val solvedDate = try {
                LocalDate.parse(problem.solvedDate)
            } catch (_: Exception) {
                null
            } ?: return@mapNotNull null

            val daysAgo = java.time.temporal.ChronoUnit.DAYS.between(solvedDate, today)
            if (daysAgo < 0) return@mapNotNull null

            val is3Exact = daysAgo == 3L
            val is7Exact = daysAgo == 7L
            val is30Exact = daysAgo == 30L

            val isRevisedToday = problem.lastRevisedDate == today.toString()
            val isDue = (is3Exact || is7Exact || is30Exact || daysAgo in 3L..6L || daysAgo in 7L..29L || daysAgo >= 30L) && !isRevisedToday

            RevisionProblemItem(
                problem = problem,
                topic = topicMap[problem.topicId],
                solvedLocalDate = solvedDate,
                daysAgo = daysAgo,
                is3DaysAgo = is3Exact,
                is7DaysAgo = is7Exact,
                is30DaysAgo = is30Exact,
                isDue = isDue,
                isRevisedToday = isRevisedToday
            )
        }.sortedBy { it.daysAgo }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Gemini AI Daily Plan
    private val geminiPlanService = GeminiPlanService()

    private val _isPlanLoading = MutableStateFlow(false)
    val isPlanLoading: StateFlow<Boolean> = _isPlanLoading.asStateFlow()

    private val _suggestedProblems = MutableStateFlow<List<SuggestedProblem>>(emptyList())
    val suggestedProblems: StateFlow<List<SuggestedProblem>> = _suggestedProblems.asStateFlow()

    private val _planIsFromAI = MutableStateFlow(false)
    val planIsFromAI: StateFlow<Boolean> = _planIsFromAI.asStateFlow()

    private val _planMessage = MutableStateFlow<String?>(null)
    val planMessage: StateFlow<String?> = _planMessage.asStateFlow()

    private val _dailyPlanDialogTopic = MutableStateFlow<TopicEntity?>(null)
    val dailyPlanDialogTopic: StateFlow<TopicEntity?> = _dailyPlanDialogTopic.asStateFlow()

    private val _planDifficulty = MutableStateFlow("Medium")
    val planDifficulty: StateFlow<String> = _planDifficulty.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
        }
    }

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun navigateToRoadmapTopic(topicId: Int) {
        _activeTopicId.value = topicId
        _selectedTab.value = 1
    }

    fun dismissMilestone() {
        val milestone = _milestoneToCelebrate.value
        _milestoneToCelebrate.value = null
        if (milestone != null) {
            viewModelScope.launch {
                val current = settings.value
                if (milestone > current.lastCelebratedMilestone) {
                    repository.updateSettings(current.copy(lastCelebratedMilestone = milestone))
                }
            }
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(current.copy(isDarkMode = !current.isDarkMode))
        }
    }

    fun updateSettings(dailyGoal: Int, minMinutes: Int, streakFreezeEnabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(
                current.copy(
                    dailyGoal = dailyGoal,
                    minMinutes = minMinutes,
                    streakFreezeEnabled = streakFreezeEnabled
                )
            )
        }
    }

    fun updateTopicStatus(topic: TopicEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateTopic(topic.copy(status = newStatus))
        }
    }

    fun addSubtopic(topicId: Int, title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addSubtopic(topicId, title)
        }
    }

    fun toggleSubtopic(topicId: Int, index: Int) {
        viewModelScope.launch {
            repository.toggleSubtopic(topicId, index)
        }
    }

    fun deleteSubtopic(topicId: Int, index: Int) {
        viewModelScope.launch {
            repository.deleteSubtopic(topicId, index)
        }
    }

    fun addProblem(problem: ProblemEntity) {
        viewModelScope.launch {
            repository.insertProblem(problem)
        }
    }

    fun updateProblem(problem: ProblemEntity) {
        viewModelScope.launch {
            repository.updateProblem(problem)
        }
    }

    fun toggleProblemSolved(problem: ProblemEntity) {
        viewModelScope.launch {
            repository.toggleProblemSolved(problem)
        }
    }

    fun deleteProblem(problemId: String) {
        viewModelScope.launch {
            repository.deleteProblem(problemId)
        }
    }

    fun logPractice(
        date: String,
        topicId: Int,
        problemsSolved: Int,
        minutes: Int,
        easy: Int,
        med: Int,
        hard: Int,
        notes: String
    ) {
        viewModelScope.launch {
            repository.logPractice(
                date = date,
                topicId = topicId,
                problemsSolved = problemsSolved,
                minutes = minutes,
                easy = easy,
                med = med,
                hard = hard,
                notes = notes
            )
            _editingLog.value = null
        }
    }

    fun setEditingLog(log: DailyLogEntity?) {
        _editingLog.value = log
        if (log != null) {
            _selectedTab.value = 2 // Switch to Daily Log page
        }
    }

    fun overwriteLog(log: DailyLogEntity) {
        viewModelScope.launch {
            repository.overwriteLog(log)
            _editingLog.value = null
        }
    }

    fun deleteLog(date: String) {
        viewModelScope.launch {
            repository.deleteLog(date)
            if (_editingLog.value?.date == date) {
                _editingLog.value = null
            }
        }
    }

    fun useStreakFreezeToday() {
        val todayStr = LocalDate.now().toString()
        viewModelScope.launch {
            repository.useStreakFreeze(todayStr)
        }
    }

    fun useStreakFreezeYesterday() {
        val yesterdayStr = LocalDate.now().minusDays(1).toString()
        viewModelScope.launch {
            repository.useStreakFreeze(yesterdayStr)
        }
    }

    fun loadDemoData() {
        viewModelScope.launch {
            repository.loadDemoData()
        }
    }

    suspend fun exportJson(): String {
        return repository.exportJson()
    }

    suspend fun importJson(json: String): Boolean {
        return repository.importJson(json)
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
        }
    }

    fun requestDailyPlan(topic: TopicEntity, difficulty: String? = null) {
        val targetDiff = difficulty ?: _planDifficulty.value
        _planDifficulty.value = targetDiff
        _dailyPlanDialogTopic.value = topic
        _isPlanLoading.value = true
        _suggestedProblems.value = emptyList()
        _planMessage.value = null

        viewModelScope.launch {
            val allProblemsList = problems.value
            val topicProblems = allProblemsList.filter { it.topicId == topic.id }
            val solved = topicProblems.filter { it.solved }
            val unsolved = topicProblems.filter { !it.solved }

            val result = geminiPlanService.generateDailyPlan(
                topic = topic,
                solvedProblems = solved,
                unsolvedProblems = unsolved,
                userDifficultyPreference = targetDiff
            )

            when (result) {
                is DailyPlanResult.Success -> {
                    _suggestedProblems.value = result.problems
                    _planIsFromAI.value = result.isFromAI
                    _planMessage.value = result.message
                }
                is DailyPlanResult.Error -> {
                    _suggestedProblems.value = result.fallbackProblems
                    _planIsFromAI.value = false
                    _planMessage.value = result.message
                }
            }
            _isPlanLoading.value = false
        }
    }

    fun dismissDailyPlanDialog() {
        _dailyPlanDialogTopic.value = null
    }

    fun selectRevisionFilter(filter: String) {
        _selectedRevisionFilter.value = filter
    }

    fun markProblemRevised(problemId: String, notes: String? = null) {
        viewModelScope.launch {
            repository.markProblemRevised(problemId, notes)
        }
    }

    fun updateProblemSolvedDate(problemId: String, date: String?) {
        viewModelScope.launch {
            repository.updateProblemSolvedDate(problemId, date)
        }
    }

    fun setProblemSolvedDaysAgo(problemId: String, daysAgo: Int) {
        val targetDate = LocalDate.now().minusDays(daysAgo.toLong()).toString()
        updateProblemSolvedDate(problemId, targetDate)
    }

    fun updateProblemRevisionNotes(problemId: String, notes: String) {
        viewModelScope.launch {
            repository.updateProblemRevisionNotes(problemId, notes)
        }
    }

    fun seedSampleRevisions() {
        viewModelScope.launch {
            repository.seedSampleRevisions()
        }
    }

    fun bulkAddProblems(problemsToAdd: List<ProblemEntity>) {
        viewModelScope.launch {
            repository.insertProblems(problemsToAdd)
        }
    }
}
