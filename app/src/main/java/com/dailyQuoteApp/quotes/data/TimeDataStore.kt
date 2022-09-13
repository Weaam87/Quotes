package com.dailyQuoteApp.quotes.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val TIME = "time"

private val Context.dataStore : DataStore<Preferences> by preferencesDataStore(name = TIME)

class TimeDataStore(context: Context) {

    private val SELECTED_TIME = stringPreferencesKey("selected_time")

    //Write to the Preferences DataStore
    suspend fun saveTimeToPreferencesStore(selectedTime : String , context: Context) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_TIME] = selectedTime
        }
    }

    //Read from the Preferences DataStore
    val preferenceFlow: Flow<String> = context.dataStore.data.catch {
        if (it is IOException) {
            it.printStackTrace()
            emit(emptyPreferences())
        } else {
            throw it
        }
    }.map { preferences ->

        preferences[SELECTED_TIME] ?: "first_run"
    }

    /** Exception handling شرح
     * As DataStore reads and writes data from files, IOExceptions may occur when accessing the data.
     * You handle these using the catch() operator to catch exceptions.
     * To keep things simple, since we don't expect any other types of exceptions here,
     * if a different type of exception is thrown, re-throw it.
     */
}