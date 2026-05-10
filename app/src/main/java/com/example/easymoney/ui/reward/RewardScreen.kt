package com.example.easymoney.ui.reward

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.ui.theme.TealPrimary
import com.example.easymoney.ui.theme.TextPrimary
import com.example.easymoney.ui.theme.TextSecondary

@Composable
fun RewardScreen(
    onBack: () -> Unit,
    viewModel: RewardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        // Points Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(TealPrimary, Color(0xFF0D5C6E))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "Điểm hiện có của bạn",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${uiState.totalPoints}",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "điểm",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFFFD54F),
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterEnd)
            )
        }

        Text(
            text = "Quà tặng tài chính nổi bật",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.rewards) { reward ->
                    RewardCard(
                        reward = reward,
                        canRedeem = uiState.totalPoints >= reward.points,
                        onRedeem = { viewModel.onRedeemRequest(reward.id) }
                    )
                }
            }
        }

        uiState.pendingConfirmId?.let { id ->
            val item = uiState.rewards.firstOrNull { it.id == id }
            if (item != null) {
                AlertDialog(
                    onDismissRequest = viewModel::onCancelRedeem,
                    title = { Text("Xác nhận đổi quà") },
                    text = {
                        Column {
                            Text(item.title, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text("Trừ ${item.points} điểm. Số dư sau: ${uiState.totalPoints - item.points} điểm.")
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = viewModel::onConfirmRedeem, enabled = !uiState.isRedeeming) {
                            Text("Xác nhận")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = viewModel::onCancelRedeem) {
                            Text("Hủy")
                        }
                    }
                )
            }
        }

        uiState.redeemSuccessMessage?.let { msg ->
            AlertDialog(
                onDismissRequest = viewModel::consumeMessages,
                title = { Text("Đổi quà thành công") },
                text = { Text(msg) },
                confirmButton = {
                    TextButton(onClick = viewModel::consumeMessages) { Text("OK") }
                }
            )
        }

        uiState.errorMessage?.let { msg ->
            LaunchedEffect(msg) { /* could show snackbar */ }
            AlertDialog(
                onDismissRequest = viewModel::consumeMessages,
                title = { Text("Thông báo") },
                text = { Text(msg) },
                confirmButton = {
                    TextButton(onClick = viewModel::consumeMessages) { Text("OK") }
                }
            )
        }
    }
}

@Composable
fun RewardCard(
    reward: RewardItem,
    canRedeem: Boolean,
    onRedeem: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color(0xFFE0F2F1), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = reward.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${reward.points} điểm",
                    style = MaterialTheme.typography.labelSmall,
                    color = TealPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onRedeem,
                enabled = canRedeem,
                modifier = Modifier.fillMaxWidth().height(32.dp),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text(text = "Đổi quà", fontSize = 12.sp)
            }
        }
    }
}
