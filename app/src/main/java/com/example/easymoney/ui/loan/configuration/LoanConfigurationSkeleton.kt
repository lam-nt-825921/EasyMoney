package com.example.easymoney.ui.loan.configuration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.easymoney.ui.common.loading.SectionSkeleton
import com.example.easymoney.ui.common.loading.SkeletonBlock

@Composable
fun LoanConfigurationSkeleton(
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        bottomBar = { LoanConfigurationBottomSkeleton() },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SkeletonBlock(modifier = Modifier.fillMaxWidth(0.62f), height = 30.dp)

            SkeletonBlock(height = 120.dp, cornerRadius = 14.dp)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SkeletonBlock(modifier = Modifier.fillMaxWidth(0.36f), height = 14.dp)
                SkeletonBlock(height = 24.dp, cornerRadius = 6.dp)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SectionSkeleton(lines = 2)
                    SkeletonBlock(modifier = Modifier.fillMaxWidth(), height = 1.dp, cornerRadius = 0.dp)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SkeletonBlock(modifier = Modifier.weight(1f), height = 14.dp)
                        SkeletonBlock(modifier = Modifier.weight(1f), height = 18.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoanConfigurationBottomSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SkeletonBlock(modifier = Modifier.fillMaxWidth(0.5f), height = 20.dp)
        SkeletonBlock(modifier = Modifier.fillMaxWidth(), height = 52.dp, cornerRadius = 30.dp)
        Spacer(modifier = Modifier.height(6.dp))
    }
}

