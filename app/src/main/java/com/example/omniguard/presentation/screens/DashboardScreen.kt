package com.example.omniguard.presentation.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.omniguard.presentation.components.RiskAlertCard
import com.example.omniguard.presentation.components.SecurityScoreCard
import com.example.omniguard.presentation.viewmodel.DashboardViewModel
import com.example.omniguard.utils.NotificationHelper
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        null
    }

    // Storage permissions based on Android version
    val storagePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO
        )
    } else {
        listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    
    val storagePermissionState = rememberMultiplePermissionsState(permissions = storagePermissions)

    // Automatically request permissions on first launch
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (notificationPermissionState?.status?.isGranted == false) {
                notificationPermissionState.launchPermissionRequest()
            }
        }
        if (!storagePermissionState.allPermissionsGranted) {
            storagePermissionState.launchMultiplePermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "OMNIGUARD", 
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    ) 
                },
                actions = {
                    IconButton(onClick = { 
                        navController.navigate("scan") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Re-scan")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        DashboardContent(
            uiState = uiState,
            navController = navController,
            context = context,
            paddingValues = paddingValues,
            showNotificationPermissionCard = notificationPermissionState?.status?.isGranted == false,
            onRequestNotificationPermission = { notificationPermissionState?.launchPermissionRequest() }
        )
    }
}

@Composable
fun DashboardContent(
    uiState: com.example.omniguard.presentation.viewmodel.DashboardUiState,
    navController: NavController,
    context: android.content.Context,
    paddingValues: PaddingValues,
    showNotificationPermissionCard: Boolean,
    onRequestNotificationPermission: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 0. Notification Permission Card
        if (showNotificationPermissionCard) {
            item {
                NotificationPermissionCard(onEnableClick = onRequestNotificationPermission)
            }
        }

        // 1. Security Score Section
        item {
            uiState.securityScore?.let { score ->
                SecurityScoreCard(
                    score = score.score,
                    riskLevel = score.riskLevel,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 2. Security Metrics
        item {
            SectionHeader(title = "Security Metrics")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    "Total Apps", 
                    uiState.totalApps.toString(), 
                    Icons.Outlined.Apps,
                    MaterialTheme.colorScheme.primary, 
                    Modifier.weight(1f)
                )
                StatCard(
                    "High Risk", 
                    uiState.highRiskApps.toString(), 
                    Icons.Outlined.GppBad,
                    MaterialTheme.colorScheme.error, 
                    Modifier.weight(1f)
                )
            }
        }

        // 3. System Health Section
        item {
            SectionHeader(title = "System Health")
            SystemHealthCard(
                batteryPercentage = uiState.batteryPercentage,
                storageUsedPercentage = uiState.storageUsedPercentage
            )
        }

        // 4. Active Alerts Section
        if (uiState.recentAlerts.isNotEmpty()) {
            item {
                SectionHeader(title = "Security Alerts", count = uiState.recentAlerts.size)
            }
            items(uiState.recentAlerts) { alert ->
                RiskAlertCard(
                    alert = alert,
                    onClick = {
                        when (alert.action) {
                            "Fix Now", "Review" -> navController.navigate("sentinel")
                            "Clean Up" -> context.startActivity(Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS))
                            "Check Details" -> navController.navigate("performance")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 5. Quick Actions Section
        item {
            SectionHeader(title = "Quick Actions")
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuickActionCard(
                    "Permission Audit", 
                    Icons.Outlined.Shield, 
                    { navController.navigate("sentinel") }, 
                    Modifier.weight(1f)
                )
                QuickActionCard(
                    "Storage Check", 
                    Icons.Outlined.Storage, 
                    { navController.navigate("performance") }, 
                    Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun NotificationPermissionCard(onEnableClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Background Protection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    "Enable notifications to get real-time security alerts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onEnableClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Enable", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, count: Int? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (count != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = CircleShape
            ) {
                Text(
                    text = count.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
            )
        }
    }
}

@Composable
fun SystemHealthCard(batteryPercentage: Int, storageUsedPercentage: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HealthIndicator(
                label = "Battery",
                value = "$batteryPercentage%",
                progress = batteryPercentage / 100f,
                color = if (batteryPercentage > 20) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
            Divider(modifier = Modifier.height(40.dp).width(1.dp), color = MaterialTheme.colorScheme.onSurface.copy(0.1f))
            HealthIndicator(
                label = "Storage",
                value = "${storageUsedPercentage.toInt()}%",
                progress = storageUsedPercentage / 100f,
                color = if (storageUsedPercentage < 85) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun HealthIndicator(label: String, value: String, progress: Float, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = color,
            trackColor = color.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun QuickActionCard(title: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
        }
    }
}
