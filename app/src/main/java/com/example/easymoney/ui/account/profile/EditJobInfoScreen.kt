package com.example.easymoney.ui.account.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.ui.loan.information.form.FormSheetType
import com.example.easymoney.ui.loan.information.form.SimpleSelectionBottomSheet

import androidx.compose.ui.res.stringResource
import com.example.easymoney.R

@Composable
fun EditJobInfoScreen(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val jobInfo = uiState.profile.jobInfo
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onBack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.saveProfile()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .padding(16.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp)
                ) {
                    Text(stringResource(id = R.string.profile_save_changes), fontWeight = FontWeight.Bold)
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { focusManager.clearFocus() })
                    }
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SelectorField(
                    label = stringResource(id = R.string.profile_section_job),
                    value = jobInfo.jobTitle.ifBlank { stringResource(id = R.string.error_select_profession) },
                    onClick = { viewModel.onShowSheet(FormSheetType.PROFESSION) }
                )

                OutlinedTextField(
                    value = if (jobInfo.monthlyIncome == 0L) "" else jobInfo.monthlyIncome.toString(),
                    onValueChange = { viewModel.updateJobInfo(income = it.toLongOrNull() ?: 0L) },
                    label = { Text(stringResource(id = R.string.profile_label_income)) },
                    suffix = { Text(stringResource(id = R.string.profile_unit_currency_short)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = jobInfo.companyName,
                    onValueChange = { viewModel.updateJobInfo(company = it) },
                    label = { Text(stringResource(id = R.string.profile_label_company)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth()
                )

                SelectorField(
                    label = stringResource(id = R.string.error_select_position),
                    value = jobInfo.position.ifBlank { stringResource(id = R.string.error_select_position) },
                    onClick = { viewModel.onShowSheet(FormSheetType.POSITION) }
                )

                SelectorField(
                    label = stringResource(id = R.string.error_select_education),
                    value = uiState.profile.education.ifBlank { stringResource(id = R.string.error_select_education) },
                    onClick = { viewModel.onShowSheet(FormSheetType.EDUCATION) }
                )

                SelectorField(
                    label = stringResource(id = R.string.profile_label_relationship),
                    value = uiState.profile.maritalStatus.ifBlank { stringResource(id = R.string.error_select_marital_status) },
                    onClick = { viewModel.onShowSheet(FormSheetType.MARITAL_STATUS) }
                )
            }
        }

        // Bottom Sheets
        if (uiState.activeSheet != FormSheetType.NONE) {
            val title = when (uiState.activeSheet) {
                FormSheetType.PROFESSION -> stringResource(id = R.string.profile_section_job)
                FormSheetType.POSITION -> stringResource(id = R.string.error_select_position)
                FormSheetType.EDUCATION -> stringResource(id = R.string.error_select_education)
                FormSheetType.MARITAL_STATUS -> stringResource(id = R.string.error_select_marital_status)
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
