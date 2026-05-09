package com.example.easymoney.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easymoney.ui.theme.TealPrimary
import com.example.easymoney.ui.theme.TextPrimary
import com.example.easymoney.ui.theme.TextSecondary

@Composable
fun EventDetailScreen(
    eventId: String,
    onBack: () -> Unit
) {
    // Mock event data
    val eventTitle = "Siêu hội hoàn tiền - Lên tới 500k"
    val eventTime = "01/05/2026 - 31/05/2026"
    val eventContent = """
        Chào mừng ngày Quốc tế Lao động, Easy Money mang đến chương trình ưu đãi đặc biệt dành cho tất cả khách hàng.
        
        Nội dung chương trình:
        1. Hoàn tiền 1% cho mọi khoản vay mới được giải ngân thành công trong tháng 5.
        2. Miễn phí phí xử lý hồ sơ cho khách hàng lần đầu sử dụng dịch vụ.
        3. Tặng voucher giảm 50k phí dịch vụ cho các giao dịch nạp tiền vào ví.
        
        Cách thức tham gia:
        - Đăng ký vay trực tiếp trên ứng dụng Easy Money.
        - Hệ thống sẽ tự động ghi nhận và hoàn tiền vào ví sau khi giải ngân 24h.
        
        Lưu ý:
        - Chương trình có thể kết thúc sớm nếu hết ngân sách.
        - Mỗi khách hàng chỉ được hưởng ưu đãi tối đa 1 lần.
    """.trimIndent()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Image Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(TealPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(64.dp)
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = eventTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    color = Color(0xFFF2F4F7),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Thời gian: $eventTime",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = eventContent,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(100.dp)) // Extra space for bottom button
            }
        }

        // Bottom Action
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shadowElevation = 8.dp,
            color = Color.White
        ) {
            Button(
                onClick = { /* Handle participate */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("Tham gia ngay", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
