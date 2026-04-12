package com.example.apptensionnel.data

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result

class ReminderWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val preferenceManager = PreferenceManager(applicationContext)
        val notificationHelper = NotificationHelper(applicationContext)
        
        if (preferenceManager.isReminderEnabled) {
            notificationHelper.sendTestNotification()
            
            // Programmer le rappel pour le lendemain à la même heure
            notificationHelper.scheduleDailyReminder(preferenceManager.reminderTime)
        }

        return Result.success()
    }
}
