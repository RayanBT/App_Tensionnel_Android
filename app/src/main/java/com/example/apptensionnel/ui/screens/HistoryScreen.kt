package com.example.apptensionnel.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
    var statusFilter by remember { mutableStateOf<String?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }

    val filteredMeasurements = remember(measurements, searchQuery, statusFilter) {
        measurements.filter { measurement ->
            val matchesSearch = if (searchQuery.isEmpty()) true
            else {
                measurement.notes.contains(searchQuery, ignoreCase = true) || 
                "${measurement.systolic}/${measurement.diastolic}".contains(searchQuery)
            }
            
            val matchesStatus = if (statusFilter == null) true
            else {
                getStatus(measurement.systolic, measurement.diastolic).label == statusFilter
            }
            
            matchesSearch && matchesStatus
        }
    }

    val groupedMeasurements = filteredMeasurements.groupBy { 
        SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH).format(Date(it.date)).uppercase()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- HEADER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(top = 48.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Historique", color = MaterialTheme.colorScheme.onPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (statusFilter == null) "${measurements.size} mesures enregistrées" 
                                   else "${filteredMeasurements.size} résultats pour \"$statusFilter\"", 
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), 
                            fontSize = 14.sp
                        )
                    }
                    Box {
                        IconButton(
                            onClick = { showFilterMenu = true },
                            modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                if (statusFilter == null) Icons.Default.FilterList else Icons.Default.FilterListOff, 
                                contentDescription = null, 
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Toutes les mesures") },
                                onClick = { statusFilter = null; showFilterMenu = false },
                                leadingIcon = { Icon(Icons.Default.AllInclusive, contentDescription = null) }
                            )
                            HorizontalDivider()
                            listOf("Normale", "Élevée", "Hypert. Stade 1", "Hypert. Stade 2", "Crise").forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status) },
                                    onClick = { statusFilter = status; showFilterMenu = false }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))

                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    placeholder = { Text("Rechercher une mesure...", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // --- INSTRUCTION DISCRÈTE ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.TouchApp, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Appui long pour modifier • Balayez pour supprimer",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // --- LISTE ---
        if (filteredMeasurements.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Aucune mesure trouvée", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (statusFilter != null || searchQuery.isNotEmpty()) {
                        TextButton(onClick = { statusFilter = null; searchQuery = "" }) {
                            Text("Réinitialiser les filtres")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                groupedMeasurements.forEach { (date, items) ->
                    item {
                        Text(
                            text = date,
                            modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(items, key = { it.id }) { measurement ->
                        SwipeToDeleteItem(
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteItem(
    measurement: Measurement,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { targetValue ->
            if (targetValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val isDismissing = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
            val color by animateColorAsState(
                if (isDismissing) StatusCrisis else Color.Transparent,
                label = "bg_color"
            )
            val scale by animateFloatAsState(
                if (isDismissing) 1.2f else 0.8f,
                label = "icon_scale"
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(color),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Supprimer",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(end = 24.dp)
                        .scale(scale)
                )
            }
        }
    ) {
        HistoryItemCard(
            measurement = measurement,
            onEditRequest = onEdit,
            onDismissRequest = {
                scope.launch { dismissState.reset() }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryItemCard(
    measurement: Measurement, 
    onEditRequest: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val status = getStatus(measurement.systolic, measurement.diastolic)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .combinedClickable(
                onClick = { onDismissRequest() },
                onLongClick = { onEditRequest() }
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(status.color))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.FRENCH).format(Date(measurement.date)),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = "${measurement.systolic}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(text = " / ", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    Text(text = "${measurement.diastolic}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "mmHg", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = StatusCrisis, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${measurement.pulse} bpm", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
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
                    Text(text = "\"${measurement.notes}\"", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.EditNote,
                    contentDescription = "Éditer (Appui long)",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                    modifier = Modifier.size(28.dp)
                )
                Text("ÉDITER", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
            }
        }
    }
}
