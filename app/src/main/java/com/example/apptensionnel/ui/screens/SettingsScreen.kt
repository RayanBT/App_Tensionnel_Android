package com.example.apptensionnel.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.window.Dialog
import com.example.apptensionnel.data.NotificationHelper
import com.example.apptensionnel.data.PreferenceManager
import com.example.apptensionnel.data.ReportManager
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferenceManager: PreferenceManager,
    onNavigateToProfileSelection: () -> Unit = {},
    onThemeChanged: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val reportManager = remember { ReportManager(context) }
    val notificationHelper = remember { NotificationHelper(context) }
    
    // On utilise les couleurs du thème Material au lieu de hardcoder le bleu
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    // Permission pour Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                notificationHelper.sendTestNotification()
            }
        }
    )

    var showTimePicker by remember { mutableStateOf(false) }
    val currentTime = preferenceManager.reminderTime.split(":")
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.getOrNull(0)?.toInt() ?: 8,
        initialMinute = currentTime.getOrNull(1)?.toInt() ?: 0,
        is24Hour = true
    )

    if (showTimePicker) {
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Choisir l'heure du rappel",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Annuler")
                        }
                        TextButton(onClick = {
                            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", timePickerState.hour, timePickerState.minute)
                            preferenceManager.reminderTime = formattedTime
                            if (preferenceManager.isReminderEnabled) {
                                notificationHelper.scheduleDailyReminder(formattedTime)
                            }
                            showTimePicker = false
                        }) {
                            Text("Confirmer")
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(scrollState)
    ) {
        // --- SECTION HEADER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(primaryColor)
                .padding(top = 48.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
        ) {
            Column {
                Text(
                    text = "Paramètres",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Gérez vos préférences et données",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
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
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
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

            // --- SECTION APPARENCE ---
            SettingsSection(title = "APPARENCE", icon = Icons.Default.Palette) {
                var selectedTheme by remember { mutableIntStateOf(preferenceManager.themeMode) }
                
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Thème de l'application", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionCard(
                            title = "Auto",
                            icon = Icons.Default.BrightnessAuto,
                            isSelected = selectedTheme == 0,
                            modifier = Modifier.weight(1f),
                            onClick = { 
                                selectedTheme = 0
                                preferenceManager.themeMode = 0
                                onThemeChanged(0)
                            }
                        )
                        ThemeOptionCard(
                            title = "Clair",
                            icon = Icons.Default.LightMode,
                            isSelected = selectedTheme == 1,
                            modifier = Modifier.weight(1f),
                            onClick = { 
                                selectedTheme = 1
                                preferenceManager.themeMode = 1
                                onThemeChanged(1)
                            }
                        )
                        ThemeOptionCard(
                            title = "Sombre",
                            icon = Icons.Default.DarkMode,
                            isSelected = selectedTheme == 2,
                            modifier = Modifier.weight(1f),
                            onClick = { 
                                selectedTheme = 2
                                preferenceManager.themeMode = 2
                                onThemeChanged(2)
                            }
                        )
                    }
                }
            }

            // --- SECTION PROFIL ---
            SettingsSection(title = "PROFIL", icon = Icons.Default.Person) {
                val currentProfile = remember { preferenceManager.getCurrentProfile() }
                SettingsItem(
                    icon = Icons.Default.AccountCircle,
                    iconBackground = MaterialTheme.colorScheme.secondaryContainer,
                    iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    title = currentProfile?.name ?: "Utilisateur",
                    subtitle = "Profil actuellement actif",
                    trailing = {
                        TextButton(onClick = onNavigateToProfileSelection) {
                            Text("Changer", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // --- SAUVEGARDE AUTOMATIQUE ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                var backupEnabled by remember { mutableStateOf(preferenceManager.isBackupEnabled) }
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
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
                        }
                    )
                }
            }

            // --- SECTION RAPPELS & NOTIFICATIONS ---
            SettingsSection(title = "RAPPELS & NOTIFICATIONS", icon = Icons.Default.NotificationsNone) {
                var dailyReminder by remember { mutableStateOf(preferenceManager.isReminderEnabled) }
                SettingsItem(
                    icon = Icons.Default.NotificationsNone,
                    iconBackground = MaterialTheme.colorScheme.primaryContainer,
                    iconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    title = "Rappel quotidien",
                    subtitle = "Me rappeler de mesurer ma tension",
                    trailing = {
                        Switch(
                            checked = dailyReminder,
                            onCheckedChange = { 
                                dailyReminder = it
                                preferenceManager.isReminderEnabled = it
                                if (it) {
                                    notificationHelper.scheduleDailyReminder(preferenceManager.reminderTime)
                                } else {
                                    notificationHelper.cancelAllReminders()
                                }
                            }
                        )
                    }
                )
                
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(40.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
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
                        modifier = Modifier.fillMaxWidth().clickable { showTimePicker = true },
                        color = MaterialTheme.colorScheme.surfaceVariant,
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
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.LightGray)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

                SettingsItem(
                    icon = Icons.Default.NotificationAdd,
                    iconBackground = MaterialTheme.colorScheme.secondaryContainer,
                    iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    title = "Tester la notification",
                    subtitle = "Envoyer une notification immédiate",
                    showArrow = true,
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            notificationHelper.sendTestNotification()
                        }
                    }
                )
            }

            // --- SECTION DONNÉES & EXPORT ---
            SettingsSection(title = "DONNÉES & EXPORT", icon = Icons.Default.FileDownload) {
                SettingsItem(
                    icon = Icons.Default.PictureAsPdf,
                    iconBackground = MaterialTheme.colorScheme.primaryContainer,
                    iconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    title = "Exporter en PDF",
                    subtitle = "Télécharger votre rapport complet",
                    showArrow = true,
                    onClick = { reportManager.exportToPDF(preferenceManager.getMeasurements()) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                SettingsItem(
                    icon = Icons.Default.Description,
                    iconBackground = MaterialTheme.colorScheme.secondaryContainer,
                    iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    title = "Exporter en CSV",
                    subtitle = "Données brutes pour tableur",
                    showArrow = true,
                    onClick = { reportManager.exportToCSV(preferenceManager.getMeasurements()) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                SettingsItem(
                    icon = Icons.Default.Analytics,
                    iconBackground = MaterialTheme.colorScheme.tertiaryContainer,
                    iconColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    title = "Générer des données (DÉMO)",
                    subtitle = "Ajouter 30 jours de mesures fictives",
                    showArrow = true,
                    onClick = { preferenceManager.generateFakeData() }
                )
            }

            // --- SECTION À PROPOS ---
            SettingsSection(title = "À PROPOS", icon = Icons.Default.Info) {
                SettingsItem(
                    icon = Icons.Default.Info,
                    iconBackground = MaterialTheme.colorScheme.surfaceVariant,
                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
fun ThemeOptionCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                icon, 
                contentDescription = null, 
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
