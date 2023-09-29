package com.dailyQuoteApp.quotes

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode

/**
 * Custom Application class responsible for initializing the app's theme mode
 * based on the user's preference stored in SharedPreferences when the app starts.
 * This ensures that the theme mode is set before any activity is created,
 * preventing any delay or flicker in theme changes when the app is launched.
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Retrieve the user's preference for theme mode from SharedPreferences
        val sharedPreferences = getSharedPreferences("ModePrefs", Context.MODE_PRIVATE)
        val savedThemeMode = sharedPreferences.getInt("theme_mode", MODE_NIGHT_FOLLOW_SYSTEM)

        // Apply the user's preferred theme mode
        setDefaultNightMode(savedThemeMode)
    }
}


