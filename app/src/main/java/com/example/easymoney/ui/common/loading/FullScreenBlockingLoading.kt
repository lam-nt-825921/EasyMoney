package com.example.easymoney.ui.common.loading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun FullScreenBlockingLoading(
    modifier: Modifier = Modifier,
    scrimColor: Color = Color.Black.copy(alpha = 0.22f)
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(scrimColor),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

