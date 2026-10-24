package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JervesAmber
import com.example.ui.theme.JervesCrimson
import com.example.ui.theme.JervesCyanGlow
import com.example.ui.theme.JervesCyanPrimary
import com.example.ui.theme.JervesNeonGreen
import com.example.ui.theme.JervesTextPrimary
import com.example.ui.theme.JervesTextSecondary
import com.example.ui.viewmodel.AssistantCoreState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorOrb(
    state: AssistantCoreState,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorOrb")

    // Slow continuous rotation for outer ring
    val outerAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerAngle"
    )

    // Counter-rotation for middle ring
    val midAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "midAngle"
    )

    // Breathing pulse scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AssistantCoreState.LISTENING -> 800
                    AssistantCoreState.PROCESSING -> 1000
                    AssistantCoreState.SPEAKING -> 600
                    AssistantCoreState.ERROR -> 1200
                    AssistantCoreState.STANDBY -> 2400
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val primaryColor = when (state) {
        AssistantCoreState.LISTENING -> JervesNeonGreen
        AssistantCoreState.PROCESSING -> JervesAmber
        AssistantCoreState.SPEAKING -> JervesCyanPrimary
        AssistantCoreState.ERROR -> JervesCrimson
        AssistantCoreState.STANDBY -> JervesCyanPrimary
    }

    val stateLabel = when (state) {
        AssistantCoreState.LISTENING -> "LISTENING..."
        AssistantCoreState.PROCESSING -> "PROCESSING"
        AssistantCoreState.SPEAKING -> "TRANSMITTING"
        AssistantCoreState.ERROR -> "SYSTEM ERROR"
        AssistantCoreState.STANDBY -> "JERVES // ONLINE"
    }

    val statusSub = when (state) {
        AssistantCoreState.LISTENING -> "Speak directive clearly"
        AssistantCoreState.PROCESSING -> "Synthesizing neural logic"
        AssistantCoreState.SPEAKING -> "Audio output active"
        AssistantCoreState.ERROR -> "Check telemetry logs"
        AssistantCoreState.STANDBY -> "Tap orb to initiate query"
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .testTag("arc_reactor_orb")
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val center = Offset(this.size.width / 2, this.size.height / 2)
                val baseRadius = (this.size.minDimension / 2) - 12.dp.toPx()

                // Radial ambient glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.28f * pulseScale),
                            primaryColor.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = baseRadius * 1.15f
                    ),
                    radius = baseRadius * 1.15f,
                    center = center
                )

                // Outer rotating segmented tech ring
                rotate(outerAngle, pivot = center) {
                    val segments = 8
                    val sweep = 360f / segments
                    for (i in 0 until segments) {
                        drawArc(
                            color = primaryColor.copy(alpha = if (i % 2 == 0) 0.85f else 0.35f),
                            startAngle = i * sweep,
                            sweepAngle = sweep * 0.65f,
                            useCenter = false,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                            size = androidx.compose.ui.geometry.Size(baseRadius * 2, baseRadius * 2),
                            topLeft = Offset(center.x - baseRadius, center.y - baseRadius)
                        )
                    }
                }

                // Middle counter-rotating dashed precision ring
                rotate(midAngle, pivot = center) {
                    val midRadius = baseRadius * 0.82f
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.6f),
                        radius = midRadius,
                        center = center,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                        )
                    )

                    // Small orbital node dots
                    for (i in 0 until 4) {
                        val angleRad = Math.toRadians((i * 90.0) + midAngle.toDouble())
                        val nx = center.x + (midRadius * cos(angleRad)).toFloat()
                        val ny = center.y + (midRadius * sin(angleRad)).toFloat()
                        drawCircle(
                            color = primaryColor,
                            radius = 3.5.dp.toPx(),
                            center = Offset(nx, ny)
                        )
                    }
                }

                // Inner breathing core
                val coreRadius = baseRadius * 0.58f * pulseScale
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.75f),
                            primaryColor.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = coreRadius
                    ),
                    radius = coreRadius,
                    center = center
                )

                // Sharp inner core border
                drawCircle(
                    color = primaryColor,
                    radius = coreRadius * 0.75f,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Center glowing dot
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx() * pulseScale,
                    center = center
                )
            }

            // Center overlay text label
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "AI CORE",
                    color = primaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = stateLabel,
            color = primaryColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = statusSub,
            color = JervesTextSecondary,
            fontSize = 12.sp,
            fontFamily = FontFamily.SansSerif
        )
    }
}
