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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.R
import com.example.easymoney.domain.model.MasterDataItem
import com.example.easymoney.ui.loan.information.form.FormSheetType
import com.example.easymoney.ui.loan.information.form.SimpleSelectionBottomSheet

@Composable
fun EditPersonalInfoScreen(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val personalInfo = uiState.profile.personalInfo
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
                text = stringResource(id = R.string.profile_section_personal),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            FormCard {
                InputField(
                    label = stringResource(id = R.string.profile_label_fullname),
                    value = personalInfo.fullName,
                    onValueChange = { viewModel.updatePersonalInfo(fullName = it) },
                    imeAction = ImeAction.Next,
                    errorText = uiState.fieldErrors[ProfileField.FULL_NAME]?.asMessage()
                )

                Spacer(modifier = Modifier.height(16.dp))

                InputField(
                    label = stringResource(id = R.string.profile_label_id_number),
                    value = personalInfo.nationalId,
                    onValueChange = {
                        viewModel.updatePersonalInfo(
                            nationalId = ProfileInputValidator.digitsOnlyInput(it, maxLength = 12)
                        )
                    },
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                    errorText = uiState.fieldErrors[ProfileField.NATIONAL_ID]?.asMessage()
                )

                Spacer(modifier = Modifier.height(16.dp))

                SelectorItem(
                    label = stringResource(id = R.string.profile_label_gender),
                    value = personalInfo.gender.ifBlank { stringResource(id = R.string.profile_gender_select) },
                    isPlaceholder = personalInfo.gender.isBlank(),
                    errorText = uiState.fieldErrors[ProfileField.GENDER]?.asMessage(),
                    onClick = { viewModel.onShowSheet(FormSheetType.GENDER) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                InputField(
                    label = stringResource(id = R.string.profile_label_dob),
                    value = personalInfo.dateOfBirth,
                    onValueChange = {
                        viewModel.updatePersonalInfo(
                            dob = ProfileInputValidator.dateOfBirthInput(it)
                        )
                    },
                    placeholder = stringResource(id = R.string.profile_placeholder_dob),
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                    onImeAction = { focusManager.clearFocus() },
                    errorText = uiState.fieldErrors[ProfileField.DATE_OF_BIRTH]?.asMessage()
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
                        viewModel.saveProfile(EditProfileSection.PERSONAL)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    enabled = !uiState.isLoading && uiState.isPersonalInfoValid
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    } else {
                        Text(text = stringResource(id = R.string.profile_save_changes), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        // Workflow #95 — Bottom sheet chọn giới tính cố định (Nam/Nữ)
        if (uiState.activeSheet == FormSheetType.GENDER) {
            val genderOptions = listOf(
                MasterDataItem(id = ProfileInputValidator.GENDER_MALE, name = stringResource(id = R.string.profile_gender_male)),
                MasterDataItem(id = ProfileInputValidator.GENDER_FEMALE, name = stringResource(id = R.string.profile_gender_female))
            )
            SimpleSelectionBottomSheet(
                title = stringResource(id = R.string.profile_label_gender),
                items = genderOptions,
                selectedId = personalInfo.gender.ifBlank { null },
                onItemSelected = { viewModel.onSelectGender(it.id) },
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
    keyboardType: KeyboardType = KeyboardType.Text,
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
    isPlaceholder: Boolean = false,
    errorText: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isPlaceholder) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
            )
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
