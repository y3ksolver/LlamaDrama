package com.dramallama.app

import android.app.Application
import com.dramallama.app.data.database.AppDatabase
import com.dramallama.app.data.repository.TeamRepository
import com.dramallama.app.notification.NotificationHelper
import com.dramallama.app.worker.ReminderWorker

class LlamaDramaApp : Application() {
    
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: TeamRepository by lazy { TeamRepository(database) }
    
    override fun onCreate() {
        super.onCreate()
        
        // Create notification channel
        NotificationHelper.createNotificationChannel(this)
        
        // Schedule daily reminder check
        ReminderWorker.schedule(this)
    }
}

