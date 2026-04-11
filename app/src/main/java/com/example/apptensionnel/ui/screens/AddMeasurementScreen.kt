package com.example.apptensionnel.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apptensionnel.data.PreferenceManager
import com.example.apptensionnel.data.models.Measurement
import com.example.apptensionnel.ui.theme.AppBlue
import com.example.apptensionnel.ui.theme.StatusCrisis
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddMeasurementScreen(
    preferenceManager: PreferenceManager,
    measurementToEdit: Measurement? = null,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) }
    var systolic by remember { mutableStateOf(measurementToEdit?.systolic?.toString() ?: "") }
    var diastolic by remember { mutableStateOf(measurementToEdit?.diastolic?.toString() ?: "") }
    var pulse by remember { mutableStateOf(measurementToEdit?.pulse?.toString() ?: "") }
    var notes by remember { mutableStateOf(measurementToEdit?.notes ?: "") }
    
    val context = LocalContext.current
    val totalSteps = 4

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header Bleu identique aux images
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppBlue)
                .padding(top = 32.dp, bottom = 20.dp, start = 16.dp, end = 16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (currentStep > 1) currentStep-- else onBack() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = if (measurementToEdit == null) "Nouvelle Mesure" else "Modifier la Mesure",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                
                // Progress Bars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(totalSteps) { index ->
                        val stepIndex = index + 1
                        val alpha = if (stepIndex <= currentStep) 1f else 0.3f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = alpha))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Étape $currentStep sur $totalSteps", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            }
        }

        // Zone de contenu (ajustée pour éviter le scroll)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            when (currentStep) {
                1 -> InputStep(
                    title = "Systolique",
                    description = "Pression maximale lors du battement cardiaque",
                    value = systolic,
                    unit = "mmHg",
                    range = "70 – 250 mmHg",
                    onValueChange = { if (it.length <= 3) systolic = it }
                )
                2 -> InputStep(
                    title = "Diastolique",
                    description = "Pression minimale entre les battements",
                    value = diastolic,
                    unit = "mmHg",
                    range = "40 – 150 mmHg",
                    onValueChange = { if (it.length <= 3) diastolic = it }
                )
                3 -> InputStep(
                    title = "Pouls",
                    description = "Fréquence cardiaque par minute",
                    value = pulse,
                    unit = "bpm",
                    range = "30 – 200 bpm",
                    isPulse = true,
                    onValueChange = { if (it.length <= 3) pulse = it }
                )
                4 -> SummaryStep(
                    systolic = systolic,
                    diastolic = diastolic,
                    pulse = pulse,
                    notes = notes,
                    onNotesChange = { notes = it },
                    date = measurementToEdit?.date ?: System.currentTimeMillis()
                )
            }
        }

        // Bouton de navigation bas
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (currentStep < totalSteps) {
                        currentStep++
                    } else {
                        val sys = systolic.toIntOrNull() ?: 0
                        val dia = diastolic.toIntOrNull() ?: 0
                        val pls = pulse.toIntOrNull() ?: 0
                        
                        // Règle métier : Alerte Crise (Systolique > 180 ou Diastolique > 120)
                        if (sys > 180 || dia > 120) {
                            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                                vibratorManager.defaultVibrator
                            } else {
                                @Suppress("DEPRECATION")
                                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            }
                            
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator.vibrate(500)
                            }
                        }

                        if (measurementToEdit == null) {
                            preferenceManager.addMeasurement(
                                Measurement(
                                    profileId = preferenceManager.currentProfileId ?: "",
                                    systolic = sys,
                                    diastolic = dia,
                                    pulse = pls,
                                    notes = notes
                                )
                            )
                        } else {
                            preferenceManager.updateMeasurement(
                                measurementToEdit.copy(
                                    systolic = sys,
                                    diastolic = dia,
                                    pulse = pls,
                                    notes = notes
                                )
                            )
                        }
                        onSave()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                enabled = when(currentStep) {
                    1 -> systolic.isNotEmpty()
                    2 -> diastolic.isNotEmpty()
                    3 -> pulse.isNotEmpty()
                    else -> true
                }
            ) {
                if (currentStep < totalSteps) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Suivant", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (measurementToEdit == null) "Enregistrer" else "Mettre à jour", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (currentStep == totalSteps) {
                TextButton(onClick = { currentStep-- }) {
                    Text("← Retour", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun InputStep(
    title: String,
    description: String,
    value: String,
    unit: String,
    range: String,
    isPulse: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = if (isPulse) StatusCrisis else AppBlue)
        Text(text = description, color = Color.Gray, fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(16.dp))

        // Affichage de la valeur
        Card(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FB))
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (value.isEmpty()) {
                        Text(text = "— — —", fontSize = 42.sp, color = Color(0xFFDDE2E9))
                    } else {
                        Text(text = value, fontSize = 48.sp, fontWeight = FontWeight.Bold, color = AppBlue)
                    }
                    Text(text = unit, color = Color.Gray, fontSize = 14.sp)
                }
            }
        }
        
        Text(
            text = "Plage : $range",
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            textAlign = TextAlign.Center,
            color = Color.LightGray,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        // Pavé numérique compact
        NumericKeypad(onValueChange = onValueChange, currentValue = value)
    }
}

@Composable
fun NumericKeypad(onValueChange: (String) -> Unit, currentValue: String) {
    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "DEL")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (i in 0 until 4) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (j in 0 until 3) {
                    val key = keys[i * 3 + j]
                    if (key.isNotEmpty()) {
                        Surface(
                            onClick = {
                                if (key == "DEL") {
                                    if (currentValue.isNotEmpty()) onValueChange(currentValue.dropLast(1))
                                } else if (currentValue.length < 3) {
                                    onValueChange(currentValue + key)
                                }
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = if (key == "DEL") Color(0xFFFFEBEE) else Color.White,
                            shadowElevation = 1.dp
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                if (key == "DEL") {
                                    Icon(Icons.Default.Backspace, contentDescription = null, tint = StatusCrisis, modifier = Modifier.size(20.dp))
                                } else {
                                    Text(text = key, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryStep(
    systolic: String,
    diastolic: String,
    pulse: String,
    notes: String,
    onNotesChange: (String) -> Unit,
    date: Long = System.currentTimeMillis()
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "Note (optionnel)", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        Text(text = "Contexte de la mesure (activité, humeur...)", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            modifier = Modifier.fillMaxWidth().height(100.dp),
            placeholder = { Text("Ex: Après une promenade, stress au travail...") },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF8F9FB),
                unfocusedContainerColor = Color(0xFFF8F9FB),
                focusedBorderColor = AppBlue,
                unfocusedBorderColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "RÉCAPITULATIF", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                Spacer(modifier = Modifier.height(8.dp))
                SummaryRow("Systolique", "$systolic mmHg", AppBlue)
                SummaryRow("Diastolique", "$diastolic mmHg", AppBlue)
                SummaryRow("Pouls", "$pulse bpm", StatusCrisis)
                SummaryRow("Date", SimpleDateFormat("d MMMM yyyy, HH:mm", Locale.FRENCH).format(Date(date)), Color.DarkGray)
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 15.sp)
        Text(text = value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}
