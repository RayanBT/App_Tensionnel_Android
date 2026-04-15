package com.example.apptensionnel.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.apptensionnel.data.models.Measurement
import com.example.apptensionnel.data.models.Profile
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_tensionnel_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_REMINDER_ENABLED = "reminder_enabled"
        private const val KEY_REMINDER_TIME = "reminder_time"
        private const val KEY_BACKUP_ENABLED = "backup_enabled"
        private const val KEY_MEASUREMENTS = "measurements"
        private const val KEY_PROFILES = "profiles"
        private const val KEY_CURRENT_PROFILE_ID = "current_profile_id"
        private const val KEY_THEME_MODE = "theme_mode" // 0: Auto, 1: Light, 2: Dark
    }

    var themeMode: Int
        get() = sharedPreferences.getInt(KEY_THEME_MODE, 0)
        set(value) = sharedPreferences.edit { putInt(KEY_THEME_MODE, value) }

    var currentProfileId: String?
        get() = sharedPreferences.getString(KEY_CURRENT_PROFILE_ID, null)
        set(value) = sharedPreferences.edit { putString(KEY_CURRENT_PROFILE_ID, value) }

    fun getCurrentProfile(): Profile? {
        val id = currentProfileId ?: return null
        return getProfiles().find { it.id == id }
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

    // --- PROFILES ---
    fun getProfiles(): List<Profile> {
        val jsonString = sharedPreferences.getString(KEY_PROFILES, null) ?: return emptyList()
        val profiles = mutableListOf<Profile>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                profiles.add(
                    Profile(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        iconColor = obj.getInt("iconColor"),
                        age = obj.optInt("age", 0),
                        weight = obj.optDouble("weight", 0.0).toFloat(),
                        height = obj.optInt("height", 0)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return profiles
    }

    fun addProfile(profile: Profile) {
        val profiles = getProfiles().toMutableList()
        profiles.add(profile)
        saveProfiles(profiles)
        if (currentProfileId == null) {
            currentProfileId = profile.id
        }
    }

    private fun saveProfiles(profiles: List<Profile>) {
        val jsonArray = JSONArray()
        profiles.forEach { p ->
            val obj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("iconColor", p.iconColor)
                put("age", p.age)
                put("weight", p.weight.toDouble())
                put("height", p.height)
            }
            jsonArray.put(obj)
        }
        sharedPreferences.edit { putString(KEY_PROFILES, jsonArray.toString()) }
    }

    // --- MEASUREMENTS ---
    fun addMeasurement(measurement: Measurement) {
        val measurements = getAllMeasurements().toMutableList()
        measurements.add(0, measurement)
        saveAllMeasurements(measurements)
    }

    fun updateMeasurement(updatedMeasurement: Measurement) {
        val measurements = getAllMeasurements().toMutableList()
        val index = measurements.indexOfFirst { it.id == updatedMeasurement.id }
        if (index != -1) {
            measurements[index] = updatedMeasurement
            saveAllMeasurements(measurements)
        }
    }

    fun deleteMeasurementById(id: String) {
        val measurements = getAllMeasurements().toMutableList()
        measurements.removeAll { it.id == id }
        saveAllMeasurements(measurements)
    }

    fun getMeasurementsForProfile(profileId: String): List<Measurement> {
        return getAllMeasurements().filter { it.profileId == profileId }
    }

    fun getMeasurements(): List<Measurement> {
        val profileId = currentProfileId ?: return emptyList()
        return getMeasurementsForProfile(profileId)
    }

    private fun getAllMeasurements(): List<Measurement> {
        val jsonString = sharedPreferences.getString(KEY_MEASUREMENTS, null) ?: return emptyList()
        val measurements = mutableListOf<Measurement>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                measurements.add(
                    Measurement(
                        id = obj.getString("id"),
                        profileId = obj.optString("profileId", ""), // Migration: default empty
                        systolic = obj.getInt("systolic"),
                        diastolic = obj.getInt("diastolic"),
                        pulse = obj.getInt("pulse"),
                        date = obj.getLong("date"),
                        notes = obj.optString("notes", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return measurements
    }

    private fun saveAllMeasurements(measurements: List<Measurement>) {
        val jsonArray = JSONArray()
        measurements.forEach { m ->
            val obj = JSONObject().apply {
                put("id", m.id)
                put("profileId", m.profileId)
                put("systolic", m.systolic)
                put("diastolic", m.diastolic)
                put("pulse", m.pulse)
                put("date", m.date)
                put("notes", m.notes)
            }
            jsonArray.put(obj)
        }
        sharedPreferences.edit { putString(KEY_MEASUREMENTS, jsonArray.toString()) }
    }

    fun generateFakeData(profileId: String? = null) {
        val targetId = profileId ?: currentProfileId ?: return
        val calendar = Calendar.getInstance()
        val random = Random()
        
        for (i in 30 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            
            val trendImprovement = (i.toFloat() / 30f) * 5f
            val baseSys = 135 - trendImprovement.toInt()
            val baseDia = 85 - (trendImprovement / 2).toInt()
            
            addMeasurement(
                Measurement(
                    profileId = targetId,
                    systolic = baseSys + random.nextInt(15) - 5,
                    diastolic = baseDia + random.nextInt(10) - 3,
                    pulse = 72 + random.nextInt(12) - 4,
                    date = calendar.timeInMillis,
                    notes = if (i % 5 == 0) "Après le café" else ""
                )
            )
        }
    }
}
