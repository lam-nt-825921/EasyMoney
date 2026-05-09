package com.example.easymoney.ui.account.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun EditPersonalInfoScreen(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val personalInfo = uiState.profile.personalInfo

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onBack()
    }

    Scaffold(
        bottomBar = {
            Button(
                onClick = { viewModel.saveProfile() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(54.dp),
                shape = RoundedCornerShape(27.dp)
            ) {
                Text("Lưu thay đổi", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = personalInfo.fullName,
                onValueChange = { viewModel.updatePersonalInfo(fullName = it) },
                label = { Text("Họ và tên") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = personalInfo.nationalId,
                onValueChange = { viewModel.updatePersonalInfo(nationalId = it) },
                label = { Text("Số CCCD") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = personalInfo.gender,
                onValueChange = { viewModel.updatePersonalInfo(gender = it) },
                label = { Text("Giới tính") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = personalInfo.dateOfBirth,
                onValueChange = { viewModel.updatePersonalInfo(dob = it) },
                label = { Text("Ngày sinh") },
                placeholder = { Text("YYYY-MM-DD") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
