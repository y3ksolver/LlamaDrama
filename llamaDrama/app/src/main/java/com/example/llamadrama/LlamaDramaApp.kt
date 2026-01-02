package com.example.llamadrama

import android.app.Application
import com.example.llamadrama.data.database.AppDatabase
import com.example.llamadrama.data.repository.TeamRepository

class LlamaDramaApp : Application() {
    
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: TeamRepository by lazy { TeamRepository(database) }
}

