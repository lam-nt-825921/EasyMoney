package com.example.easymoney.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Immutable
data class HomeFeatureColors(
    val mainBannerGradient: Brush,
    val mainBannerTitle: Color,
    val manageLoanGradient: Brush,
    val manageLoanTitle: Color,
    val suggestLoanGradient: Brush,
    val suggestLoanTitle: Color,
    val consultLoanGradient: Brush,
    val consultLoanTitle: Color
)

val LocalHomeColors = staticCompositionLocalOf {
    HomeFeatureColors(
        mainBannerGradient = Brush.horizontalGradient(listOf(Color.White, Color(0xFFE3F2FD))),
        mainBannerTitle = Color(0xFF1976D2),
        manageLoanGradient = Brush.horizontalGradient(listOf(Color.White, Color(0xFFE0F2F1))),
        manageLoanTitle = Color(0xFF00796B),
        suggestLoanGradient = Brush.horizontalGradient(listOf(Color.White, Color(0xFFFFFDE7))),
        suggestLoanTitle = Color(0xFFFBC02D),
        consultLoanGradient = Brush.horizontalGradient(listOf(Color.White, Color(0xFFF3E5F5))),
        consultLoanTitle = Color(0xFF7B1FA2)
    )
}

@Composable
fun getHomeColors(isDark: Boolean): HomeFeatureColors {
    return if (isDark) {
        // Dark Mode: Dùng màu nền tối hơn một chút nhưng vẫn giữ hiệu ứng gradient nhẹ
        HomeFeatureColors(
            mainBannerGradient = Brush.horizontalGradient(listOf(Color(0xFF7F1FD0), Color(0xFF0D47A1))),
            mainBannerTitle = Color(0xFFBBDEFB),
            manageLoanGradient = Brush.horizontalGradient(listOf(Color(0xFFB7BD01), Color(0xFF004D40))),
            manageLoanTitle = Color(0xFFB2DFDB),
            suggestLoanGradient = Brush.horizontalGradient(listOf(Color(0xFF8D9A13), Color(0xFFFBC02D).copy(alpha = 0.3f))),
            suggestLoanTitle = Color(0xFFFFF9C4),
            consultLoanGradient = Brush.horizontalGradient(listOf(Color(0xFF174486), Color(0xFF4A148C))),
            consultLoanTitle = Color(0xFFE1BEE7)
        )
    } else {
        // Light Mode: Trắng sang màu Pastel nhạt
        HomeFeatureColors(
            mainBannerGradient = Brush.horizontalGradient(listOf(Color.White, Color(0xFFBBDEFB).copy(alpha = 0.5f))),
            mainBannerTitle = Color(0xFF0D47A1),
            manageLoanGradient = Brush.horizontalGradient(listOf(Color.White, Color(0xFFB2DFDB).copy(alpha = 0.5f))),
            manageLoanTitle = Color(0xFF004D40),
            suggestLoanGradient = Brush.horizontalGradient(listOf(Color.White, Color(0xFFFFF9C4).copy(alpha = 0.5f))),
            suggestLoanTitle = Color(0xFF827717),
            consultLoanGradient = Brush.horizontalGradient(listOf(Color.White, Color(0xFFE1BEE7).copy(alpha = 0.5f))),
            consultLoanTitle = Color(0xFF4A148C)
        )
    }
}
