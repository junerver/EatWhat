package com.eatwhat.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Simple circular progress indicator that avoids Material3 animation API issues
 * This is a custom implementation to replace CircularProgressIndicator
 */
@Composable
fun SimpleCircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color.Blue,
    strokeWidth: Dp = 4.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "progress")
    
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Canvas(modifier = modifier.size(48.dp)) {
        val sweepAngle = 270f
        
        drawArc(
            color = color,
            startAngle = angle,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
    }
}