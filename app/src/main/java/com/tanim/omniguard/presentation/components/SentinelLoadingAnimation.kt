package com.tanim.omniguard.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SentinelLoadingAnimation(
    modifier: Modifier = Modifier,
    label: String? = "AUDITING APP PERMISSIONS",
    showShield: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sentinel_scan")

    // Core pulsing scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Orbit rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Vertical scan line
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_line"
    )

    val color = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {

            // 1. Orbital Background
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2
                drawCircle(
                    color = color.copy(alpha = 0.05f),
                    radius = radius,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                )

                // Vertical Scan Line Effect
                val lineY = size.height * scanLineY
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, color.copy(alpha = 0.4f), Color.Transparent)
                    ),
                    start = Offset(0f, lineY),
                    end = Offset(size.width, lineY),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // 2. Rotating Permission Icons
            val icons = listOf(
                Icons.Default.PhotoCamera,
                Icons.Outlined.LocationOn,
                Icons.Default.Mic,
                Icons.Default.Fingerprint
            )

            icons.forEachIndexed { index, icon ->
                val angle = Math.toRadians((rotation + (index * 90)).toDouble())
                val orbitRadius = 100.dp

                Box(
                    modifier = Modifier
                        .offset(
                            x = (orbitRadius.value * cos(angle)).dp,
                            y = (orbitRadius.value * sin(angle)).dp
                        )
                        .alpha(0.6f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = color
                    )
                }
            }

            // 3. Central Sentinel Shield
            if (showShield) {
                Surface(
                    modifier = Modifier
                        .size(90.dp)
                        .scale(pulseScale),
                    shape = CircleShape,
                    color = color.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(2.dp, color.copy(alpha = 0.3f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(44.dp),
                            tint = color
                        )
                    }
                }
            }
        }

        if (label != null) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}