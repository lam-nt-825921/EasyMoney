package com.example.easymoney.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * eKYC-specific colors (camera & immersive scenes)
 * These are fixed and do NOT follow app theme (light/dark)
 */
object EkycColors {
    // ========== CAMERA SCENE ==========
    // Main backgrounds
    val cameraSceneBackground = Color(0xFF000000)  // Pure black
    val cameraSceneOverlay = Color(0x00000000)     // Transparent (for preview)
    
    // Text & UI elements
    val cameraTextPrimary = Color(0xFFFFFFFF)      // White
    val cameraTextSecondary = Color(0xFFBBBBBB)    // Light gray (secondary text)
    val cameraButtonBackground = Color(0xFFFFFFFF) // White button
    val cameraButtonText = Color(0xFF000000)       // Black text on white button
    val cameraIconColor = Color(0xFFFFFFFF)        // White icons
    
    // Overlays & guides
    val cameraFrameGuide = Color(0xFF4A4A4A)       // Gray frame border
    val cameraFrameGuideFill = Color(0x1A4A4A4A)   // Semi-transparent for guidance
    
    // Messages & feedback
    val cameraErrorText = Color(0xFFFF4444)        // Red for errors
    val cameraWarningText = Color(0xFFFFAA00)      // Orange for warnings
    val cameraSuccessText = Color(0xFF00CC00)      // Green for success
    
    // ========== PERMISSION SCENE ==========
    val permissionBackground = Color(0xFF000000)   // Same as camera
    val permissionText = Color(0xFFFFFFFF)
    val permissionButtonBackground = Color(0xFFFFFFFF)
    val permissionButtonText = Color(0xFF000000)
}

/**
 * Camera scene typography & spacing
 */
object EkycDimens {
    val cameraFrameWidthRatio = 0.8f    // Frame occupies 80% of screen width
    val cameraFrameHeightRatio = 0.6f   // Frame occupies 60% of screen height
    val cameraGuideStrokeWidth = 2f     // px
    
    val permissionTitleSize = 20f       // sp
    val permissionMessageSize = 14f     // sp
    val permissionButtonHeight = 52f    // dp
    val permissionButtonWidth = 300f    // dp
}

