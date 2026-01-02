package com.dramallama.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.dramallama.app.data.database.AppDatabase
import com.dramallama.app.notification.NotificationHelper
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        private const val WORK_NAME = "overdue_reminder"
        private const val OVERDUE_THRESHOLD_DAYS = 14L
        
        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
                1, TimeUnit.DAYS
            ).build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
    
    override suspend fun doWork(): Result {
        val today = LocalDate.now()
        val database = AppDatabase.getDatabase(applicationContext)
        val members = database.teamMemberDao().getAllMembers()
        
        val overdueMembers = members.filter { member ->
            val lastContact = member.lastContactEpochDay?.let { LocalDate.ofEpochDay(it) }
            if (lastContact == null) {
                true // Never contacted = overdue
            } else {
                ChronoUnit.DAYS.between(lastContact, today) > OVERDUE_THRESHOLD_DAYS
            }
        }
        
        if (overdueMembers.isNotEmpty()) {
            NotificationHelper.showOverdueNotification(
                applicationContext,
                overdueMembers.size,
                overdueMembers.map { it.name }
            )
        }
        
        return Result.success()
    }
}

