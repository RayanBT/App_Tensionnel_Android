package com.example.apptensionnel.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apptensionnel.data.PreferenceManager
import com.example.apptensionnel.data.models.Measurement
import com.example.apptensionnel.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    preferenceManager: PreferenceManager,
    onNavigateToAdd: () -> Unit
) {
    val measurements by remember { mutableStateOf(preferenceManager.getMeasurements()) }
    val lastMeasurement = measurements.firstOrNull()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FA))) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { HeaderSection() }

            item {
                LastMeasurementCard(lastMeasurement, measurements.getOrNull(1))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val avg7 = calculateAverage(measurements, 7)
                    val avg30 = calculateAverage(measurements, 30)

                    SummaryCard(
                        label = "Moy. 7 jours",
                        value = if (avg7.first > 0) "${avg7.first}" else "--",
                        subtitle = "/ ${if (avg7.second > 0) avg7.second else "--"} mmHg",
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        label = "Moy. 30 jours",
                        value = if (avg30.first > 0) "${avg30.first}" else "--",
                        subtitle = "/ ${if (avg30.second > 0) avg30.second else "--"} mmHg",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item { ReferenceGuideCard(modifier = Modifier.padding(horizontal = 16.dp)) }

            item {
                RecentMeasurementsCard(
                    measurements = measurements.take(5),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }

        FloatingActionButton(
            onClick = onNavigateToAdd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 16.dp),
            containerColor = AppBlue,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Saisir une mesure", modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun HeaderSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppBlue)
            .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 60.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(text = "Bonjour, Jean-Pierre 👋", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
                Text(text = "Tableau de Bord", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(
                onClick = { /* Notifications */ },
                modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@Composable
fun LastMeasurementCard(current: Measurement?, previous: Measurement?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .offset(y = (-40).dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            if (current != null) {
                val status = getStatus(current.systolic, current.diastolic)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(status.bgColor)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(status.color))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = status.label, color = status.color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                val sdf = SimpleDateFormat("d MMMM yyyy à HH:mm", Locale.FRENCH)
                Text(text = "Dernière mesure • ${sdf.format(Date(current.date))}", color = Color.Gray, fontSize = 13.sp)
                Text(text = "SYSTOLIQUE / DIASTOLIQUE", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${current.systolic}", fontSize = 56.sp, fontWeight = FontWeight.Bold, color = AppBlue)
                    Text(text = " / ", fontSize = 40.sp, color = Color.LightGray)
                    Text(text = "${current.diastolic}", fontSize = 56.sp, fontWeight = FontWeight.Bold, color = AppBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "mmHg", color = Color.Gray, fontSize = 18.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (previous != null) {
                        val sysDiff = current.systolic - previous.systolic
                        val diaDiff = current.diastolic - previous.diastolic
                        TrendItem("Sys ${if (sysDiff >= 0) "+" else ""}$sysDiff", if (sysDiff > 0) StatusStage2 else StatusNormal, sysDiff > 0)
                        Spacer(modifier = Modifier.width(16.dp))
                        TrendItem("Dia ${if (diaDiff >= 0) "+" else ""}$diaDiff", if (diaDiff > 0) StatusStage2 else StatusNormal, diaDiff > 0)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "vs précédente", color = Color.LightGray, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF8F9FB)).padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Color.White).padding(8.dp), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Fréquence cardiaque", color = Color.Gray, fontSize = 12.sp)
                            Text(text = "${current.pulse} bpm", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            } else {
                Text("Aucune mesure enregistrée", modifier = Modifier.padding(20.dp), color = Color.Gray)
            }
        }
    }
}

@Composable
fun TrendItem(text: String, color: Color, isUp: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (isUp) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SummaryCard(label: String, value: String, subtitle: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.offset(y = (-30).dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AppBlue)
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 3.dp))
            }
        }
    }
}

@Composable
fun ReferenceGuideCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "GUIDE DE RÉFÉRENCE", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(16.dp))
            ReferenceRow("Normale", "< 120 / 80", StatusNormal, StatusNormalBg)
            ReferenceRow("Élevée", "120–129 / < 80", StatusElevated, StatusElevatedBg)
            ReferenceRow("Hypert. Stade 1", "130–139 / 80–89", StatusStage1, StatusStage1Bg)
            ReferenceRow("Hypert. Stade 2", "≥ 140 / ≥ 90", StatusStage2, StatusStage2Bg)
            ReferenceRow("Crise", "> 180 / > 120", StatusCrisis, StatusCrisisBg)
        }
    }
}

@Composable
fun ReferenceRow(label: String, value: String, color: Color, bgColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(bgColor).padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(text = label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Text(text = value, fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun RecentMeasurementsCard(measurements: List<Measurement>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Mesures récentes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                TextButton(onClick = { /* Voir tout */ }, contentPadding = PaddingValues(0.dp)) {
                    Text(text = "Voir tout", color = AppBlue)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = AppBlue, modifier = Modifier.size(20.dp))
                }
            }
            
            measurements.forEachIndexed { index, measurement ->
                val status = getStatus(measurement.systolic, measurement.diastolic)
                MeasurementRow(
                    pressure = "${measurement.systolic}/${measurement.diastolic}",
                    date = SimpleDateFormat("d MMM, HH:mm", Locale.FRENCH).format(Date(measurement.date)),
                    pulse = "${measurement.pulse} bpm",
                    statusColor = status.color
                )
                if (index < measurements.size - 1) {
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                }
            }
        }
    }
}

@Composable
fun MeasurementRow(pressure: String, date: String, pulse: String, statusColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(statusColor))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = pressure, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(text = " mmHg", color = Color.LightGray, fontSize = 14.sp)
            }
            Text(text = date, color = Color.Gray, fontSize = 12.sp)
        }
        Text(text = pulse, color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

data class StatusInfo(val label: String, val color: Color, val bgColor: Color)

fun getStatus(systolic: Int, diastolic: Int): StatusInfo {
    return when {
        systolic > 180 || diastolic > 120 -> StatusInfo("Crise", StatusCrisis, StatusCrisisBg)
        systolic >= 140 || diastolic >= 90 -> StatusInfo("Hypert. Stade 2", StatusStage2, StatusStage2Bg)
        systolic >= 130 || diastolic >= 80 -> StatusInfo("Hypert. Stade 1", StatusStage1, StatusStage1Bg)
        systolic >= 120 -> StatusInfo("Élevée", StatusElevated, StatusElevatedBg)
        else -> StatusInfo("Normale", StatusNormal, StatusNormalBg)
    }
}

fun calculateAverage(measurements: List<Measurement>, days: Int): Pair<Int, Int> {
    val cutoff = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
    val filtered = measurements.filter { it.date >= cutoff }
    if (filtered.isEmpty()) return Pair(0, 0)
    val avgSys = filtered.map { it.systolic }.average().toInt()
    val avgDia = filtered.map { it.diastolic }.average().toInt()
    return Pair(avgSys, avgDia)
}
