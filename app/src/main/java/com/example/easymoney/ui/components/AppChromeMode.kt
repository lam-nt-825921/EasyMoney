package com.example.easymoney.ui.components

enum class TopBarMode {
    STANDARD, NO_TITLE, HIDDEN
}

enum class SystemBarMode {
    THEME_DEFAULT, CAMERA_DARK_IMMERSIVE
}

enum class ScreenColorMode {
    THEME_AWARE, FIXED_CAMERA_BLACK
}

data class AppChromeConfig(
    val topBarMode: TopBarMode = TopBarMode.STANDARD,
    val systemBarMode: SystemBarMode = SystemBarMode.THEME_DEFAULT,
    val screenColorMode: ScreenColorMode = ScreenColorMode.THEME_AWARE
)
