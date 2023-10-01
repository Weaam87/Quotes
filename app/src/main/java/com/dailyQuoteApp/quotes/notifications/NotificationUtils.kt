package com.dailyQuoteApp.quotes.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.dailyQuoteApp.quotes.MainActivity
import com.example.quotes.R


private const val NOTIFICATION_ID = 0

// Extension function to send a notification
fun NotificationManager.sendNotification(messageBody: String, applicationContext: Context) {

    // Create an intent to open the main activity when the notification is clicked
    val contentIntent = Intent(applicationContext, MainActivity::class.java)

    // Create a pending intent to handle the intent later
    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_IMMUTABLE
    )

    // Load a bitmap for the notification's big picture style
    val quoteImage = BitmapFactory.decodeResource(
        applicationContext.resources,
        R.drawable.quote_icon
    )

    // Create a BigPictureStyle for the notification
    val bigPicStyle = NotificationCompat.BigPictureStyle().bigPicture(quoteImage)

    // Build the notification using NotificationCompat.Builder
    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.notification_channel_id)
    )
        .setSmallIcon(R.drawable.quote_icon)
        .setContentTitle(applicationContext.getString(R.string.notification_title))
        .setContentText(messageBody)
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)
        .setStyle(bigPicStyle)
        .setLargeIcon(quoteImage)
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    // Notify the notification manager to display the notification
    notify(NOTIFICATION_ID, builder.build())
}

// Extension function to cancel all notifications
fun NotificationManager.cancelNotification() {
    cancelAll()
}
