package com.dailyQuoteApp.quotes.notifications

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi


class AlarmService(private val context: Context) {

    private val alarmManager: AlarmManager? =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

    @SuppressLint("BatteryLife")
    private fun setAlarm(timeInMillis: Long, pendingIntent: PendingIntent) {
        alarmManager?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // Use canScheduleExactAlarms for Android 12 (API level 31) and higher
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            timeInMillis,
                            pendingIntent
                        )
                    } else {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        context.startActivity(intent)
                    }
                } else {
                    // Use setExactAndAllowWhileIdle for Android 8.0 (API level 26) to Android 11 (API level 30)
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                // Use setExact for Android versions below 8.0 (API level 26)
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
            }
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

    //1 Day


    fun setRepetitiveAlarm(timeInMillis: Long) {
        setAlarm(
            timeInMillis,
            getPendingIntent(
                getIntent().apply {
                    action = "notification"
                }
            )
        )
    }
}