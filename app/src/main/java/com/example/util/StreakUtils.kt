package com.example.util

import com.example.data.model.DailyLogEntity
import com.example.data.model.StreakFreezeEntity
import com.example.data.model.StreakInfo
import com.example.data.model.UserSettingsEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object StreakUtils {

    fun calculateStreak(
        logs: List<DailyLogEntity>,
        freezes: List<StreakFreezeEntity>,
        settings: UserSettingsEntity,
        referenceDate: LocalDate = LocalDate.now()
    ): StreakInfo {
        val minMinutes = settings.minMinutes
        val validDateStrings = mutableSetOf<String>()

        // Merge daily totals by date to ensure duplicate logs on the same date combine properly
        val logsByDate = logs.groupBy { it.date }
        for ((date, dayLogs) in logsByDate) {
            val totalProblems = dayLogs.sumOf { it.problemsSolved }
            val totalMinutes = dayLogs.sumOf { it.minutes }
            if (totalProblems >= 1 || totalMinutes >= minMinutes) {
                validDateStrings.add(date)
            }
        }

        // Only enforce freezes if enabled in settings, and limit to allowed freezes per month
        val validFreezes = if (settings.streakFreezeEnabled) {
            freezes.groupBy { it.monthKey }
                .flatMap { (_, monthFreezes) ->
                    monthFreezes.sortedBy { it.date }.take(settings.freezesPerMonth)
                }
        } else {
            emptyList()
        }

        val freezeDateStrings = validFreezes.map { it.date }.toSet()
        val allValidDates = validDateStrings + freezeDateStrings

        val todayStr = referenceDate.toString()
        val yesterday = referenceDate.minusDays(1)
        val yesterdayStr = yesterday.toString()

        val isTodayCompleted = validDateStrings.contains(todayStr) || freezeDateStrings.contains(todayStr)

        var streak = 0
        var hasActualLog = false
        val startDate = if (isTodayCompleted) referenceDate else yesterday

        if (isTodayCompleted || allValidDates.contains(yesterdayStr)) {
            var checkDate = startDate
            while (allValidDates.contains(checkDate.toString())) {
                if (validDateStrings.contains(checkDate.toString())) {
                    hasActualLog = true
                }
                streak++
                checkDate = checkDate.minusDays(1)
            }
        }

        // A streak cannot be formed solely from freeze days without any actual practice
        if (!hasActualLog) {
            streak = 0
        }

        // Calculate all-time longest streak
        var longest = settings.longestStreak
        if (streak > longest) {
            longest = streak
        }

        // Historical streak calculation from chronological dates up to referenceDate
        val sortedDates = allValidDates.mapNotNull {
            try {
                val parsed = LocalDate.parse(it)
                if (!parsed.isAfter(referenceDate)) parsed else null
            } catch (_: Exception) {
                null
            }
        }.distinct().sorted()

        var currentRun = 0
        var runHasLog = false
        var prevDate: LocalDate? = null
        for (date in sortedDates) {
            val isLog = validDateStrings.contains(date.toString())
            if (prevDate == null) {
                currentRun = 1
                runHasLog = isLog
            } else if (date == prevDate.plusDays(1)) {
                currentRun++
                if (isLog) runHasLog = true
            } else {
                currentRun = 1
                runHasLog = isLog
            }
            if (runHasLog && currentRun > longest) {
                longest = currentRun
            }
            prevDate = date
        }

        val currentMonthKey = referenceDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val freezesUsedThisMonth = if (settings.streakFreezeEnabled) {
            freezes.count { it.monthKey == currentMonthKey }
        } else 0
        val freezesRemaining = if (settings.streakFreezeEnabled) {
            maxOf(0, settings.freezesPerMonth - freezesUsedThisMonth)
        } else 0

        val todayWarning = !isTodayCompleted && streak > 0

        return StreakInfo(
            currentStreak = streak,
            longestStreak = longest,
            isTodayCompleted = isTodayCompleted,
            freezesUsedThisMonth = freezesUsedThisMonth,
            freezesRemainingThisMonth = freezesRemaining,
            todayWarning = todayWarning
        )
    }

    val MILESTONES = listOf(7, 30, 50, 100, 365)

    fun checkNewMilestone(currentStreak: Int, lastCelebrated: Int): Int? {
        val earnedMilestones = MILESTONES.filter { currentStreak >= it }
        val nextMilestone = earnedMilestones.lastOrNull()
        return if (nextMilestone != null && nextMilestone > lastCelebrated) {
            nextMilestone
        } else {
            null
        }
    }
}
