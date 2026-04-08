package com.example.easymoney.ui.common.loading

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun rememberShimmerBrush(
    shimmerWidth: Float = 220f,
    baseColor: Color = Color(0xFFF2F4F7),
    highlightColor: Color = Color(0xFFE4E7EC)
): Brush {
    return Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset.Zero,
        end = Offset(x = shimmerWidth, y = shimmerWidth)
    )
}

fun Modifier.shimmerBackground(): Modifier = composed {
    background(rememberShimmerBrush())
}


