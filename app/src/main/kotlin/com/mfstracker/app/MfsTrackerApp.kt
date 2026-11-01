package com.mfstracker.app

import android.app.Application
import com.mfstracker.app.data.local.TrackerDatabase
import com.mfstracker.app.data.preferences.ThemePreferences
import com.mfstracker.app.data.repository.DefaultTrackerRepository
import com.mfstracker.app.data.repository.TrackerRepository

class MfsTrackerApp : Application() {

    lateinit var repository: TrackerRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = TrackerDatabase.getInstance(this)
        val preferences = ThemePreferences(this)
        repository = DefaultTrackerRepository(
            appContext = applicationContext,
            trackerDao = database.trackerDao(),
            themePreferences = preferences
        )
    }
}

