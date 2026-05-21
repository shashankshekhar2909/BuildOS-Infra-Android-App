package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ThemePrimary
import com.example.ui.theme.ThemeSecondary

@Composable
fun CyberLoader(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    strokeWidth: Dp = 3.dp,
    color1: Color = ThemePrimary,
    color2: Color = ThemeSecondary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cyber_loader")
    
    // Smooth angle rotating infinitely 
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Breathing pulse scale for the satellite orbit element
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Outer cyber rotating segment arc
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = color1,
                startAngle = rotation,
                sweepAngle = 260f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            // Dynamic satellite indicator riding the curve of the outer arc
            val radius = (size.toPx() - strokeWidth.toPx()) / 2f
            val angleRad = Math.toRadians(rotation.toDouble() + 260.0)
            val dx = center.x + radius * Math.cos(angleRad).toFloat()
            val dy = center.y + radius * Math.sin(angleRad).toFloat()
            
            drawCircle(
                color = color2,
                radius = (strokeWidth.toPx() * 1.3f) * pulseScale,
                center = Offset(dx, dy)
            )
        }

        // Inner counter-rotating stabilizer ring 
        Canvas(modifier = Modifier
            .fillMaxSize()
            .padding(size * 0.2f)
        ) {
            drawArc(
                color = color2.copy(alpha = 0.6f),
                startAngle = -rotation - 180f,
                sweepAngle = 140f,
                useCenter = false,
                style = Stroke(width = (strokeWidth.toPx() * 0.6f), cap = StrokeCap.Round)
            )
        }
    }
}
