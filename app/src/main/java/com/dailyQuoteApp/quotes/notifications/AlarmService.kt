package com.dailyQuoteApp.quotes.notifications

import android.annotation.SuppressLint
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

    @SuppressLint("BatteryLife")
    fun setRepetitiveAlarm(timeInMillis: Long) {
        alarmManager?.let {
            // Calculate the time for the next alarm, which will be 24 hours from the current time
            val repeatInterval = 24 * 60 * 60 * 1000L // 24 hours in milliseconds


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            timeInMillis,
                            getPendingIntent(
                                getIntent().apply {
                                    action = "notification"
                                }
                            )
                        )

                        // Schedule the alarm to repeat every 24 hours
                        alarmManager.setRepeating(
                            AlarmManager.RTC_WAKEUP,
                            timeInMillis + repeatInterval,
                            repeatInterval,
                            getPendingIntent(
                                getIntent().apply {
                                    action = "notification"
                                }
                            )
                        )
                    } else {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        context.startActivity(intent)
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        timeInMillis,
                        getPendingIntent(
                            getIntent().apply {
                                action = "notification"
                            }
                        )
                    )

                    // Schedule the alarm to repeat every 24 hours
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        timeInMillis + repeatInterval,
                        repeatInterval,
                        getPendingIntent(
                            getIntent().apply {
                                action = "notification"
                            }
                        )
                    )
                }
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    getPendingIntent(
                        getIntent().apply {
                            action = "notification"
                        }
                    )
                )

                // Schedule the alarm to repeat every 24 hours
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis + repeatInterval,
                    repeatInterval,
                    getPendingIntent(
                        getIntent().apply {
                            action = "notification"
                        }
                    )
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
}
