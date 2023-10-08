package com.dailyQuoteApp.quotes.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent

/**
 *  BootReceiver is used to ensure that the daily reminder alarm is rescheduled
 *  after the device reboots. It retrieves the selected time for the daily reminder
 * from SharedPreferences and sets the alarm accordingly.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            // Retrieve the stored daily reminder time from SharedPreferences
            val sharedPreferences = context.getSharedPreferences(context.packageName, MODE_PRIVATE)
            val dailyReminderTimeInMillis =
                sharedPreferences.getLong("dailyReminderTime", -1)

            // Check if a valid time is stored
            if (dailyReminderTimeInMillis != -1L) {
                // Create an instance of the AlarmService
                val alarmService = AlarmService(context)

                // Set the repetitive alarm using the AlarmService
                alarmService.setRepetitiveAlarm(dailyReminderTimeInMillis)
            }
        }
    }

    fun deleteBootReceiverSharedPreferences(context: Context) {
        val sharedPreferences = context.getSharedPreferences(context.packageName, MODE_PRIVATE)

        // Use an editor to remove the specific preference
        val editor = sharedPreferences.edit()
        editor.remove("dailyReminderTime")
        editor.apply()
    }
}
