package com.example.apptensionnel.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apptensionnel.ui.theme.*

@Composable
fun HomeScreen() {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FA))) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bleu
            item {
                HeaderSection()
            }

            // Carte principale de la dernière mesure (qui chevauche un peu le bleu)
            item {
                LastMeasurementCard()
            }

            // Résumé des moyennes (7j et 30j)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        label = "Moy. 7 jours",
                        value = "130",
                        subtitle = "/ 81 mmHg",
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        label = "Moy. 30 jours",
                        value = "126",
                        subtitle = "/ 83 mmHg",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Guide de Référence
            item {
                ReferenceGuideCard(modifier = Modifier.padding(horizontal = 16.dp))
            }

            // Mesures récentes
            item {
                RecentMeasurementsCard(modifier = Modifier.padding(horizontal = 16.dp))
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }

        // Bouton "+" (Saisie)
        FloatingActionButton(
            onClick = { /* Action saisie */ },
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
            verticalAlignment = Alignment.CenterVertically
        ) {
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
fun LastMeasurementCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .offset(y = (-40).dp), // Chevauchement
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Badge d'état
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(StatusStage1Bg)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(StatusStage1))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Hypertension Stade 1", color = StatusStage1, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Dernière mesure • 9 avril 2026 à 19:55", color = Color.Gray, fontSize = 13.sp)
            Text(text = "SYSTOLIQUE / DIASTOLIQUE", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "133", fontSize = 56.sp, fontWeight = FontWeight.Bold, color = AppBlue)
                Text(text = " / ", fontSize = 40.sp, color = Color.LightGray)
                Text(text = "76", fontSize = 56.sp, fontWeight = FontWeight.Bold, color = AppBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "mmHg", color = Color.Gray, fontSize = 18.sp)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                TrendItem("Sys +43", StatusStage2)
                Spacer(modifier = Modifier.width(16.dp))
                TrendItem("Dia +6", StatusStage2)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "vs précédente", color = Color.LightGray, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Box Fréquence Cardiaque
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8F9FB))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Fréquence cardiaque", color = Color.Gray, fontSize = 12.sp)
                        Text(text = "61 bpm", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun TrendItem(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
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
fun RecentMeasurementsCard(modifier: Modifier = Modifier) {
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
            
            MeasurementRow("133/76", "9 avr., 19:55", "61 bpm", StatusStage1)
            HorizontalDivider(color = Color(0xFFF0F0F0))
            MeasurementRow("90/70", "9 avr., 09:19", "90 bpm", StatusNormal)
            HorizontalDivider(color = Color(0xFFF0F0F0))
            MeasurementRow("134/77", "9 avr., 07:42", "79 bpm", StatusStage1)
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
