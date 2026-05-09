package com.example.easymoney.ui.account.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.ui.theme.TealPrimary
import com.example.easymoney.ui.theme.TextPrimary
import com.example.easymoney.ui.theme.TextSecondary

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileCompletionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.profile

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .verticalScroll(rememberScrollState())
    ) {
        // Avatar Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Box {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = TealPrimary.copy(alpha = 0.1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.padding(24.dp)
                    )
                }
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp),
                    shape = CircleShape,
                    color = TealPrimary,
                    shadowElevation = 2.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProfileInfoGroup(title = "Thông tin cơ bản") {
                ProfileInfoItem(label = "Họ và tên", value = profile.personalInfo.fullName)
                ProfileInfoItem(label = "Số điện thoại", value = profile.personalInfo.phoneNumber)
                ProfileInfoItem(label = "Số CCCD", value = profile.personalInfo.nationalId)
                ProfileInfoItem(label = "Giới tính", value = profile.personalInfo.gender)
                ProfileInfoItem(label = "Ngày sinh", value = profile.personalInfo.dateOfBirth)
            }

            ProfileInfoGroup(title = "Địa chỉ") {
                ProfileInfoItem(label = "Địa chỉ thường trú", value = profile.addressInfo.permanentAddress)
                ProfileInfoItem(label = "Địa chỉ hiện tại", value = profile.addressInfo.currentAddress)
            }

            ProfileInfoGroup(title = "Nghề nghiệp") {
                ProfileInfoItem(label = "Công việc", value = profile.jobInfo.jobTitle)
                ProfileInfoItem(label = "Thu nhập hàng tháng", value = "%,dđ".format(profile.jobInfo.monthlyIncome).replace(',', '.'))
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileInfoGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun ProfileInfoItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value.ifBlank { "Chưa cập nhật" },
            style = MaterialTheme.typography.bodyLarge,
            color = if (value.isBlank()) TextSecondary.copy(alpha = 0.5f) else TextPrimary,
            fontWeight = FontWeight.Medium
        )
        HorizontalDivider(modifier = Modifier.padding(top = 12.dp), color = Color(0xFFF2F4F7))
    }
}
