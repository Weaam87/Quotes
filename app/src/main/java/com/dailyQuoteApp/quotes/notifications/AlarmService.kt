package com.dailyQuoteApp.quotes.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings


class AlarmService(private val context: Context) {

    private val alarmManager: AlarmManager? =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

    fun cancelAlarm() {
        val intent = getIntent().apply {
            action = "notification"
        }
        val pendingIntent = getPendingIntent(intent)
        alarmManager?.cancel(pendingIntent)
    }

    fun setRepetitiveAlarm(timeInMillis: Long) {
        alarmManager?.let { alarmManager ->
            // Create an intent for the alarm with "notification" action
            val intent = getIntent().apply { action = "notification" }

            // Create a PendingIntent from the intent
            val pendingIntent = getPendingIntent(intent)

            // Calculate the time for the next alarm, which will be 24 hours from the current time
            val repeatInterval = 24 * 60 * 60 * 1000L // 24 hours in milliseconds

            // 1 minute for testing purposes only
            //val repeatInterval = 60 * 1000L

            val alarmType = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms() -> AlarmManager.RTC_WAKEUP
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> AlarmManager.RTC_WAKEUP
                else -> AlarmManager.RTC_WAKEUP
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    // Exact alarms are not allowed, request permission
                    val requestIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    context.startActivity(requestIntent)
                }
                alarmManager.setExactAndAllowWhileIdle(alarmType, timeInMillis, pendingIntent)
            } else {
                alarmManager.setExact(alarmType, timeInMillis, pendingIntent)
            }

            alarmManager.setRepeating(
                alarmType,
                timeInMillis + repeatInterval,
                repeatInterval,
                pendingIntent
            )
        }
    }

    private fun getPendingIntent(intent: Intent) =
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

    private fun getIntent() = Intent(context, AlarmReceiver::class.java)
}
