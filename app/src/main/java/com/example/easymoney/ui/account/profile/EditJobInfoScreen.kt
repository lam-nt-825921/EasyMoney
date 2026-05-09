package com.example.easymoney.ui.account.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.ui.loan.information.form.FormSheetType
import com.example.easymoney.ui.loan.information.form.SimpleSelectionBottomSheet

@Composable
fun EditJobInfoScreen(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val jobInfo = uiState.profile.jobInfo

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onBack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                SelectorField(
                    label = "Nghề nghiệp",
                    value = jobInfo.jobTitle.ifBlank { "Chọn nghề nghiệp" },
                    onClick = { viewModel.onShowSheet(FormSheetType.PROFESSION) }
                )

                OutlinedTextField(
                    value = if (jobInfo.monthlyIncome == 0L) "" else jobInfo.monthlyIncome.toString(),
                    onValueChange = { viewModel.updateJobInfo(income = it.toLongOrNull() ?: 0L) },
                    label = { Text("Thu nhập hàng tháng") },
                    suffix = { Text("đ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = jobInfo.companyName,
                    onValueChange = { viewModel.updateJobInfo(company = it) },
                    label = { Text("Tên công ty") },
                    modifier = Modifier.fillMaxWidth()
                )

                SelectorField(
                    label = "Chức vụ",
                    value = jobInfo.position.ifBlank { "Chọn chức vụ" },
                    onClick = { viewModel.onShowSheet(FormSheetType.POSITION) }
                )

                SelectorField(
                    label = "Trình độ học vấn",
                    value = uiState.profile.education.ifBlank { "Chọn trình độ học vấn" },
                    onClick = { viewModel.onShowSheet(FormSheetType.EDUCATION) }
                )

                SelectorField(
                    label = "Tình trạng hôn nhân",
                    value = uiState.profile.maritalStatus.ifBlank { "Chọn tình trạng hôn nhân" },
                    onClick = { viewModel.onShowSheet(FormSheetType.MARITAL_STATUS) }
                )
            }
        }

        // Bottom Sheets
        if (uiState.activeSheet != FormSheetType.NONE) {
            val title = when (uiState.activeSheet) {
                FormSheetType.PROFESSION -> "Nghề nghiệp"
                FormSheetType.POSITION -> "Chức vụ"
                FormSheetType.EDUCATION -> "Trình độ học vấn"
                FormSheetType.MARITAL_STATUS -> "Tình trạng hôn nhân"
                else -> ""
            }
            val items = when (uiState.activeSheet) {
                FormSheetType.PROFESSION -> uiState.professions
                FormSheetType.POSITION -> uiState.positions
                FormSheetType.EDUCATION -> uiState.educationLevels
                FormSheetType.MARITAL_STATUS -> uiState.maritalStatuses
                else -> emptyList()
            }
            
            SimpleSelectionBottomSheet(
                title = title,
                items = items,
                selectedId = null,
                onItemSelected = { viewModel.onSelectItem(it) },
                onDismiss = { viewModel.onDismissSheet() }
            )
        }
    }
}

@Composable
fun SelectorField(label: String, value: String, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedCard(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = value, style = MaterialTheme.typography.bodyLarge)
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            }
        }
    }
}
