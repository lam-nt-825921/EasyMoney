package com.example.easymoney.ui.loan.information.ekyc

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easymoney.R
import com.example.easymoney.ui.theme.EasyMoneyTheme

@Composable
fun EkycIntroScreen(
    @DrawableRes illustrationResId: Int,
    onStartCaptureClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkMode = androidx.compose.foundation.isSystemInDarkTheme()
    val illustrationBackground = if (isDarkMode) Color(0xFF2B2D31) else Color(0xFFEAF2F8)

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = onStartCaptureClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Bắt đầu chụp", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Chụp CCCD/CMND",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(illustrationBackground),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = illustrationResId),
                    contentDescription = "Ảnh minh họa eKYC",
                    modifier = Modifier.size(220.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 4.dp, start = 2.dp, end = 2.dp)
            ) {
                EkycInstructionText(
                    step = "Bước 1",
                    content = "Chụp mặt trước CMND/CCCD của bạn"
                )
                EkycInstructionText(
                    step = "Bước 2",
                    content = "Chụp mặt sau CMND/CCCD của bạn"
                )
                EkycInstructionText(
                    step = "Bước 3",
                    content = "Chụp ảnh chân dung của bạn"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun EkycInstructionText(step: String, content: String) {
    val annotatedText = buildAnnotatedString {
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append("$step: ")
        }
        append(content)
    }

    Text(
        text = annotatedText,
        style = MaterialTheme.typography.bodyLarge,
        fontSize = 15.sp,
        lineHeight = 24.sp
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun EkycIntroScreenPreview() {
    EasyMoneyTheme {
        EkycIntroScreen(
            illustrationResId = R.drawable.ekyc_preview_img,
            onStartCaptureClick = {}
        )
    }
}

