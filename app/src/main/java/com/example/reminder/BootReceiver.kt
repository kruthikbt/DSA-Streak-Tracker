package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            Log.d("BootReceiver", "Device rebooted or package replaced, rescheduling daily reminder")
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = AppDatabase.getDatabase(context, this)
                    val settings = database.userSettingsDao().getSettingsSync()
                    if (settings == null || settings.reminderEnabled) {
                        val reminderTime = settings?.reminderTime ?: "20:00"
                        DailyReminderScheduler.scheduleDailyReminder(context, reminderTime)
                        Log.d("BootReceiver", "Rescheduled daily reminder for $reminderTime on boot")
                    } else {
                        Log.d("BootReceiver", "Daily reminder disabled by user, skipping reboot schedule")
                    }
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Failed to reschedule reminder after reboot", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
