package com.example.easymoney.ui.loan.information.form

import android.Manifest
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LoanInformationFormScreen(
    onNextStep: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: LoanInformationFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Launcher để mở danh bạ hệ thống
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let { contactUri ->
            try {
                // Query để lấy tên và ID contact
                context.contentResolver.query(contactUri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                        val name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                        
                        // Query lấy số điện thoại (Yêu cầu quyền READ_CONTACTS)
                        context.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(id),
                            null
                        )?.use { pCursor ->
                            if (pCursor.moveToFirst()) {
                                val number = pCursor.getString(pCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                viewModel.onContactNameChanged(name)
                                viewModel.onContactPhoneChanged(number)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ContactPicker", "Error picking contact", e)
                // Fallback nếu có lỗi (ví dụ: SecurityException)
            }
        }
    }

    // Launcher để xin quyền trước khi mở danh bạ
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            contactPickerLauncher.launch(null)
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = onNextStep,
                    enabled = uiState.isFormValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                ) {
                    Text(text = "Tiếp tục", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section: Địa chỉ thường trú
            FormSection(title = "Địa chỉ thường trú") {
                AddressSummaryItem(
                    label = "Chọn Tỉnh/Thành phố, Quận/Huyện, Phường/Xã",
                    value = if (uiState.permanentProvince == null) "Chọn địa chỉ" 
                            else "${uiState.permanentProvince?.name}, ${uiState.permanentDistrict?.name ?: ""}, ${uiState.permanentWard?.name ?: ""}".trimEnd(',', ' '),
                    onClick = { viewModel.onShowSheet(FormSheetType.PROVINCE, true) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                InputField(
                    label = "Địa chỉ chi tiết",
                    value = uiState.permanentDetail,
                    onValueChange = { viewModel.onDetailAddressChanged(it, true) },
                    maxLength = 150
                )
            }

            // Section: Địa chỉ hiện tại
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Địa chỉ hiện tại",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Switch(
                        checked = uiState.isCurrentSameAsPermanent,
                        onCheckedChange = { viewModel.onCurrentAddressToggle(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFD1D1D1)
                        )
                    )
                }
                Text(
                    text = "Giống với địa chỉ thường trú",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (!uiState.isCurrentSameAsPermanent) {
                    FormSection {
                        AddressSummaryItem(
                            label = "Chọn Tỉnh/Thành phố, Quận/Huyện, Phường/Xã",
                            value = if (uiState.currentProvince == null) "Chọn địa chỉ" 
                                    else "${uiState.currentProvince?.name}, ${uiState.currentDistrict?.name ?: ""}, ${uiState.currentWard?.name ?: ""}".trimEnd(',', ' '),
                            onClick = { viewModel.onShowSheet(FormSheetType.PROVINCE, false) }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        InputField(
                            label = "Địa chỉ chi tiết",
                            value = uiState.currentDetail,
                            onValueChange = { viewModel.onDetailAddressChanged(it, false) },
                            maxLength = 150
                        )
                    }
                }
            }

            // Section: Thông tin cá nhân
            FormSection(title = "Thông tin cá nhân") {
                InputField(
                    label = "Thu nhập hàng tháng",
                    value = uiState.monthlyIncome,
                    onValueChange = viewModel::onMonthlyIncomeChanged,
                    suffix = "đ",
                    keyboardType = KeyboardType.Number,
                    placeholder = "1.000.000"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                SelectorItem(
                    label = "Nghề nghiệp",
                    value = uiState.profession?.name ?: "Chọn nghề nghiệp",
                    onClick = { viewModel.onShowSheet(FormSheetType.PROFESSION) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SelectorItem(
                    label = "Chức vụ",
                    value = uiState.position?.name ?: "Chọn chức vụ",
                    onClick = { viewModel.onShowSheet(FormSheetType.POSITION) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SelectorItem(
                    label = "Trình độ học vấn",
                    value = uiState.education?.name ?: "Chọn trình độ học vấn",
                    onClick = { viewModel.onShowSheet(FormSheetType.EDUCATION) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SelectorItem(
                    label = "Tình trạng hôn nhân",
                    value = uiState.maritalStatus?.name ?: "Chọn tình trạng hôn nhân",
                    onClick = { viewModel.onShowSheet(FormSheetType.MARITAL_STATUS) }
                )
            }

            // Section: Thông tin người liên hệ
            FormSection(title = "Thông tin người liên hệ") {
                InputField(
                    label = "Họ và tên",
                    value = uiState.contactName,
                    onValueChange = viewModel::onContactNameChanged
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                SelectorItem(
                    label = "Mối quan hệ với bạn",
                    value = uiState.contactRelationship?.name ?: "Chọn mối quan hệ",
                    onClick = { viewModel.onShowSheet(FormSheetType.RELATIONSHIP) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                InputField(
                    label = "Số điện thoại",
                    value = uiState.contactPhone,
                    onValueChange = viewModel::onContactPhoneChanged,
                    keyboardType = KeyboardType.Phone,
                    trailingIcon = {
                        Column(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable {
                                    val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                        contactPickerLauncher.launch(null)
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                                    }
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.ContactPage,
                                contentDescription = "Contacts",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "Danh bạ", 
                                fontSize = 10.sp, 
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }

    // Xử lý hiển thị Bottom Sheets dựa trên loại
    val sheetType = uiState.activeSheet
    if (sheetType != FormSheetType.NONE) {
        val isHierarchical = sheetType == FormSheetType.PROVINCE || sheetType == FormSheetType.DISTRICT || sheetType == FormSheetType.WARD
        
        val title = when(sheetType) {
            FormSheetType.PROVINCE -> "Tỉnh/Thành phố"
            FormSheetType.DISTRICT -> "Quận/Huyện"
            FormSheetType.WARD -> "Phường/Xã"
            FormSheetType.PROFESSION -> "Nghề nghiệp"
            FormSheetType.POSITION -> "Chức vụ"
            FormSheetType.EDUCATION -> "Trình độ học vấn"
            FormSheetType.MARITAL_STATUS -> "Tình trạng hôn nhân"
            FormSheetType.RELATIONSHIP -> "Mối quan hệ với bạn"
            else -> ""
        }
        
        val items = when(sheetType) {
            FormSheetType.PROVINCE -> uiState.provinces
            FormSheetType.DISTRICT -> uiState.districts
            FormSheetType.WARD -> uiState.wards
            FormSheetType.PROFESSION -> uiState.professions
            FormSheetType.POSITION -> uiState.positions
            FormSheetType.EDUCATION -> uiState.educationLevels
            FormSheetType.MARITAL_STATUS -> uiState.maritalStatuses
            FormSheetType.RELATIONSHIP -> uiState.relationships
            else -> emptyList()
        }

        val selectedId = when(sheetType) {
            FormSheetType.PROVINCE -> if (uiState.isSelectingPermanentAddress) uiState.permanentProvince?.id else uiState.currentProvince?.id
            FormSheetType.DISTRICT -> if (uiState.isSelectingPermanentAddress) uiState.permanentDistrict?.id else uiState.currentDistrict?.id
            FormSheetType.WARD -> if (uiState.isSelectingPermanentAddress) uiState.permanentWard?.id else uiState.currentWard?.id
            FormSheetType.PROFESSION -> uiState.profession?.id
            FormSheetType.POSITION -> uiState.position?.id
            FormSheetType.EDUCATION -> uiState.education?.id
            FormSheetType.MARITAL_STATUS -> uiState.maritalStatus?.id
            FormSheetType.RELATIONSHIP -> uiState.contactRelationship?.id
            else -> null
        }

        if (isHierarchical) {
            HierarchicalSelectionBottomSheet(
                title = title,
                items = items,
                selectedId = selectedId,
                onItemSelected = { viewModel.onSelectItem(it) },
                onBack = if (sheetType != FormSheetType.PROVINCE) { { viewModel.onBackSheet() } } else null,
                onDismiss = { viewModel.onDismissSheet() }
            )
        } else {
            SimpleSelectionBottomSheet(
                title = title,
                items = items,
                selectedId = selectedId,
                onItemSelected = { viewModel.onSelectItem(it) },
                onDismiss = { viewModel.onDismissSheet() }
            )
        }
    }
}

@Composable
private fun FormSection(
    title: String? = null,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (title != null) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    maxLength: Int? = null,
    suffix: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false,
    placeholder: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 14.sp, color = Color.Gray)
            if (maxLength != null) {
                Text(text = "${value.length}/$maxLength", fontSize = 12.sp, color = Color.Gray)
            }
        }
        
        TextField(
            value = value,
            onValueChange = { if (maxLength == null || it.length <= maxLength) onValueChange(it) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = readOnly,
            placeholder = { if (placeholder != null) Text(placeholder, color = Color.LightGray) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            suffix = { if (suffix != null) Text(suffix, fontWeight = FontWeight.Bold) },
            trailingIcon = trailingIcon,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                unfocusedIndicatorColor = Color(0xFFEAECF0),
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
    }
}

@Composable
private fun SelectorItem(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Text(text = label, fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                color = if (value.contains("Chọn")) Color.LightGray else Color.Black
            )
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = Color(0xFFEAECF0))
    }
}

@Composable
private fun AddressSummaryItem(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                color = if (value.contains("Chọn")) Color.LightGray else Color.Black,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = Color(0xFFEAECF0))
    }
}
