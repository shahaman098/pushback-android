package com.pushback.app

import android.app.Application
import com.pushback.app.data.PushBackDatabase
import com.pushback.app.data.Repository
import com.pushback.app.data.SettingsStore
import com.pushback.app.domain.MentalityEngine
import com.pushback.app.notify.CoachNotifications

class AppContainer(app: Application) {
    val db = PushBackDatabase.create(app)
    val settings = SettingsStore(app)
    val mentality = MentalityEngine(app)
    val repo = Repository(app, db, settings, mentality)
}

class PushBackApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        CoachNotifications.ensureChannels(this)
    }
}
