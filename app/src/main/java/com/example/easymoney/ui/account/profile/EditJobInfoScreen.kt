package com.example.easymoney.ui.account.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.R
import com.example.easymoney.ui.loan.information.form.FormSheetType
import com.example.easymoney.ui.loan.information.form.SimpleSelectionBottomSheet
import com.example.easymoney.ui.loan.information.form.ThousandsSeparatorTransformation

@Composable
fun EditJobInfoScreen(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val jobInfo = uiState.profile.jobInfo
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 86.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = stringResource(id = R.string.profile_section_job),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            FormCard {
                InputField(
                    label = stringResource(id = R.string.profile_label_income),
                    value = if (jobInfo.monthlyIncome == 0L) "" else jobInfo.monthlyIncome.toString(),
                    onValueChange = { newValue ->
                        val cleanValue = ProfileInputValidator.digitsOnlyInput(newValue)
                        viewModel.updateJobInfo(income = cleanValue.toLongOrNull() ?: 0L) 
                    },
                    suffix = stringResource(R.string.common_money_suffix),
                    keyboardType = KeyboardType.Number,
                    visualTransformation = ThousandsSeparatorTransformation(),
                    imeAction = ImeAction.Done,
                    onImeAction = { focusManager.clearFocus() },
                    errorText = uiState.fieldErrors[ProfileField.INCOME]?.asMessage()
                )

                Spacer(modifier = Modifier.height(16.dp))

                SelectorItem(
                    label = stringResource(id = R.string.profile_section_job),
                    value = jobInfo.jobTitle.ifBlank { stringResource(id = R.string.error_select_profession) },
                    onClick = { viewModel.onShowSheet(FormSheetType.PROFESSION) },
                    errorText = uiState.fieldErrors[ProfileField.JOB_TITLE]?.asMessage()
                )

                // Visibility logic for Company Name and Position based on profession ID 'p1' (Office Worker)
                if (uiState.selectedProfession?.id == "p1" || (uiState.selectedProfession == null && jobInfo.jobTitle == "Nhân viên văn phòng công ty")) {
                    Spacer(modifier = Modifier.height(16.dp))
                    InputField(
                        label = stringResource(id = R.string.profile_label_company),
                        value = jobInfo.companyName,
                        onValueChange = { viewModel.updateJobInfo(company = it) },
                        placeholder = stringResource(R.string.profile_job_company_placeholder),
                        imeAction = ImeAction.Done,
                        onImeAction = { focusManager.clearFocus() },
                        errorText = uiState.fieldErrors[ProfileField.COMPANY_NAME]?.asMessage()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SelectorItem(
                        label = stringResource(id = R.string.error_select_position),
                        value = jobInfo.position.ifBlank { stringResource(id = R.string.error_select_position) },
                        onClick = { viewModel.onShowSheet(FormSheetType.POSITION) },
                        errorText = uiState.fieldErrors[ProfileField.POSITION]?.asMessage()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                SelectorItem(
                    label = stringResource(id = R.string.error_select_education),
                    value = uiState.profile.education.ifBlank { stringResource(id = R.string.error_select_education) },
                    onClick = { viewModel.onShowSheet(FormSheetType.EDUCATION) },
                    errorText = uiState.fieldErrors[ProfileField.EDUCATION]?.asMessage()
                )

                Spacer(modifier = Modifier.height(16.dp))

                SelectorItem(
                    label = stringResource(id = R.string.error_select_marital_status),
                    value = uiState.profile.maritalStatus.ifBlank { stringResource(id = R.string.error_select_marital_status) },
                    onClick = { viewModel.onShowSheet(FormSheetType.MARITAL_STATUS) },
                    errorText = uiState.fieldErrors[ProfileField.MARITAL_STATUS]?.asMessage()
                )
            }
        }

        // Bottom Action
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.saveProfile(EditProfileSection.JOB)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    enabled = !uiState.isLoading && uiState.isJobInfoValid
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    } else {
                        Text(text = stringResource(id = R.string.profile_save_changes), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
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
            val selectedId = when (uiState.activeSheet) {
                FormSheetType.PROFESSION -> uiState.selectedProfession?.id
                FormSheetType.POSITION -> uiState.selectedPosition?.id
                FormSheetType.EDUCATION -> uiState.selectedEducation?.id
                FormSheetType.MARITAL_STATUS -> uiState.selectedMaritalStatus?.id
                else -> null
            }
            
            SimpleSelectionBottomSheet(
                title = title,
                items = items,
                selectedId = selectedId,
                onItemSelected = { viewModel.onSelectItem(it) },
                onDismiss = { viewModel.onDismissSheet() }
            )
        }

        uiState.errorMessage?.let { message ->
            AlertDialog(
                onDismissRequest = viewModel::clearError,
                confirmButton = {
                    TextButton(onClick = viewModel::clearError) {
                        Text(text = stringResource(id = android.R.string.ok))
                    }
                },
                text = { Text(text = message) }
            )
        }
    }
}

@Composable
private fun FormCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) { content() }
    }
}

@Composable
private fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String? = null,
    suffix: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    errorText: String? = null
) {
    val focusManager = LocalFocusManager.current
    val isError = errorText != null
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            isError = isError,
            placeholder = { if (placeholder != null) Text(placeholder, color = MaterialTheme.colorScheme.outline) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(onDone = { onImeAction() }, onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }),
            visualTransformation = visualTransformation,
            suffix = { if (suffix != null) Text(suffix, fontWeight = FontWeight.Bold) },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                errorIndicatorColor = MaterialTheme.colorScheme.error
            )
        )
        if (errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun SelectorItem(
    label: String,
    value: String,
    onClick: () -> Unit,
    errorText: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = value, style = MaterialTheme.typography.bodyLarge, color = if (value.contains("Chọn")) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = if (errorText != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant)
        if (errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
