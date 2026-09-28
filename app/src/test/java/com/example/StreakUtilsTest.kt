package com.example

import com.example.data.model.DailyLogEntity
import com.example.data.model.StreakFreezeEntity
import com.example.data.model.UserSettingsEntity
import com.example.util.StreakUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StreakUtilsTest {

    // ==========================================
    // CASE 1: Logged Today
    // ==========================================
    @Test
    fun testCase1_LoggedToday_SingleDay() {
        val today = LocalDate.of(2026, 9, 28)
        val logs = listOf(
            DailyLogEntity(date = "2026-09-28", topicId = 1, problemsSolved = 2, minutes = 25)
        )
        val settings = UserSettingsEntity(minMinutes = 30)
        val streakInfo = StreakUtils.calculateStreak(logs, emptyList(), settings, today)

        assertEquals(1, streakInfo.currentStreak)
        assertTrue(streakInfo.isTodayCompleted)
        assertFalse(streakInfo.todayWarning)
        assertEquals(1, streakInfo.longestStreak)
    }

    @Test
    fun testCase1_LoggedToday_MultiDayStreak() {
        val today = LocalDate.of(2026, 9, 28)
        val logs = listOf(
            DailyLogEntity(date = "2026-09-28", topicId = 1, problemsSolved = 2, minutes = 45),
            DailyLogEntity(date = "2026-09-27", topicId = 1, problemsSolved = 1, minutes = 30),
            DailyLogEntity(date = "2026-09-26", topicId = 2, problemsSolved = 3, minutes = 60)
        )
        val settings = UserSettingsEntity(minMinutes = 30)
        val streakInfo = StreakUtils.calculateStreak(logs, emptyList(), settings, today)

        assertEquals(3, streakInfo.currentStreak)
        assertTrue(streakInfo.isTodayCompleted)
        assertFalse(streakInfo.todayWarning)
        assertEquals(3, streakInfo.longestStreak)
    }

    @Test
    fun testCase1_LoggedToday_QualifiesByMinutesEvenWithZeroProblems() {
        val today = LocalDate.of(2026, 9, 28)
        // 0 problems, but 40 minutes spent >= 30 min minimum
        val logs = listOf(
            DailyLogEntity(date = "2026-09-28", topicId = 1, problemsSolved = 0, minutes = 40)
        )
        val settings = UserSettingsEntity(minMinutes = 30)
        val streakInfo = StreakUtils.calculateStreak(logs, emptyList(), settings, today)

        assertEquals(1, streakInfo.currentStreak)
        assertTrue(streakInfo.isTodayCompleted)
    }

    // ==========================================
    // CASE 2: Logged Yesterday But Not Today
    // ==========================================
    @Test
    fun testCase2_LoggedYesterdayButNotToday_StreakRemainsAliveWithWarning() {
        val today = LocalDate.of(2026, 9, 28)
        val logs = listOf(
            DailyLogEntity(date = "2026-09-27", topicId = 1, problemsSolved = 2, minutes = 40),
            DailyLogEntity(date = "2026-09-26", topicId = 1, problemsSolved = 1, minutes = 35)
        )
        val settings = UserSettingsEntity(minMinutes = 30)
        val streakInfo = StreakUtils.calculateStreak(logs, emptyList(), settings, today)

        // Streak is 2 because yesterday and the day before were completed
        assertEquals(2, streakInfo.currentStreak)
        // Today is not yet completed
        assertFalse(streakInfo.isTodayCompleted)
        // Warning MUST be shown to alert user to log today before midnight
        assertTrue(streakInfo.todayWarning)
        assertEquals(2, streakInfo.longestStreak)
    }

    // ==========================================
    // CASE 3: Missed 2 Days
    // ==========================================
    @Test
    fun testCase3_Missed2Days_StreakResetsToZero() {
        val today = LocalDate.of(2026, 9, 28)
        // User logged on 2026-09-25, but missed both 2026-09-26 and 2026-09-27, and today is 2026-09-28
        val logs = listOf(
            DailyLogEntity(date = "2026-09-25", topicId = 1, problemsSolved = 4, minutes = 60),
            DailyLogEntity(date = "2026-09-24", topicId = 1, problemsSolved = 2, minutes = 35)
        )
        val settings = UserSettingsEntity(minMinutes = 30, longestStreak = 2)
        val streakInfo = StreakUtils.calculateStreak(logs, emptyList(), settings, today)

        // Streak is broken
        assertEquals(0, streakInfo.currentStreak)
        assertFalse(streakInfo.isTodayCompleted)
        // Since streak is already 0, no warning to save an existing streak
        assertFalse(streakInfo.todayWarning)
        // Historical longest streak of 2 days is preserved
        assertEquals(2, streakInfo.longestStreak)
    }

    @Test
    fun testCase3_Missed2Days_ThenLoggedTodayStartsNewStreak() {
        val today = LocalDate.of(2026, 9, 28)
        // Missed 2026-09-26 and 2026-09-27, but logged on 2026-09-25 and logged today 2026-09-28
        val logs = listOf(
            DailyLogEntity(date = "2026-09-28", topicId = 1, problemsSolved = 3, minutes = 50),
            DailyLogEntity(date = "2026-09-25", topicId = 1, problemsSolved = 2, minutes = 30)
        )
        val settings = UserSettingsEntity(minMinutes = 30, longestStreak = 1)
        val streakInfo = StreakUtils.calculateStreak(logs, emptyList(), settings, today)

        // Starts a fresh streak of 1 day
        assertEquals(1, streakInfo.currentStreak)
        assertTrue(streakInfo.isTodayCompleted)
        assertFalse(streakInfo.todayWarning)
    }

    // ==========================================
    // CASE 4: A Streak Freeze Used
    // ==========================================
    @Test
    fun testCase4_StreakFreezeBridgesYesterdayMissedDay() {
        val today = LocalDate.of(2026, 9, 28)
        // Logged on 26th, missed yesterday (27th) but used a freeze, logged today (28th)
        val logs = listOf(
            DailyLogEntity(date = "2026-09-28", topicId = 1, problemsSolved = 2, minutes = 30),
            DailyLogEntity(date = "2026-09-26", topicId = 1, problemsSolved = 3, minutes = 45)
        )
        val freezes = listOf(
            StreakFreezeEntity(date = "2026-09-27", monthKey = "2026-09")
        )
        val settings = UserSettingsEntity(minMinutes = 30, streakFreezeEnabled = true)
        val streakInfo = StreakUtils.calculateStreak(logs, freezes, settings, today)

        // Streak is 3 days connected across the freeze
        assertEquals(3, streakInfo.currentStreak)
        assertTrue(streakInfo.isTodayCompleted)
        assertFalse(streakInfo.todayWarning)
        assertEquals(1, streakInfo.freezesUsedThisMonth)
        assertEquals(1, streakInfo.freezesRemainingThisMonth)
    }

    @Test
    fun testCase4_StreakFreezeUsedYesterday_TodayPending() {
        val today = LocalDate.of(2026, 9, 28)
        // Logged on 26th, freeze used on 27th (yesterday), today (28th) not yet logged
        val logs = listOf(
            DailyLogEntity(date = "2026-09-26", topicId = 1, problemsSolved = 2, minutes = 40)
        )
        val freezes = listOf(
            StreakFreezeEntity(date = "2026-09-27", monthKey = "2026-09")
        )
        val settings = UserSettingsEntity(minMinutes = 30, streakFreezeEnabled = true)
        val streakInfo = StreakUtils.calculateStreak(logs, freezes, settings, today)

        // Streak is 2, today is pending, warning is displayed
        assertEquals(2, streakInfo.currentStreak)
        assertFalse(streakInfo.isTodayCompleted)
        assertTrue(streakInfo.todayWarning)
    }

    @Test
    fun testCase4_StreakFreezeDisabledInSettings_DoesNotBridgeStreak() {
        val today = LocalDate.of(2026, 9, 28)
        val logs = listOf(
            DailyLogEntity(date = "2026-09-28", topicId = 1, problemsSolved = 2, minutes = 30),
            DailyLogEntity(date = "2026-09-26", topicId = 1, problemsSolved = 3, minutes = 45)
        )
        val freezes = listOf(
            StreakFreezeEntity(date = "2026-09-27", monthKey = "2026-09")
        )
        // streakFreezeEnabled = false
        val settings = UserSettingsEntity(minMinutes = 30, streakFreezeEnabled = false)
        val streakInfo = StreakUtils.calculateStreak(logs, freezes, settings, today)

        // Because freezes are disabled, yesterday is missed, so current streak is only today (1)
        assertEquals(1, streakInfo.currentStreak)
    }

    @Test
    fun testCase4_IsolatedFreezeWithoutLogs_CannotCreateFakeStreak() {
        val today = LocalDate.of(2026, 9, 28)
        // User has no logs, only an isolated freeze on yesterday
        val freezes = listOf(
            StreakFreezeEntity(date = "2026-09-27", monthKey = "2026-09")
        )
        val settings = UserSettingsEntity(minMinutes = 30, streakFreezeEnabled = true)
        val streakInfo = StreakUtils.calculateStreak(emptyList(), freezes, settings, today)

        // Streak must be 0 because no actual practice occurred
        assertEquals(0, streakInfo.currentStreak)
        assertFalse(streakInfo.isTodayCompleted)
        assertFalse(streakInfo.todayWarning)
    }

    // ==========================================
    // MILESTONES CHECK
    // ==========================================
    @Test
    fun testMilestonesCheck() {
        assertEquals(7, StreakUtils.checkNewMilestone(currentStreak = 7, lastCelebrated = 0))
        assertEquals(null, StreakUtils.checkNewMilestone(currentStreak = 7, lastCelebrated = 7))
        assertEquals(30, StreakUtils.checkNewMilestone(currentStreak = 30, lastCelebrated = 7))
        assertEquals(50, StreakUtils.checkNewMilestone(currentStreak = 50, lastCelebrated = 30))
        assertEquals(100, StreakUtils.checkNewMilestone(currentStreak = 100, lastCelebrated = 50))
        assertEquals(365, StreakUtils.checkNewMilestone(currentStreak = 365, lastCelebrated = 100))
    }
}
