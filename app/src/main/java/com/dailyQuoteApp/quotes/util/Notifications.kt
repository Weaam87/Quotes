package com.dailyQuoteApp.quotes.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.dailyQuoteApp.quotes.MainActivity
import com.dailyQuoteApp.quotes.R
import com.dailyQuoteApp.quotes.util.Constants.NOTIFICATION_ID

@RequiresApi(Build.VERSION_CODES.M)
fun NotificationManager.sendNotification(applicationContext: Context, messageBody: String) {

    // Create the content intent for the notification, which launches this activity
    val contentIntent = Intent(applicationContext, MainActivity::class.java)

    // create PendingIntent
    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_IMMUTABLE
    )

    // add style
    val quoteImage = BitmapFactory.decodeResource(
        applicationContext.resources,
        R.drawable.quote_icon
    )

    val bigPicStyle = NotificationCompat.BigPictureStyle().
    bigPicture(quoteImage).
    bigLargeIcon(null as Bitmap?)  // Specify Bitmap?

    // Build the notification
    val builder = NotificationCompat.Builder(
        applicationContext, applicationContext.getString(R.string.notification_channel_id)).
    setSmallIcon(R.drawable.quote_icon).
    setContentTitle(applicationContext.getString(R.string.notification_title)).
    setContentText(messageBody).
    setContentIntent(contentPendingIntent).
    setAutoCancel(true).
    setStyle(bigPicStyle).
    setLargeIcon(quoteImage).
    setPriority(NotificationCompat.PRIORITY_HIGH)

    // call notify
    notify(NOTIFICATION_ID,builder.build())
}
