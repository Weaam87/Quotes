package com.example.quotes.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.quotes.service.AlarmService
import com.example.quotes.util.Constants
import com.example.quotes.util.sendNotification
import java.util.*
import java.util.concurrent.TimeUnit

class AlarmReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = ContextCompat.getSystemService(
            context, NotificationManager::class.java
        ) as NotificationManager

        when (intent.action) {
            Constants.ACTION_SET_REPETITIVE_EXACT -> {

                setRepetitiveAlarm(AlarmService(context))
                notificationManager.sendNotification(context, "Don't forget to get today's quote")
            }
        }
    }

    private fun setRepetitiveAlarm(alarmService: AlarmService) {
        val calendar = Calendar.getInstance().apply {
            this.timeInMillis = timeInMillis + TimeUnit.HOURS.toMillis(24)
        }
        alarmService.setRepetitiveAlarm(calendar.timeInMillis)
    }
}