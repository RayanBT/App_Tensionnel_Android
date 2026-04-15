package com.example.apptensionnel.ui.screens

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.example.apptensionnel.data.PreferenceManager
import com.example.apptensionnel.data.models.Measurement
import com.example.apptensionnel.ui.theme.StatusCrisis
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TrendsScreen(preferenceManager: PreferenceManager) {
    val allMeasurements = remember { preferenceManager.getMeasurements().reversed() }
    val scrollState = rememberScrollState()
    var selectedPeriod by remember { mutableStateOf("7 jours") }
    var selectedMeasurement by remember { mutableStateOf<Measurement?>(null) }

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

    if (selectedMeasurement != null) {
        MeasurementDetailDialog(
            measurement = selectedMeasurement!!,
            onDismiss = { selectedMeasurement = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(top = 48.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
        ) {
            Column {
                Text(text = "Tendances", color = MaterialTheme.colorScheme.onPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(text = "Évolution de votre tension artérielle", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), fontSize = 16.sp)
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    listOf("7 jours", "30 jours", "90 jours").forEach { period ->
                        val isSelected = selectedPeriod == period
                        Button(
                            onClick = { selectedPeriod = period },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            elevation = null
                        ) {
                            Text(text = period, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }

            ChartCard(
                title = "Tension Artérielle",
                subtitle = "Appuyez sur un point pour voir les détails"
            ) {
                if (filteredMeasurements.isEmpty()) {
                    EmptyStatePlaceholder()
                } else {
                    TensionChart(filteredMeasurements) { selectedMeasurement = it }
                }
            }

            ChartCard(
                title = "Fréquence cardiaque",
                subtitle = "Appuyez sur un point pour voir les détails"
            ) {
                if (filteredMeasurements.isEmpty()) {
                    EmptyStatePlaceholder()
                } else {
                    PulseChart(filteredMeasurements) { selectedMeasurement = it }
                }
            }

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

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "FRÉQUENCE CARDIAQUE MOYENNE", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = StatusCrisis.copy(alpha = 0.1f),
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
                                Text(text = " bpm", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
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
fun ChartCard(title: String, subtitle: String? = null, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
            if (subtitle != null) {
                Text(text = subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 12.sp)
            }
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = value, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(text = " $unit", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
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
        Text(text = "Aucune donnée disponible", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
    }
}

@Composable
fun TensionChart(measurements: List<Measurement>, onValueSelected: (Measurement) -> Unit) {
    val sysColor = MaterialTheme.colorScheme.primary.toArgb()
    val diaColor = StatusCrisis.toArgb()
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val gridColor = MaterialTheme.colorScheme.outlineVariant.toArgb()
    
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 450)
                description.isEnabled = false
                legend.isEnabled = true
                legend.textColor = textColor
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.textColor = textColor
                axisRight.isEnabled = false
                axisLeft.setDrawGridLines(true)
                axisLeft.gridColor = gridColor
                axisLeft.textColor = textColor
                setTouchEnabled(true)
                setScaleEnabled(false)
                
                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        e?.let {
                            val index = it.x.toInt()
                            if (index in measurements.indices) {
                                onValueSelected(measurements[index])
                            }
                        }
                    }
                    override fun onNothingSelected() {}
                })
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
                highLightColor = sysColor
                setDrawHighlightIndicators(true)
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
                highLightColor = diaColor
                setDrawHighlightIndicators(true)
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
fun PulseChart(measurements: List<Measurement>, onValueSelected: (Measurement) -> Unit) {
    val pulseColor = Color(0xFFFB8C00).toArgb()
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val gridColor = MaterialTheme.colorScheme.outlineVariant.toArgb()
    
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 400)
                description.isEnabled = false
                legend.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.textColor = textColor
                axisRight.isEnabled = false
                axisLeft.setDrawGridLines(true)
                axisLeft.gridColor = gridColor
                axisLeft.textColor = textColor
                setTouchEnabled(true)
                
                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        e?.let {
                            val index = it.x.toInt()
                            if (index in measurements.indices) {
                                onValueSelected(measurements[index])
                            }
                        }
                    }
                    override fun onNothingSelected() {}
                })
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
                highLightColor = pulseColor
                setDrawHighlightIndicators(true)
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

@Composable
fun MeasurementDetailDialog(measurement: Measurement, onDismiss: () -> Unit) {
    val status = getStatus(measurement.systolic, measurement.diastolic)
    val sdfDate = SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH)
    val sdfTime = SimpleDateFormat("HH:mm", Locale.FRENCH)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Détails de la mesure",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(status.bgColor)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = status.label, color = status.color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DetailItem(
                        label = "SYSTOLIQUE",
                        value = "${measurement.systolic}",
                        unit = "mmHg",
                        color = MaterialTheme.colorScheme.primary
                    )
                    Box(modifier = Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.outlineVariant))
                    DetailItem(
                        label = "DIASTOLIQUE",
                        value = "${measurement.diastolic}",
                        unit = "mmHg",
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                DetailInfoRow(Icons.Default.Favorite, "Pouls", "${measurement.pulse} bpm", StatusCrisis)
                DetailInfoRow(Icons.Default.CalendarMonth, "Date", sdfDate.format(Date(measurement.date)).replaceFirstChar { it.uppercase() })
                DetailInfoRow(Icons.Default.AccessTime, "Heure", sdfTime.format(Date(measurement.date)))
                
                if (measurement.notes.isNotEmpty()) {
                    DetailInfoRow(Icons.Default.Notes, "Notes", measurement.notes)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Fermer")
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, unit: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = value, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = " $unit", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
        }
    }
}

@Composable
fun DetailInfoRow(icon: ImageVector, label: String, value: String, iconColor: Color = MaterialTheme.colorScheme.primary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = "$label : ", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
