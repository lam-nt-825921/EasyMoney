package com.example.easymoney.ui.common.error

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easymoney.R
import com.example.easymoney.ui.theme.LocalDarkMode

/**
 * Màn hình thông báo lỗi chung cho ứng dụng.
 * @param title Tiêu đề lỗi (ví dụ: "Hồ sơ chưa đạt yêu cầu")
 * @param message Nội dung mô tả chi tiết lỗi
 * @param buttonText Văn bản trên nút bấm duy nhất
 * @param onButtonClick Hành động khi nhấn nút
 */
@Composable
fun AppErrorScreen(
    title: String,
    message: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    secondaryButtonText: String? = null,
    onSecondaryButtonClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDarkMode = LocalDarkMode.current
    val illustrationRes = if (isDarkMode) R.drawable.error_1_dark else R.drawable.error_1_light

    Scaffold(
        modifier = modifier,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onButtonClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE60023),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = buttonText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (secondaryButtonText != null && onSecondaryButtonClick != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = onSecondaryButtonClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = secondaryButtonText,
                            color = Color(0xFFE60023),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) {
 paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = illustrationRes),
                contentDescription = null,
                modifier = Modifier
                    .size(280.dp) // Kích thước ảnh cân đối theo mẫu
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }
    }
}
