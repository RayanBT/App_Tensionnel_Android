package com.example.apptensionnel.ui.screens

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.apptensionnel.data.PreferenceManager
import com.example.apptensionnel.data.models.Measurement
import com.example.apptensionnel.ui.theme.AppBlue
import com.example.apptensionnel.ui.theme.StatusCrisis
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TrendsScreen(preferenceManager: PreferenceManager) {
    // On récupère toutes les mesures une fois (ordre chronologique pour le graph)
    val allMeasurements = remember { preferenceManager.getMeasurements().reversed() }
    val scrollState = rememberScrollState()
    var selectedPeriod by remember { mutableStateOf("7 jours") }

    // Filtrage dynamique selon la période choisie
    val filteredMeasurements = remember(allMeasurements, selectedPeriod) {
        val days = when (selectedPeriod) {
            "7 jours" -> 7
            "30 jours" -> 30
            "90 jours" -> 90
            else -> 7
        }
        val cutoff = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        allMeasurements.filter { it.date >= cutoff }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .verticalScroll(scrollState)
    ) {
        // Header (Bleu)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppBlue)
                .padding(top = 48.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
        ) {
            Column {
                Text(text = "Tendances", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(text = "Évolution de votre tension artérielle", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sélecteur de Période
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    listOf("7 jours", "30 jours", "90 jours").forEach { period ->
                        val isSelected = selectedPeriod == period
                        Button(
                            onClick = { selectedPeriod = period },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) AppBlue else Color.Transparent,
                                contentColor = if (isSelected) Color.White else Color.Gray
                            ),
                            elevation = null
                        ) {
                            Text(text = period, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }

            // Graphique Tension
            ChartCard(title = "Tension Artérielle") {
                if (filteredMeasurements.isEmpty()) {
                    EmptyStatePlaceholder()
                } else {
                    TensionChart(filteredMeasurements)
                }
            }

            // Graphique Pouls
            ChartCard(title = "Fréquence cardiaque") {
                if (filteredMeasurements.isEmpty()) {
                    EmptyStatePlaceholder()
                } else {
                    PulseChart(filteredMeasurements)
                }
            }

            // Moyennes Systolique/Diastolique
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    title = "MOY. SYSTOLIQUE",
                    value = if (filteredMeasurements.isEmpty()) "—" else filteredMeasurements.map { it.systolic }.average().toInt().toString(),
                    unit = "mmHg",
                    trend = "-2 sur la période",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "MOY. DIASTOLIQUE",
                    value = if (filteredMeasurements.isEmpty()) "—" else filteredMeasurements.map { it.diastolic }.average().toInt().toString(),
                    unit = "mmHg",
                    trend = "-1 sur la période",
                    modifier = Modifier.weight(1f)
                )
            }

            // Fréquence Cardiaque Moyenne
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "FRÉQUENCE CARDIAQUE MOYENNE", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFEBEE),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = StatusCrisis, modifier = Modifier.size(28.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = if (filteredMeasurements.isEmpty()) "—" else filteredMeasurements.map { it.pulse }.average().toInt().toString(),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusCrisis
                                )
                                Text(text = " bpm", color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF43A047), modifier = Modifier.size(16.dp))
                                Text(text = " Normale", color = Color(0xFF43A047), fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ChartCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
fun StatCard(title: String, value: String, unit: String, trend: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = value, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = AppBlue)
                Text(text = " $unit", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null, tint = Color(0xFF43A047), modifier = Modifier.size(14.dp))
                Text(text = " $trend", color = Color(0xFF43A047), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun EmptyStatePlaceholder() {
    Box(
        modifier = Modifier.fillMaxWidth().height(150.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Aucune donnée disponible", color = Color.LightGray)
    }
}

@Composable
fun TensionChart(measurements: List<Measurement>) {
    val sysColor = AppBlue.toArgb()
    val diaColor = StatusCrisis.toArgb()
    
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 450)
                description.isEnabled = false
                legend.isEnabled = true
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                axisRight.isEnabled = false
                axisLeft.setDrawGridLines(true)
                axisLeft.gridColor = Color.LightGray.copy(alpha = 0.3f).toArgb()
                setTouchEnabled(true)
                setScaleEnabled(false)
            }
        },
        update = { chart ->
            if (measurements.isEmpty()) return@AndroidView

            val sysEntries = measurements.mapIndexed { index, m -> Entry(index.toFloat(), m.systolic.toFloat()) }
            val diaEntries = measurements.mapIndexed { index, m -> Entry(index.toFloat(), m.diastolic.toFloat()) }
            
            val sysDataSet = LineDataSet(sysEntries, "Systolique").apply {
                color = sysColor
                setCircleColor(sysColor)
                lineWidth = 2.5f
                circleRadius = 4f
                setDrawCircleHole(true)
                setDrawFilled(false)
                valueTextSize = 0f
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }
            
            val diaDataSet = LineDataSet(diaEntries, "Diastolique").apply {
                color = diaColor
                setCircleColor(diaColor)
                lineWidth = 2.5f
                circleRadius = 4f
                setDrawCircleHole(true)
                setDrawFilled(false)
                valueTextSize = 0f
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }
            
            chart.data = LineData(sysDataSet, diaDataSet)
            
            val labels = measurements.map { SimpleDateFormat("dd/MM", Locale.FRENCH).format(Date(it.date)) }
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            chart.xAxis.labelCount = if (measurements.size > 5) 5 else measurements.size
            
            chart.notifyDataSetChanged()
            chart.invalidate()
        },
        modifier = Modifier.fillMaxWidth().height(220.dp)
    )
}

@Composable
fun PulseChart(measurements: List<Measurement>) {
    val pulseColor = Color(0xFFFB8C00).toArgb()
    
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 400)
                description.isEnabled = false
                legend.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                axisRight.isEnabled = false
                axisLeft.setDrawGridLines(true)
                axisLeft.gridColor = Color.LightGray.copy(alpha = 0.3f).toArgb()
                setTouchEnabled(true)
            }
        },
        update = { chart ->
            if (measurements.isEmpty()) return@AndroidView

            val entries = measurements.mapIndexed { index, m -> Entry(index.toFloat(), m.pulse.toFloat()) }
            val dataSet = LineDataSet(entries, "Pouls").apply {
                color = pulseColor
                setCircleColor(pulseColor)
                lineWidth = 2.5f
                circleRadius = 4f
                setDrawValues(false)
                setDrawFilled(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }
            
            chart.data = LineData(dataSet)
            
            val labels = measurements.map { SimpleDateFormat("dd/MM", Locale.FRENCH).format(Date(it.date)) }
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            chart.xAxis.labelCount = if (measurements.size > 5) 5 else measurements.size

            chart.notifyDataSetChanged()
            chart.invalidate()
        },
        modifier = Modifier.fillMaxWidth().height(180.dp)
    )
}
