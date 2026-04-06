package com.example.easymoney.ui.loan.configuration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        SkeletonBlock(modifier = Modifier.fillMaxWidth(0.62f), height = 30.dp)

        LoanAmountLoadingSection()

        SkeletonBlock(modifier = Modifier.fillMaxWidth(0.42f), height = 20.dp)

        SkeletonBlock(height = 56.dp, cornerRadius = 12.dp)

        LoanSummaryLoadingCard()
    }
}

@Composable
private fun LoanAmountLoadingSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        SkeletonBlock(height = 104.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SkeletonBlock(modifier = Modifier.weight(1f), height = 20.dp)
            Spacer(modifier = Modifier.width(16.dp))
            SkeletonBlock(modifier = Modifier.weight(1f), height = 20.dp)
        }
    }
}

@Composable
private fun LoanSummaryLoadingCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionSkeleton(lines = 2)
            SkeletonBlock(height = 1.dp, cornerRadius = 0.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SkeletonBlock(modifier = Modifier.weight(1f), height = 14.dp)
                SkeletonBlock(modifier = Modifier.weight(1f), height = 18.dp)
            }
        }
    }
}


