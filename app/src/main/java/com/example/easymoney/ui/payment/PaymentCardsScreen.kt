package com.example.easymoney.ui.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun PaymentCardsScreen(
    onBack: () -> Unit,
    onAddCard: () -> Unit,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(uiState.cards) { card ->
                CreditCardItem(card)
            }
            
            item {
                OutlinedButton(
                    onClick = onAddCard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(TealPrimary, TealPrimary)))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Thêm thẻ mới", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CreditCardItem(card: com.example.easymoney.domain.model.PaymentCard) {
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFF1A237E), Color(0xFF0D47A1))
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        shadowElevation = 4.dp
    ) {
        Box(modifier = Modifier.background(gradient).padding(24.dp)) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    Text(text = card.bankName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                
                Text(
                    text = card.cardNumber,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    letterSpacing = 2.sp
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = "CARD HOLDER", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
                        Text(text = "NGUYEN VAN A", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "EXPIRES", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
                        Text(text = "12/28", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
