package com.example

import com.example.data.model.DailyLogEntity
import com.example.data.model.ProblemEntity
import com.example.data.model.StreakFreezeEntity
import com.example.data.model.UserSettingsEntity
import com.example.util.StreakUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DailyReminderTest {

    @Test
    fun testReminderSuppressedWhenTodayCompleted() {
        val today = LocalDate.of(2026, 10, 1)
        val logs = listOf(
            DailyLogEntity(date = "2026-10-01", topicId = 1, problemsSolved = 1, minutes = 30)
        )
        val settings = UserSettingsEntity(minMinutes = 30)
        val streakInfo = StreakUtils.calculateStreak(
            logs = logs,
            freezes = emptyList(),
            settings = settings,
            referenceDate = today
        )

        // Today is completed -> notification should be suppressed
        assertTrue(streakInfo.isTodayCompleted)
    }

    @Test
    fun testReminderSuppressedWhenProblemSolvedToday() {
        val today = LocalDate.of(2026, 10, 1)
        val problems = listOf(
            ProblemEntity(
                id = "p1",
                topicId = 1,
                title = "Two Sum",
                difficulty = "Easy",
                solved = true,
                solvedDate = "2026-10-01"
            )
        )
        val settings = UserSettingsEntity(minMinutes = 30)
        val streakInfo = StreakUtils.calculateStreak(
            logs = emptyList(),
            freezes = emptyList(),
            settings = settings,
            referenceDate = today,
            problems = problems
        )

        assertTrue(streakInfo.isTodayCompleted)
    }

    @Test
    fun testReminderNeededWhenTodayPendingWithActiveStreak() {
        val today = LocalDate.of(2026, 10, 1)
        // Yesterday was completed, today is not yet done
        val logs = listOf(
            DailyLogEntity(date = "2026-09-30", topicId = 1, problemsSolved = 2, minutes = 45),
            DailyLogEntity(date = "2026-09-29", topicId = 1, problemsSolved = 1, minutes = 30)
        )
        val settings = UserSettingsEntity(minMinutes = 30)
        val streakInfo = StreakUtils.calculateStreak(
            logs = logs,
            freezes = emptyList(),
            settings = settings,
            referenceDate = today
        )

        assertFalse(streakInfo.isTodayCompleted)
        assertEquals(2, streakInfo.currentStreak)

        val title = if (streakInfo.currentStreak > 0) {
            "🔥 Keep your DSA streak alive!"
        } else {
            "🚀 Start your DSA streak!"
        }

        val message = if (streakInfo.currentStreak > 0) {
            "You haven't completed today's DSA practice yet. Solve a problem and keep your ${streakInfo.currentStreak}-day streak alive!"
        } else {
            "Complete today's DSA practice and start building your streak."
        }

        assertEquals("🔥 Keep your DSA streak alive!", title)
        assertEquals(
            "You haven't completed today's DSA practice yet. Solve a problem and keep your 2-day streak alive!",
            message
        )
    }

    @Test
    fun testReminderNeededForZeroDayStreak() {
        val today = LocalDate.of(2026, 10, 1)
        val settings = UserSettingsEntity(minMinutes = 30)
        val streakInfo = StreakUtils.calculateStreak(
            logs = emptyList(),
            freezes = emptyList(),
            settings = settings,
            referenceDate = today
        )

        assertFalse(streakInfo.isTodayCompleted)
        assertEquals(0, streakInfo.currentStreak)

        val title = if (streakInfo.currentStreak > 0) {
            "🔥 Keep your DSA streak alive!"
        } else {
            "🚀 Start your DSA streak!"
        }

        val message = if (streakInfo.currentStreak > 0) {
            "You haven't completed today's DSA practice yet. Solve a problem and keep your ${streakInfo.currentStreak}-day streak alive!"
        } else {
            "Complete today's DSA practice and start building your streak."
        }

        assertEquals("🚀 Start your DSA streak!", title)
        assertEquals(
            "Complete today's DSA practice and start building your streak.",
            message
        )
    }

    @Test
    fun testDefaultSettingsHas8PMAndEnabled() {
        val defaultSettings = UserSettingsEntity()
        assertEquals("20:00", defaultSettings.reminderTime)
        assertTrue(defaultSettings.reminderEnabled)
        assertEquals("", defaultSettings.lastReminderDate)
    }

    @Test
    fun testDuplicateReminderSuppressedIfAlreadySentToday() {
        val today = LocalDate.of(2026, 10, 1)
        val todayStr = today.toString()
        val settings = UserSettingsEntity(lastReminderDate = todayStr)

        // If today's reminder date equals today, reminder must not be re-sent
        val shouldSend = settings.reminderEnabled && settings.lastReminderDate != todayStr
        assertFalse(shouldSend)
    }

    @Test
    fun testReminderSuppressedWhenDisabled() {
        val today = LocalDate.of(2026, 10, 1)
        val todayStr = today.toString()
        val settings = UserSettingsEntity(reminderEnabled = false, lastReminderDate = "")

        val shouldSend = settings.reminderEnabled && settings.lastReminderDate != todayStr
        assertFalse(shouldSend)
    }
}
