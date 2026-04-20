package com.tanim.omniguard.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.tanim.omniguard.presentation.components.SentinelLoadingAnimation
import com.tanim.omniguard.presentation.viewmodel.DashboardViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlin.math.cos
import kotlin.math.sin

enum class ScanPhase {
    SCANNING,
    ANALYZING,
    LOADING,
    COMPLETED
}

@Composable
fun ScanScreen(
    navController: NavController,
    dashboardViewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()

    var currentLoadingMessage by remember { mutableStateOf("Initializing security modules...") }
    var loadingProgress by remember { mutableFloatStateOf(0f) }
    var currentPhase by remember { mutableStateOf(ScanPhase.LOADING) }

    LaunchedEffect(Unit) {
        val steps = listOf(
            Triple("Initializing core security modules...", 0.1f, ScanPhase.LOADING),
            Triple("Scanning installed applications for threats...", 0.3f, ScanPhase.SCANNING),
            Triple("Analyzing system permission requests...", 0.5f, ScanPhase.SCANNING),
            Triple("Performing deep heuristic scanning...", 0.7f, ScanPhase.SCANNING),
            Triple("Finalizing security health report...", 0.9f, ScanPhase.LOADING)
        )

        // 1. Show professional scanning animations first
        for ((message, progress, phase) in steps) {
            currentLoadingMessage = message
            loadingProgress = progress
            currentPhase = phase
            delay(4000) // Much slower transition between phases (4 seconds each)
        }

        // 2. Now check if the Dashboard data is ready
        currentLoadingMessage = "Syncing final security data..."
        currentPhase = ScanPhase.LOADING

        // Wait until Dashboard data is actually loaded (isLoading is false and apps is not empty)
        snapshotFlow { uiState }
            .filter { !it.isLoading && it.apps.isNotEmpty() }
            .first()

        // 3. Once data is ready, show completion state
        currentLoadingMessage = "Security integrity check complete."
        loadingProgress = 1.0f
        currentPhase = ScanPhase.COMPLETED

        // 4. Navigate to Dashboard after a 1 second delay as requested
        delay(1000)
        navController.navigate("dashboard") {
            popUpTo("scan") { inclusive = true }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_radar")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing), // Slower pulse (2 seconds)
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(300.dp)) {

                // Animated transitions between different scanning visual phases
                AnimatedContent(
                    targetState = currentPhase,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(1500)) togetherWith
                                fadeOut(animationSpec = tween(1500)) // Slower crossfade (1.5 seconds)
                    },
                    label = "phase_transition"
                ) { phase ->
                    when (phase) {
                        ScanPhase.SCANNING -> RadarScanner(modifier = Modifier.size(260.dp))
                        ScanPhase.ANALYZING -> SentinelLoadingAnimation(
                            modifier = Modifier.size(260.dp),
                            label = null,
                            showShield = false
                        )
                        ScanPhase.LOADING -> CircularSecurityProgress(modifier = Modifier.size(260.dp))
                        ScanPhase.COMPLETED -> SuccessAnimation(modifier = Modifier.size(260.dp))
                    }
                }

                // Central Protection Shield
                Surface(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(pulseScale),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                    shadowElevation = 12.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))

            // Professional Typewriter Text
            TypewriterText(
                text = currentLoadingMessage,
                modifier = Modifier.padding(horizontal = 40.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Percentage indicator
            Text(
                text = "${(loadingProgress * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { loadingProgress },
                modifier = Modifier
                    .width(260.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun RadarScanner(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing), // Slower rotation (5 seconds)
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearOutSlowInEasing), // Slower expansion pulse (4 seconds)
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    val color = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2

        // Circular Pulses
        drawCircle(
            color = color.copy(alpha = (1f - pulse) * 0.5f),
            radius = radius * pulse,
            style = Stroke(width = 2.dp.toPx())
        )

        // Static Radar Rings
        for (i in 1..4) {
            drawCircle(
                color = color.copy(alpha = 0.1f),
                radius = radius * (i / 4f),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Sweeping Radar Beam
        rotate(rotation) {
            val sweepGradient = Brush.sweepGradient(
                0.75f to Color.Transparent,
                0.85f to color.copy(alpha = 0.1f),
                0.95f to color.copy(alpha = 0.4f),
                1.0f to color,
                center = center
            )
            drawArc(
                brush = sweepGradient,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = true,
                style = Fill
            )
        }
    }
}

@Composable
fun CircularSecurityProgress(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "security_progress")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing), // Slowed down from 2000 to 4000
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 30f,
        targetValue = 280f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000 // Slowed down from 2000 to 4000
                30f at 0 with FastOutSlowInEasing
                280f at 2000 with FastOutSlowInEasing
                30f at 4000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    val color = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val strokeWidth = 14.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2

        // 1. Thick Background Track
        drawCircle(
            color = color.copy(alpha = 0.08f),
            radius = radius,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // 2. Animated Thick Progress Arc
        rotate(rotation) {
            drawArc(
                color = color,
                startAngle = 0f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(center.x - radius, center.y - radius)
            )
        }

        // 3. Inner Pulsing Tech Ring (Dashed)
        rotate(-rotation * 0.5f) {
            drawCircle(
                color = color.copy(alpha = 0.2f),
                radius = radius - 20.dp.toPx(),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )
        }

        // 4. Moving "Data Bits"
        for (i in 0 until 4) {
            rotate(rotation * 1.2f + (i * 90f)) {
                drawCircle(
                    color = color.copy(alpha = 0.7f),
                    radius = 4.dp.toPx(),
                    center = Offset(center.x + radius, center.y)
                )
            }
        }
    }
}

@Composable
fun SuccessAnimation(modifier: Modifier = Modifier) {
    val color = Color(0xFF4CAF50) // Material Green
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2
        drawCircle(
            color = color.copy(alpha = 0.15f),
            radius = radius
        )
        drawCircle(
            color = color,
            radius = radius,
            style = Stroke(width = 6.dp.toPx())
        )
    }
}

@Composable
fun TypewriterText(text: String, modifier: Modifier = Modifier) {
    var visibleText by remember { mutableStateOf("") }

    LaunchedEffect(text) {
        visibleText = ""
        text.forEach { char ->
            visibleText += char
            delay(75) // Slower typewriter effect (75ms per char)
        }
    }

    Text(
        text = visibleText,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.fillMaxWidth(),
        minLines = 2
    )
}