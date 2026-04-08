package com.example.easymoney.ui.loan.information.form

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.easymoney.ui.common.loading.SkeletonBlock

@Composable
fun LoanInformationFormSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Section Skeleton: Địa chỉ thường trú
        SkeletonSection(titleWidth = 0.4f, lines = 2)

        // Section Skeleton: Địa chỉ hiện tại
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkeletonBlock(modifier = Modifier.fillMaxWidth(0.35f), height = 24.dp)
                SkeletonBlock(modifier = Modifier.width(48.dp), height = 24.dp, cornerRadius = 12.dp)
            }
            SkeletonBlock(modifier = Modifier.fillMaxWidth(0.5f), height = 16.dp)
        }

        // Section Skeleton: Thông tin cá nhân
        SkeletonSection(titleWidth = 0.45f, lines = 4)

        // Section Skeleton: Thông tin người liên hệ
        SkeletonSection(titleWidth = 0.5f, lines = 3)
    }
}

@Composable
private fun SkeletonSection(titleWidth: Float, lines: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SkeletonBlock(modifier = Modifier.fillMaxWidth(titleWidth), height = 22.dp)
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                repeat(lines) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SkeletonBlock(modifier = Modifier.fillMaxWidth(0.3f), height = 14.dp)
                        SkeletonBlock(modifier = Modifier.fillMaxWidth(), height = 32.dp)
                    }
                }
            }
        }
    }
}
