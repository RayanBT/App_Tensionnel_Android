package com.example.apptensionnel.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    preferenceManager: PreferenceManager,
    onEdit: (Measurement) -> Unit
) {
    var measurements by remember { mutableStateOf(preferenceManager.getMeasurements()) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredMeasurements = remember(measurements, searchQuery) {
        if (searchQuery.isEmpty()) measurements
        else measurements.filter { 
            it.notes.contains(searchQuery, ignoreCase = true) || 
            "${it.systolic}/${it.diastolic}".contains(searchQuery)
        }
    }

    val groupedMeasurements = filteredMeasurements.groupBy { 
        SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH).format(Date(it.date)).uppercase()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppBlue)
                .padding(top = 48.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Historique", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text(text = "${measurements.size} mesures enregistrées", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                    }
                    IconButton(
                        onClick = { /* Filtrer */ },
                        modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = null, tint = Color.White)
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))

                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    placeholder = { Text("Rechercher une mesure...", color = Color.White.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.6f)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.15f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.15f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            groupedMeasurements.forEach { (date, items) ->
                item {
                    Text(
                        text = date,
                        modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp),
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(items, key = { it.id }) { measurement ->
                    StickySwipeItem(
                        measurement = measurement,
                        onDelete = {
                            preferenceManager.deleteMeasurementById(measurement.id)
                            measurements = preferenceManager.getMeasurements()
                        },
                        onEdit = { onEdit(measurement) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickySwipeItem(
    measurement: Measurement,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState()
    val scope = rememberCoroutineScope()

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { scope.launch { dismissState.reset() } },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(4.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.Black, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppBlue)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editer", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(StatusCrisis)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color.White)
                    }
                }
            }
        }
    ) {
        HistoryItemCard(
            measurement = measurement,
            onClick = {
                if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                    scope.launch { dismissState.reset() }
                }
            }
        )
    }
}

@Composable
fun HistoryItemCard(measurement: Measurement, onClick: () -> Unit = {}) {
    val status = getStatus(measurement.systolic, measurement.diastolic)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(status.color))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.FRENCH).format(Date(measurement.date)),
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = "${measurement.systolic}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AppBlue)
                    Text(text = " / ", fontSize = 20.sp, color = Color.LightGray)
                    Text(text = "${measurement.diastolic}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AppBlue)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "mmHg", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = StatusCrisis, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${measurement.pulse} bpm", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(status.bgColor)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = status.label, color = status.color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                if (measurement.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "\"${measurement.notes}\"", color = Color.Gray, fontSize = 13.sp)
                }
            }
            
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFFE0E0E0),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
