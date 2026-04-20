package com.example.omniguard.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.omniguard.presentation.viewmodel.Alert
import com.example.omniguard.presentation.viewmodel.AlertType

@Composable
fun RiskAlertCard(
    alert: Alert,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alertColor = when (alert.type) {
        AlertType.CRITICAL -> MaterialTheme.colorScheme.error
        AlertType.WARNING -> Color(0xFFFFA000) // Using Amber for warning
        AlertType.INFO -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = alertColor.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Alert icon
            Icon(
                imageVector = when (alert.type) {
                    AlertType.CRITICAL -> Icons.Default.Warning
                    AlertType.WARNING -> Icons.Default.PriorityHigh
                    AlertType.INFO -> Icons.Default.Info
                },
                contentDescription = null,
                tint = alertColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = alertColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onClick,
                    modifier = Modifier.offset(x = (-8).dp)
                ) {
                    Text(
                        text = alert.action,
                        style = MaterialTheme.typography.labelMedium,
                        color = alertColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
