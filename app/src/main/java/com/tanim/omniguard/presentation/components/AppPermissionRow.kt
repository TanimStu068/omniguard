package com.tanim.omniguard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tanim.omniguard.domain.model.AppInfo
import com.tanim.omniguard.domain.model.RiskLevel
import com.tanim.omniguard.utils.Constants

@Composable
fun AppPermissionRow(
    appInfo: AppInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appInfo.appName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = appInfo.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }

            // Permission icons
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val expectedPerms = Constants.EXPECTED_PERMISSIONS[appInfo.category] ?: emptySet()

                if (hasCameraPermission(appInfo)) {
                    PermissionIcon(
                        icon = Icons.Default.PhotoCamera,
                        description = "Camera",
                        isDangerous = !expectedPerms.contains("android.permission.CAMERA")
                    )
                }
                if (hasMicrophonePermission(appInfo)) {
                    PermissionIcon(
                        icon = Icons.Default.Mic,
                        description = "Microphone",
                        isDangerous = !expectedPerms.contains("android.permission.RECORD_AUDIO")
                    )
                }
                if (hasLocationPermission(appInfo)) {
                    PermissionIcon(
                        icon = Icons.Default.LocationOn,
                        description = "Location",
                        isDangerous = !expectedPerms.contains("android.permission.ACCESS_FINE_LOCATION") &&
                                !expectedPerms.contains("android.permission.ACCESS_COARSE_LOCATION")
                    )
                }
            }

            // Risk badge
            val riskColor = getRiskColor(appInfo.riskLevel)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = riskColor.copy(alpha = 0.2f),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = appInfo.riskLevel.name,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = riskColor
                )
            }
        }
    }
}

@Composable
fun PermissionIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    isDangerous: Boolean
) {
    Icon(
        imageVector = icon,
        contentDescription = description,
        modifier = Modifier.size(20.dp),
        tint = if (isDangerous) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    )
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

private fun hasCameraPermission(app: AppInfo): Boolean {
    return app.permissions.any { it.isGranted && it.name.contains("CAMERA") }
}

private fun hasMicrophonePermission(app: AppInfo): Boolean {
    return app.permissions.any { it.isGranted && it.name.contains("RECORD_AUDIO") }
}

private fun hasLocationPermission(app: AppInfo): Boolean {
    return app.permissions.any { it.isGranted && it.name.contains("LOCATION") }
}