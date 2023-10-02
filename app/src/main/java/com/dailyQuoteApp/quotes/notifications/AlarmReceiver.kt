package com.dailyQuoteApp.quotes.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.quotes.R

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        // Get the system's notification manager
        val notificationManager = ContextCompat.getSystemService(
            context, NotificationManager::class.java
        ) as NotificationManager

        // Send a notification using a custom extension function "sendNotification"
        notificationManager.sendNotification(
            context.getText(R.string.notification_messageBody).toString(), context
        )
    }
}
