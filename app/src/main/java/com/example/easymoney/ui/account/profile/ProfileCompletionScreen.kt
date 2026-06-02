package com.example.easymoney.ui.account.profile

import android.nfc.NfcAdapter
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.R
import com.example.easymoney.domain.model.ProfileVerificationStatus
import com.example.easymoney.ui.common.identity.*
import com.example.easymoney.ui.loan.information.ekyc.EkycFaceCaptureScreen
import java.text.NumberFormat
import java.util.Locale

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
    val context = LocalContext.current
    val supportsNfc = remember { NfcAdapter.getDefaultAdapter(context) != null }

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
                message = uiState.profile.statusMessage ?: stringResource(R.string.profile_completion_status_default)
            )

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Identity Verification Section
                IdentitySection(
                    status = uiState.profile.identityStatus,
                    onFaceClick = { viewModel.openModule(IdentityModule.FACE_CAPTURE, supportsNfc = supportsNfc) },
                    onNfcClick = { viewModel.openModule(IdentityModule.NFC_READER, supportsNfc = supportsNfc) },
                    onDocumentClick = { viewModel.openModule(IdentityModule.DOCUMENT_UPLOAD, supportsNfc = supportsNfc) }
                )

                // Personal Info Section
                ProfileSectionCard(
                    title = stringResource(R.string.profile_section_personal),
                    icon = Icons.Default.Person,
                    isCompleted = uiState.profile.personalInfo.fullName.isNotBlank(),
                    onClick = onNavigateToEditPersonalInfo
                ) {
                    InfoItem(stringResource(R.string.profile_label_fullname), uiState.profile.personalInfo.fullName)
                    InfoItem(stringResource(R.string.profile_label_phone), uiState.profile.personalInfo.phoneNumber)
                    InfoItem(stringResource(R.string.profile_label_id_number), uiState.profile.personalInfo.nationalId)
                }

                // Job & Income Section
                ProfileSectionCard(
                    title = stringResource(R.string.profile_section_job_income),
                    icon = Icons.Default.Work,
                    isCompleted = uiState.profile.jobInfo.jobTitle.isNotBlank(),
                    onClick = onNavigateToEditJobInfo
                ) {
                    val income = NumberFormat.getNumberInstance(Locale.getDefault()).format(uiState.profile.jobInfo.monthlyIncome)
                    InfoItem(stringResource(R.string.profile_label_job_title), uiState.profile.jobInfo.jobTitle)
                    InfoItem(
                        stringResource(R.string.profile_label_income),
                        stringResource(R.string.profile_income_value, income, stringResource(R.string.profile_unit_currency_short))
                    )
                }

                // Contact Info Section
                ProfileSectionCard(
                    title = stringResource(R.string.profile_section_contact),
                    icon = Icons.Default.ContactPhone,
                    isCompleted = uiState.profile.contactInfo.contactName.isNotBlank(),
                    onClick = onNavigateToEditContactInfo
                ) {
                    InfoItem(stringResource(R.string.profile_label_fullname), uiState.profile.contactInfo.contactName)
                    InfoItem(stringResource(R.string.profile_label_relationship), uiState.profile.contactInfo.relationship)
                    InfoItem(stringResource(R.string.profile_label_phone), uiState.profile.contactInfo.phoneNumber)
                }
            }
        }

        // Module Overlays
        uiState.activeModule?.let { module ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.9f))
            ) {
                when (module) {
                    IdentityModule.FACE_CAPTURE -> {
                        EkycFaceCaptureScreen(
                            onBackToIntro = { viewModel.closeModule() },
                            onSuccess = { viewModel.onFaceCaptureUploaded() },
                            onNavigateToError = { viewModel.onIdentityError(it) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    IdentityModule.NFC_READER -> {
                        NfcReaderModule(
                            onResult = { viewModel.onNfcResult(it) },
                            onDismiss = { viewModel.closeModule() }
                        )
                    }
                    IdentityModule.DOCUMENT_UPLOAD -> {
                        DocumentUploadModule(
                            onResult = { viewModel.onDocumentUploadResult(it) },
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
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.dialog_button_close),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        if (uiState.isSubmittingIdentity) {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.errorMessage?.let { message ->
            AlertDialog(
                onDismissRequest = viewModel::clearError,
                title = { Text(stringResource(R.string.dialog_error_title)) },
                text = { Text(message.asString()) },
                confirmButton = {
                    TextButton(onClick = viewModel::clearError) {
                        Text(stringResource(R.string.dialog_button_close))
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileStatusBanner(
    status: ProfileVerificationStatus,
    message: String
) {
    val scheme = MaterialTheme.colorScheme
    val (bgColor, textColor, icon) = when (status) {
        ProfileVerificationStatus.VERIFIED -> Triple(
            scheme.primaryContainer, scheme.onPrimaryContainer, Icons.Default.CheckCircle
        )
        ProfileVerificationStatus.PENDING -> Triple(
            scheme.secondaryContainer, scheme.onSecondaryContainer, Icons.Default.History
        )
        ProfileVerificationStatus.INCOMPLETE -> Triple(
            scheme.tertiaryContainer, scheme.onTertiaryContainer, Icons.Default.Info
        )
        ProfileVerificationStatus.REJECTED, ProfileVerificationStatus.EXPIRED -> Triple(
            scheme.errorContainer, scheme.onErrorContainer, Icons.Default.Error
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
    onDocumentClick: () -> Unit
) {
    // Workflow #70 — eKYC khuôn mặt + Căn cước công dân. Bước sinh trắc học thiết bị đã gỡ.
    // CCCD mở bottom sheet với 2 phương thức thay thế: NFC hoặc tải giấy tờ.
    var showCccdSheet by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.identity_section_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.identity_document_alternative_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            IdentityTaskItem(
                title = stringResource(R.string.ekyc_step_face),
                isDone = status.isFaceVerified,
                onClick = onFaceClick
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            IdentityTaskItem(
                title = stringResource(R.string.identity_cta_cccd),
                isDone = status.isIdentityDocumentVerified,
                onClick = { showCccdSheet = true }
            )
        }
    }

    if (showCccdSheet) {
        CccdMethodBottomSheet(
            onDismiss = { showCccdSheet = false },
            onUploadClick = {
                showCccdSheet = false
                onDocumentClick()
            },
            onNfcClick = {
                showCccdSheet = false
                onNfcClick()
            }
        )
    }
}

/**
 * Workflow #40 — Bottom sheet cho CTA Căn cước công dân. NFC và tải giấy tờ là hai
 * phương thức thay thế nhau; chỉ cần một thành công. NFC bị disable nếu thiết bị
 * không hỗ trợ.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CccdMethodBottomSheet(
    onDismiss: () -> Unit,
    onUploadClick: () -> Unit,
    onNfcClick: () -> Unit
) {
    val context = LocalContext.current
    val isNfcSupported = remember { NfcAdapter.getDefaultAdapter(context) != null }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 16.dp)) {
            Text(
                text = stringResource(R.string.identity_cccd_sheet_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.identity_cccd_sheet_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            CccdMethodOption(
                icon = Icons.Default.UploadFile,
                title = stringResource(R.string.ekyc_step_docs),
                enabled = true,
                onClick = onUploadClick
            )
            Spacer(modifier = Modifier.height(8.dp))
            CccdMethodOption(
                icon = Icons.Default.Nfc,
                title = stringResource(R.string.ekyc_step_nfc),
                subtitle = if (!isNfcSupported) stringResource(R.string.nfc_status_unsupported) else null,
                enabled = isNfcSupported,
                onClick = onNfcClick
            )
        }
    }
}

@Composable
private fun CccdMethodOption(
    icon: ImageVector,
    title: String,
    enabled: Boolean,
    onClick: () -> Unit,
    subtitle: String? = null
) {
    val contentColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    OutlinedCard(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary else contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = contentColor
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            if (enabled) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
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
            tint = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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
                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
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
                Text(text = stringResource(if (isCompleted) R.string.action_edit else R.string.action_complete_now))
            }
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(text = value.ifBlank { stringResource(R.string.profile_value_not_updated) }, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}
