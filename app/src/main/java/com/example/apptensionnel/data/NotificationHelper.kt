package com.example.apptensionnel.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationHelper(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "reminders"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Rappels de tension",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications pour ne pas oublier de prendre sa tension"
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleDailyReminder(time: String) {
        val workManager = WorkManager.getInstance(context)
        
        val parts = time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0) // Très important pour le calcul précis
        }

        val now = Calendar.getInstance()
        if (calendar.before(now)) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        val delay = calendar.timeInMillis - System.currentTimeMillis()
        Log.d("TensioCare", "Rappel programmé dans ${delay / 1000 / 60} minutes (à $time)")

        val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("daily_reminder")
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.MINUTES)
            .build()

        // Utilise UniqueWork pour éviter d'avoir 50 rappels en même temps si on change l'heure souvent
        workManager.enqueueUniqueWork(
            "daily_reminder_work",
            ExistingWorkPolicy.REPLACE,
            reminderRequest
        )
    }

    fun cancelAllReminders() {
        WorkManager.getInstance(context).cancelUniqueWork("daily_reminder_work")
        Log.d("TensioCare", "Tous les rappels ont été annulés")
    }

    fun sendTestNotification() {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Rappel TensioCare")
            .setContentText("C'est l'heure de prendre votre tension ! 🩺")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
