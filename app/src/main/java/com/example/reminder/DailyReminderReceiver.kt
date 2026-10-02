package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.db.AppDatabase
import com.example.data.model.UserSettingsEntity
import com.example.util.StreakUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate

class DailyReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_DAILY_REMINDER = "com.example.action.DAILY_REMINDER"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context, this)
                val logs = database.dailyLogDao().getAllLogs().firstOrNull() ?: emptyList()
                val freezes = database.streakFreezeDao().getAllFreezes().firstOrNull() ?: emptyList()
                val settings = database.userSettingsDao().getSettingsSync() ?: UserSettingsEntity()
                val problems = database.problemDao().getAllProblems().firstOrNull() ?: emptyList()

                val today = LocalDate.now()
                val todayStr = today.toString()

                // 1. Check if Daily Streak Reminder is enabled
                if (!settings.reminderEnabled) {
                    Log.d("DailyReminderReceiver", "Daily reminder is disabled in settings, skipping.")
                    return@launch
                }

                // 2. Prevent duplicate notifications for the same day
                if (settings.lastReminderDate == todayStr) {
                    Log.d("DailyReminderReceiver", "Daily reminder already sent today ($todayStr), skipping duplicate.")
                    DailyReminderScheduler.scheduleDailyReminder(context, settings.reminderTime)
                    return@launch
                }

                val streakInfo = StreakUtils.calculateStreak(
                    logs = logs,
                    freezes = freezes,
                    settings = settings,
                    referenceDate = today,
                    problems = problems
                )

                // 3. If today's streak requirement has already been completed, DO NOT send the notification.
                if (!streakInfo.isTodayCompleted) {
                    DailyReminderHelper.showReminderNotification(context, streakInfo.currentStreak)
                    // Record lastReminderDate to guarantee only one notification per day
                    database.userSettingsDao().insertOrUpdateSettings(settings.copy(lastReminderDate = todayStr))
                } else {
                    Log.d("DailyReminderReceiver", "Today's streak requirement already completed. Notification suppressed.")
                }

                // Automatically schedule the next day's reminder
                DailyReminderScheduler.scheduleDailyReminder(context, settings.reminderTime)
            } catch (e: Exception) {
                Log.e("DailyReminderReceiver", "Error evaluating streak reminder", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
