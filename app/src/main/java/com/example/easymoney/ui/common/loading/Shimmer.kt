package com.example.easymoney.ui.common.loading

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun rememberShimmerBrush(
    shimmerWidth: Float = 220f,
    baseColor: Color = Color(0xFFF2F4F7),
    highlightColor: Color = Color(0xFFE4E7EC)
): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val xOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = shimmerWidth * 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    return Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(x = xOffset - shimmerWidth, y = 0f),
        end = Offset(x = xOffset, y = shimmerWidth)
    )
}

fun Modifier.shimmerBackground(): Modifier = composed {
    background(rememberShimmerBrush())
}

@Composable
fun ShimmerDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color(0xFFEAECF0))
    )
}

