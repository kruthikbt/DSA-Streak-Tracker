package com.example.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

object DailyReminderScheduler {

    const val REMINDER_REQUEST_CODE = 2001

    fun scheduleDailyReminder(context: Context, reminderTimeStr: String = "20:00") {
        try {
            // Cancel any old scheduled alarm first to ensure only one active alarm exists
            cancelDailyReminder(context)

            val parts = reminderTimeStr.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 20
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                // If reminder time has already passed today, schedule for tomorrow
                if (before(now)) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, DailyReminderReceiver::class.java).apply {
                action = DailyReminderReceiver.ACTION_DAILY_REMINDER
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REMINDER_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        target.timeInMillis,
                        pendingIntent
                    )
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        target.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        target.timeInMillis,
                        pendingIntent
                    )
                }
            } catch (secEx: SecurityException) {
                Log.w("DailyReminderScheduler", "Exact alarm permission unavailable, falling back to setAndAllowWhileIdle", secEx)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        target.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        target.timeInMillis,
                        pendingIntent
                    )
                }
            }
            Log.d("DailyReminderScheduler", "Scheduled daily streak reminder for: ${target.time}")
        } catch (e: Exception) {
            Log.e("DailyReminderScheduler", "Failed to schedule daily reminder", e)
        }
    }

    fun cancelDailyReminder(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, DailyReminderReceiver::class.java).apply {
                action = DailyReminderReceiver.ACTION_DAILY_REMINDER
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REMINDER_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            Log.e("DailyReminderScheduler", "Failed to cancel daily reminder", e)
        }
    }
}
