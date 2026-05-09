package com.example.easymoney.ui.account.profile

import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.ui.loan.information.form.FormSheetType
import com.example.easymoney.ui.loan.information.form.SimpleSelectionBottomSheet

@Composable
fun EditContactInfoScreen(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val contactInfo = uiState.profile.contactInfo
    val context = LocalContext.current

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let { contactUri ->
            try {
                context.contentResolver.query(contactUri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                        val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        
                        if (idIndex != -1 && nameIndex != -1) {
                            val id = cursor.getString(idIndex)
                            val name = cursor.getString(nameIndex)
                            
                            context.contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                arrayOf(id),
                                null
                            )?.use { pCursor ->
                                if (pCursor.moveToFirst()) {
                                    val numIndex = pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                    if (numIndex != -1) {
                                        val number = pCursor.getString(numIndex)
                                        viewModel.updateContactInfo(name = name, phone = number)
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

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
                OutlinedTextField(
                    value = contactInfo.contactName,
                    onValueChange = { viewModel.updateContactInfo(name = it) },
                    label = { Text("Họ và tên người liên hệ") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { contactPickerLauncher.launch(null) }) {
                            Icon(Icons.Default.ContactPage, contentDescription = null)
                        }
                    }
                )

                SelectorField(
                    label = "Mối quan hệ",
                    value = contactInfo.relationship.ifBlank { "Chọn mối quan hệ" },
                    onClick = { viewModel.onShowSheet(FormSheetType.RELATIONSHIP) }
                )

                OutlinedTextField(
                    value = contactInfo.phoneNumber,
                    onValueChange = { viewModel.updateContactInfo(phone = it) },
                    label = { Text("Số điện thoại") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Bottom Sheets
        if (uiState.activeSheet != FormSheetType.NONE) {
            SimpleSelectionBottomSheet(
                title = "Mối quan hệ",
                items = uiState.relationships,
                selectedId = null,
                onItemSelected = { viewModel.onSelectItem(it) },
                onDismiss = { viewModel.onDismissSheet() }
            )
        }
    }
}
