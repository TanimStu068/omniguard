package com.tanim.omniguard.presentation.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tanim.omniguard.domain.model.AppCategory
import com.tanim.omniguard.domain.model.AppInfo
import com.tanim.omniguard.domain.model.PermissionInfo
import com.tanim.omniguard.domain.model.RiskLevel
import com.tanim.omniguard.presentation.viewmodel.AppDetailViewModel
import com.tanim.omniguard.utils.Constants
import com.tanim.omniguard.utils.FormatterUtils
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    navController: NavController,
    packageName: String,
    viewModel: AppDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Animated entrance
    var contentAlpha by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        delay(100)
        contentAlpha = 1f
    }

    LaunchedEffect(packageName) {
        viewModel.loadAppDetails(packageName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.appInfo?.appName?.take(20) ?: "App Details",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = Uri.parse("package:$packageName")
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val intent = Intent(Settings.ACTION_SETTINGS)
                            context.startActivity(intent)
                        }
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "App Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading app details...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                uiState.appInfo == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "App not found",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "The app may have been uninstalled",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { navController.navigateUp() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Go Back")
                            }
                        }
                    }
                }

                else -> {
                    val app = uiState.appInfo!!
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .animateContentSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // App Header Card
                        item {
                            AppHeaderCard(
                                appInfo = app,
                                alpha = contentAlpha
                            )
                        }

                        // Risk Assessment Card
                        item {
                            RiskAssessmentCard(
                                appInfo = app,
                                alpha = contentAlpha
                            )
                        }

                        // App Details Card
                        item {
                            AppDetailsCard(
                                appInfo = app,
                                usageTime = uiState.usageTimeMillis,
                                alpha = contentAlpha
                            )
                        }

                        // Permissions Section Header
                        item {
                            Column(
                                modifier = Modifier.graphicsLayer { alpha = contentAlpha }
                            ) {
                                Text(
                                    text = "🔐 Permissions",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                Text(
                                    text = "These are the permissions this app has access to",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        // Granted Permissions List
                        val grantedPermissions = app.permissions.filter { it.isGranted }

                        if (grantedPermissions.isNotEmpty()) {
                            items(grantedPermissions) { permission ->
                                PermissionCardItem(
                                    permission = permission,
                                    category = app.category,
                                    modifier = Modifier.graphicsLayer { alpha = contentAlpha }
                                )
                            }
                        } else {
                            item {
                                EmptyPermissionsCard(alpha = contentAlpha)
                            }
                        }

                        // Bottom spacing
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppHeaderCard(
    appInfo: AppInfo,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon Placeholder
            Surface(
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appInfo.appName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = appInfo.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Risk badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = getRiskColor(appInfo.riskLevel).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "⚠️ ${appInfo.riskLevel.name} RISK",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = getRiskColor(appInfo.riskLevel)
                        )
                    }

                    // Category badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        val categoryName = appInfo.category.name.lowercase().replace("_", " ")
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                        Text(
                            text = categoryName,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RiskAssessmentCard(
    appInfo: AppInfo,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    val riskColor = getRiskColor(appInfo.riskLevel)
    val riskFactors = getRiskFactors(appInfo)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha },
        colors = CardDefaults.cardColors(
            containerColor = riskColor.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = riskColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Risk Assessment",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = riskColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = getRiskDescription(appInfo.riskLevel),
                style = MaterialTheme.typography.bodyMedium
            )

            if (riskFactors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = riskColor.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))

                riskFactors.forEach { factor ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = riskColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = factor,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppDetailsCard(
    appInfo: AppInfo,
    usageTime: Long,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "📱 App Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            DetailRow(
                icon = Icons.Default.Info,
                label = "Version",
                value = if (appInfo.versionName.isNotEmpty()) appInfo.versionName else "Unknown"
            )
            DetailRow(
                icon = Icons.Default.DateRange,
                label = "Installed",
                value = formatDate(appInfo.firstInstallTime)
            )
            DetailRow(
                icon = Icons.Default.Refresh,
                label = "Last Updated",
                value = formatDate(appInfo.lastUpdateTime)
            )
            DetailRow(
                icon = Icons.Default.Storage,
                label = "App Size",
                value = FormatterUtils.formatBytes(appInfo.appSizeBytes)
            )
            DetailRow(
                icon = Icons.Default.Smartphone,
                label = "Type",
                value = if (appInfo.isSystemApp) "System App" else "User App"
            )
            DetailRow(
                icon = Icons.Default.List,
                label = "Category",
                value = appInfo.category.name.lowercase().replace("_", " ")
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            )

            // Usage time if available
            if (usageTime > 0) {
                DetailRow(
                    icon = Icons.Default.Timer,
                    label = "Usage Time (30 days)",
                    value = FormatterUtils.formatUsageTime(usageTime)
                )
            }

            // Shadow app warning
            if (appInfo.isShadowApp) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⚠️ This app has no launcher icon (Shadow App)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun PermissionCardItem(
    permission: PermissionInfo,
    category: AppCategory,
    modifier: Modifier = Modifier
) {
    val expectedPerms = Constants.EXPECTED_PERMISSIONS[category] ?: emptySet()
    val isUnrelated = permission.isDangerous && !expectedPerms.contains(permission.name)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnrelated)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Permission icon
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = if (isUnrelated)
                    MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                else
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getPermissionIcon(permission.name),
                        contentDescription = null,
                        tint = if (isUnrelated)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getPermissionDisplayName(permission.name),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${permission.group} • ${if (isUnrelated) "Unrelated" else "Safe"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isUnrelated) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Grant status
            if (permission.isGranted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Granted",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = "Denied",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyPermissionsCard(alpha: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Special Permissions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "This app doesn't request any dangerous permissions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ==================== Helper Functions ====================

private fun getPermissionIcon(permissionName: String): ImageVector {
    return when {
        permissionName.contains("CAMERA") -> Icons.Default.PhotoCamera
        permissionName.contains("RECORD_AUDIO") -> Icons.Default.Mic
        permissionName.contains("LOCATION") -> Icons.Default.LocationOn
        permissionName.contains("CONTACTS") -> Icons.Default.Contacts
        permissionName.contains("SMS") -> Icons.Default.Email
        else -> Icons.Default.Lock
    }
}

private fun getPermissionDisplayName(permissionName: String): String {
    return when {
        permissionName.contains("CAMERA") -> "Camera"
        permissionName.contains("RECORD_AUDIO") -> "Microphone"
        permissionName.contains("ACCESS_FINE_LOCATION") -> "Precise Location"
        permissionName.contains("ACCESS_COARSE_LOCATION") -> "Approximate Location"
        permissionName.contains("LOCATION") -> "Location"
        permissionName.contains("READ_CONTACTS") -> "Read Contacts"
        permissionName.contains("WRITE_CONTACTS") -> "Edit Contacts"
        permissionName.contains("CONTACTS") -> "Contacts"
        permissionName.contains("READ_SMS") -> "Read SMS"
        permissionName.contains("SEND_SMS") -> "Send SMS"
        else -> permissionName.substringAfterLast(".")
    }
}

private fun getRiskColor(riskLevel: RiskLevel): Color {
    return when (riskLevel) {
        RiskLevel.CRITICAL -> Color(0xFFD32F2F)
        RiskLevel.HIGH -> Color(0xFFFF5722)
        RiskLevel.MEDIUM -> Color(0xFFFFC107)
        RiskLevel.LOW -> Color(0xFF4CAF50)
        RiskLevel.UNKNOWN -> Color.Gray
    }
}

private fun getRiskDescription(riskLevel: RiskLevel): String {
    return when (riskLevel) {
        RiskLevel.CRITICAL ->
            "🔴 CRITICAL RISK: This app has access to sensitive permissions that are not required for its primary function."
        RiskLevel.HIGH ->
            "🟠 HIGH RISK: This app requests sensitive permissions that seem unrelated to its category."
        RiskLevel.MEDIUM ->
            "🟡 MEDIUM RISK: This app has some permissions that might not be necessary for its work."
        RiskLevel.LOW ->
            "🟢 LOW RISK: This app uses only expected permissions for its category."
        RiskLevel.UNKNOWN ->
            "⚪ UNKNOWN RISK: Insufficient data to assess risk level."
    }
}

private fun getRiskFactors(appInfo: AppInfo): List<String> {
    val factors = mutableListOf<String>()
    val expectedPerms = Constants.EXPECTED_PERMISSIONS[appInfo.category] ?: emptySet()

    appInfo.permissions.filter { it.isGranted && it.isDangerous }.forEach { perm ->
        if (!expectedPerms.contains(perm.name)) {
            factors.add("• Unrelated: ${getPermissionDisplayName(perm.name)}")
        }
    }

    if (appInfo.isShadowApp) factors.add("• Hidden app (no launcher icon)")

    return factors
}

private fun formatDate(timestamp: Long): String {
    if (timestamp <= 0L) return "Unknown"
    val date = Date(timestamp)
    val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return format.format(date)
}