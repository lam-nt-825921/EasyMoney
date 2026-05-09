package com.example.easymoney.ui.account.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.domain.model.ProfileVerificationStatus
import com.example.easymoney.ui.common.identity.*

@Composable
fun ProfileCompletionScreen(
    onBack: () -> Unit,
    onNavigateToEditPersonalInfo: () -> Unit,
    onNavigateToEditJobInfo: () -> Unit,
    onNavigateToEditContactInfo: () -> Unit,
    viewModel: ProfileCompletionViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // Status Banner
            ProfileStatusBanner(
                status = uiState.profile.verificationStatus,
                message = uiState.profile.statusMessage ?: "Thông tin tài khoản"
            )

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Identity Verification Section
                IdentitySection(
                    status = uiState.profile.identityStatus,
                    onFaceClick = { viewModel.openModule(IdentityModule.FACE_CAPTURE) },
                    onNfcClick = { viewModel.openModule(IdentityModule.NFC_READER) },
                    onBiometricClick = { viewModel.openModule(IdentityModule.BIOMETRIC) },
                    onDocumentClick = { viewModel.openModule(IdentityModule.DOCUMENT_UPLOAD) }
                )

                // Personal Info Section
                ProfileSectionCard(
                    title = "Thông tin cá nhân",
                    icon = Icons.Default.Person,
                    isCompleted = uiState.profile.personalInfo.fullName.isNotBlank(),
                    onClick = onNavigateToEditPersonalInfo
                ) {
                    InfoItem("Họ và tên", uiState.profile.personalInfo.fullName)
                    InfoItem("Số điện thoại", uiState.profile.personalInfo.phoneNumber)
                    InfoItem("Số CCCD", uiState.profile.personalInfo.nationalId)
                }

                // Job & Income Section
                ProfileSectionCard(
                    title = "Công việc & Thu nhập",
                    icon = Icons.Default.Work,
                    isCompleted = uiState.profile.jobInfo.jobTitle.isNotBlank(),
                    onClick = onNavigateToEditJobInfo
                ) {
                    InfoItem("Nghề nghiệp", uiState.profile.jobInfo.jobTitle)
                    InfoItem("Thu nhập hàng tháng", "%,d VNĐ".format(uiState.profile.jobInfo.monthlyIncome))
                }

                // Contact Info Section
                ProfileSectionCard(
                    title = "Người liên hệ",
                    icon = Icons.Default.ContactPhone,
                    isCompleted = uiState.profile.contactInfo.contactName.isNotBlank(),
                    onClick = onNavigateToEditContactInfo
                ) {
                    InfoItem("Họ và tên", uiState.profile.contactInfo.contactName)
                    InfoItem("Mối quan hệ", uiState.profile.contactInfo.relationship)
                    InfoItem("Số điện thoại", uiState.profile.contactInfo.phoneNumber)
                }
            }
        }

        // Module Overlays
        uiState.activeModule?.let { module ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
            ) {
                when (module) {
                    IdentityModule.FACE_CAPTURE -> {
                        FaceCaptureModule(
                            onResult = { viewModel.onFaceCaptureResult(it) },
                            onDismiss = { viewModel.closeModule() }
                        )
                    }
                    IdentityModule.NFC_READER -> {
                        NfcReaderModule(
                            onResult = { viewModel.onNfcResult(it) },
                            onDismiss = { viewModel.closeModule() }
                        )
                    }
                    IdentityModule.BIOMETRIC -> {
                        BiometricModule(
                            onResult = { viewModel.onBiometricResult(it) }
                        )
                    }
                    IdentityModule.DOCUMENT_UPLOAD -> {
                        DocumentUploadModule(
                            onResult = { viewModel.closeModule() },
                            onDismiss = { viewModel.closeModule() },
                            onError = { /* Show toast */ }
                        )
                    }
                }
                
                // Close button for overlays
                IconButton(
                    onClick = { viewModel.closeModule() },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun ProfileStatusBanner(
    status: ProfileVerificationStatus,
    message: String
) {
    val (bgColor, textColor, icon) = when (status) {
        ProfileVerificationStatus.VERIFIED -> Triple(
            Color(0xFFE8F5E9), Color(0xFF2E7D32), Icons.Default.CheckCircle
        )
        ProfileVerificationStatus.PENDING -> Triple(
            Color(0xFFE3F2FD), Color(0xFF1565C0), Icons.Default.History
        )
        ProfileVerificationStatus.INCOMPLETE -> Triple(
            Color(0xFFFFF3E0), Color(0xFFEF6C00), Icons.Default.Info
        )
        ProfileVerificationStatus.REJECTED, ProfileVerificationStatus.EXPIRED -> Triple(
            Color(0xFFFFEBEE), Color(0xFFC62828), Icons.Default.Error
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = textColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun IdentitySection(
    status: com.example.easymoney.domain.model.IdentityVerificationStatus,
    onFaceClick: () -> Unit,
    onNfcClick: () -> Unit,
    onBiometricClick: () -> Unit,
    onDocumentClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Xác thực danh tính",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            IdentityTaskItem(
                title = "Xác thực khuôn mặt (eKYC)",
                isDone = status.isFaceVerified,
                onClick = onFaceClick
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            IdentityTaskItem(
                title = "Quét thẻ chip CCCD (NFC)",
                isDone = status.isNfcVerified,
                onClick = onNfcClick
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            IdentityTaskItem(
                title = "Sinh trắc học thiết bị",
                isDone = status.isBiometricEnabled,
                onClick = onBiometricClick
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            IdentityTaskItem(
                title = "Tải hồ sơ/giấy tờ",
                isDone = false,
                onClick = onDocumentClick
            )
        }
    }
}

@Composable
fun IdentityTaskItem(
    title: String,
    isDone: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!isDone) onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isDone) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        if (!isDone) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun ProfileSectionCard(
    title: String,
    icon: ImageVector,
    isCompleted: Boolean,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (isCompleted) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onClick,
                modifier = Modifier.align(Alignment.End),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(text = if (isCompleted) "Chỉnh sửa" else "Bổ sung ngay")
            }
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(text = value.ifBlank { "Chưa cập nhật" }, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}
