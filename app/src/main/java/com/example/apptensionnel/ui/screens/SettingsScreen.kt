package com.example.apptensionnel.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apptensionnel.data.PreferenceManager
import com.example.apptensionnel.data.ReportManager
import com.example.apptensionnel.ui.theme.AppBlue

@Composable
fun SettingsScreen(preferenceManager: PreferenceManager) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val reportManager = remember { ReportManager(context) }
    val primaryBlue = AppBlue
    val backgroundColor = Color(0xFFF5F7FA)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(scrollState)
    ) {
        // --- SECTION HEADER BLEU ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(primaryBlue)
                .padding(top = 48.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
        ) {
            Column {
                Text(
                    text = "Paramètres",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Gérez vos préférences et données",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp
                )
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- BOUTON ENVOYER MÉDECIN ---
            Button(
                onClick = { reportManager.exportToPDF(preferenceManager.getMeasurements()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Envoyer à mon Médecin",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // --- SAUVEGARDE AUTOMATIQUE ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                var backupEnabled by remember { mutableStateOf(preferenceManager.isBackupEnabled) }
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF3E5F5),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Shield, contentDescription = null, tint = Color(0xFF7B1FA2))
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Sauvegarde automatique", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "Activée — Dernière sauvegarde aujourd'hui", color = Color.Gray, fontSize = 13.sp)
                    }
                    Switch(
                        checked = backupEnabled,
                        onCheckedChange = { 
                            backupEnabled = it
                            preferenceManager.isBackupEnabled = it
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = primaryBlue)
                    )
                }
            }

            // --- SECTION RAPPELS & NOTIFICATIONS ---
            SettingsSection(title = "RAPPELS & NOTIFICATIONS", icon = Icons.Default.NotificationsNone) {
                var dailyReminder by remember { mutableStateOf(preferenceManager.isReminderEnabled) }
                SettingsItem(
                    icon = Icons.Default.NotificationsNone,
                    iconBackground = Color(0xFFE3F2FD),
                    iconColor = primaryBlue,
                    title = "Rappel quotidien",
                    subtitle = "Me rappeler de mesurer ma tension",
                    trailing = {
                        Switch(
                            checked = dailyReminder,
                            onCheckedChange = { 
                                dailyReminder = it
                                preferenceManager.isReminderEnabled = it
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = primaryBlue)
                        )
                    }
                )
                
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFEEEEEE))

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = Color(0xFFE3F2FD), modifier = Modifier.size(40.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = primaryBlue, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = "Heure du rappel", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "Choisissez l'heure idéale", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { /* TODO: TimePicker */ },
                        color = Color(0xFFF1F4F8),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = preferenceManager.reminderTime,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryBlue
                            )
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.LightGray)
                        }
                    }
                }
            }

            // --- SECTION DONNÉES & EXPORT ---
            SettingsSection(title = "DONNÉES & EXPORT", icon = Icons.Default.FileDownload) {
                SettingsItem(
                    icon = Icons.Default.PictureAsPdf,
                    iconBackground = Color(0xFFE3F2FD),
                    iconColor = primaryBlue,
                    title = "Exporter en PDF",
                    subtitle = "Télécharger votre rapport complet",
                    showArrow = true,
                    onClick = { reportManager.exportToPDF(preferenceManager.getMeasurements()) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFEEEEEE))
                SettingsItem(
                    icon = Icons.Default.Description,
                    iconBackground = Color(0xFFE8F5E9),
                    iconColor = Color(0xFF43A047),
                    title = "Exporter en CSV",
                    subtitle = "Données brutes pour tableur",
                    showArrow = true,
                    onClick = { reportManager.exportToCSV(preferenceManager.getMeasurements()) }
                )
            }

            // --- SECTION À PROPOS ---
            SettingsSection(title = "À PROPOS", icon = Icons.Default.Info) {
                SettingsItem(
                    icon = Icons.Default.Info,
                    iconBackground = Color(0xFFF5F5F5),
                    iconColor = Color.Gray,
                    title = "TensioCare v2.1.0",
                    subtitle = "Application de suivi tensionnel"
                )
                
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cette application est un outil de suivi personnel. Elle ne remplace pas l'avis médical. Consultez votre médecin pour toute décision thérapeutique.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    iconBackground: Color,
    iconColor: Color,
    title: String,
    subtitle: String,
    trailing: @Composable (() -> Unit)? = null,
    showArrow: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = iconBackground,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = subtitle, color = Color.Gray, fontSize = 13.sp)
        }
        if (trailing != null) {
            trailing()
        } else if (showArrow) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}
