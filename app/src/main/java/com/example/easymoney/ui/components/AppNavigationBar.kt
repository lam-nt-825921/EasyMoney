package com.example.easymoney.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigationBar(
    title: String,
    showBackButton: Boolean,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    showHelpButton: Boolean = true,
    onHelpClick: () -> Unit = {},
    backgroundColor: Color = Color.Transparent,
    contentColor: Color = Color.Unspecified,
    topBarMode: TopBarMode = TopBarMode.STANDARD,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val resolvedContentColor = if (contentColor != Color.Unspecified) {
        contentColor
    } else {
        autoContentColorFor(backgroundColor = backgroundColor, colorScheme = MaterialTheme.colorScheme)
    }

    val colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = backgroundColor,
        scrolledContainerColor = backgroundColor,
        titleContentColor = resolvedContentColor,
        navigationIconContentColor = resolvedContentColor,
        actionIconContentColor = resolvedContentColor
    )

    CenterAlignedTopAppBar(
        title = {
            if (topBarMode != TopBarMode.NO_TITLE) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = resolvedContentColor
                )
            }
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            if (showHelpButton) {
                IconButton(onClick = onHelpClick) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Help",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        },
        colors = colors,
        modifier = modifier,
        scrollBehavior = scrollBehavior
    )
}

private fun autoContentColorFor(backgroundColor: Color, colorScheme: ColorScheme): Color {
    val blendedBackground = if (backgroundColor.alpha < 1f) {
        backgroundColor.compositeOver(colorScheme.background)
    } else {
        backgroundColor
    }

    val materialMappedColor = colorScheme.contentColorFor(blendedBackground)
    if (materialMappedColor != Color.Unspecified) {
        return materialMappedColor
    }

    return if (blendedBackground.luminance() > 0.5f) Color(0xFF1A1A1A) else Color.White
}



