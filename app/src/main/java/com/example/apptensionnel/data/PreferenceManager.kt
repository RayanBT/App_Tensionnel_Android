package com.example.apptensionnel.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.apptensionnel.data.models.Measurement
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

    fun addMeasurement(measurement: Measurement) {
        val measurements = getMeasurements().toMutableList()
        measurements.add(0, measurement) // Ajoute au début (plus récent d'abord)
        saveMeasurements(measurements)
    }

    fun deleteLastMeasurement() {
        val measurements = getMeasurements().toMutableList()
        if (measurements.isNotEmpty()) {
            measurements.removeAt(0)
            saveMeasurements(measurements)
        }
    }

    fun deleteAllMeasurements() {
        sharedPreferences.edit { remove(KEY_MEASUREMENTS) }
    }

    fun getMeasurements(): List<Measurement> {
        val jsonString = sharedPreferences.getString(KEY_MEASUREMENTS, null) ?: return emptyList()
        val measurements = mutableListOf<Measurement>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                measurements.add(
                    Measurement(
                        id = obj.getString("id"),
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

    fun generateFakeData() {
        val calendar = Calendar.getInstance()
        val fakeMeasurements = mutableListOf<Measurement>()
        val random = Random()
        
        // Génère des données pour les 30 derniers jours
        for (i in 30 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            
            // Variations réalistes (Légère tendance à la baisse pour faire "joli")
            val trendImprovement = (i.toFloat() / 30f) * 5f // Amélioration de 5mmHg sur 30 jours
            val baseSys = 135 - trendImprovement.toInt()
            val baseDia = 85 - (trendImprovement / 2).toInt()
            val basePulse = 72
            
            fakeMeasurements.add(
                Measurement(
                    systolic = baseSys + random.nextInt(15) - 5,
                    diastolic = baseDia + random.nextInt(10) - 3,
                    pulse = basePulse + random.nextInt(12) - 4,
                    date = calendar.timeInMillis,
                    notes = "Donnée simulée"
                )
            )
        }
        // On sauvegarde (en inversant pour avoir le plus récent en premier comme attendu par l'app)
        saveMeasurements(fakeMeasurements.reversed())
    }

    private fun saveMeasurements(measurements: List<Measurement>) {
        val jsonArray = JSONArray()
        measurements.forEach { m ->
            val obj = JSONObject().apply {
                put("id", m.id)
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
}
