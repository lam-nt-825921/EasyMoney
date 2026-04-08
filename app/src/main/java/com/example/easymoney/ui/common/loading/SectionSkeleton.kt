package com.example.easymoney.ui.common.loading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SectionSkeleton(
    modifier: Modifier = Modifier,
    lines: Int = 3
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(lines) {
            SkeletonBlock(
                modifier = Modifier.padding(horizontal = 2.dp),
                height = if (it == 0) 18.dp else 14.dp
            )
        }
    }
}

