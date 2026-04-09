package com.example.apptensionnel.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_tensionnel_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_REMINDER_ENABLED = "reminder_enabled"
        private const val KEY_REMINDER_TIME = "reminder_time"
        private const val KEY_BACKUP_ENABLED = "backup_enabled"
    }

    var isReminderEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_REMINDER_ENABLED, true)
        set(value) = sharedPreferences.edit { putBoolean(KEY_REMINDER_ENABLED, value) }

    var reminderTime: String
        get() = sharedPreferences.getString(KEY_REMINDER_TIME, "08:00") ?: "08:00"
        set(value) = sharedPreferences.edit { putString(KEY_REMINDER_TIME, value) }

    var isBackupEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_BACKUP_ENABLED, true)
        set(value) = sharedPreferences.edit { putBoolean(KEY_BACKUP_ENABLED, value) }
}
