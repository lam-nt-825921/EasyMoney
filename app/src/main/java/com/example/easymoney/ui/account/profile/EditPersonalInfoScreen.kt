package com.example.easymoney.ui.account.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.compose.ui.res.stringResource
import com.example.easymoney.R

@Composable
fun EditPersonalInfoScreen(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val personalInfo = uiState.profile.personalInfo
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onBack()
    }

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
            OutlinedTextField(
                value = personalInfo.fullName,
                onValueChange = { viewModel.updatePersonalInfo(fullName = it) },
                label = { Text(stringResource(id = R.string.profile_label_fullname)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = personalInfo.nationalId,
                onValueChange = { viewModel.updatePersonalInfo(nationalId = it) },
                label = { Text(stringResource(id = R.string.profile_label_id_number)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = personalInfo.gender,
                onValueChange = { viewModel.updatePersonalInfo(gender = it) },
                label = { Text(stringResource(id = R.string.profile_label_gender)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = personalInfo.dateOfBirth,
                onValueChange = { viewModel.updatePersonalInfo(dob = it) },
                label = { Text(stringResource(id = R.string.profile_label_dob)) },
                placeholder = { Text(stringResource(id = R.string.profile_placeholder_dob)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
