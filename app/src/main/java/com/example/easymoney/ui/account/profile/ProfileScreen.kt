package com.example.easymoney.ui.account.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.easymoney.R
import com.example.easymoney.domain.model.ProfileVerificationStatus
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onVerifyIdentity: () -> Unit,
    viewModel: ProfileCompletionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.profile
    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.updateAvatar(it.toString()) }
    }
    val needsIdentity = profile.verificationStatus != ProfileVerificationStatus.VERIFIED ||
        !profile.identityStatus.isFaceVerified ||
        !profile.identityStatus.isIdentityDocumentVerified

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        ProfileHeader(
            avatarUri = profile.avatarUri,
            fullName = profile.personalInfo.fullName,
            phoneNumber = profile.personalInfo.phoneNumber,
            onAvatarClick = { avatarPicker.launch("image/*") },
            onEditClick = onEditProfile
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (needsIdentity) {
                IdentityPromptCard(onClick = onVerifyIdentity)
            }

            ProfileInfoGroup(title = stringResource(id = R.string.profile_section_basic)) {
                ProfileInfoItem(label = stringResource(id = R.string.profile_label_fullname), value = profile.personalInfo.fullName)
                ProfileInfoItem(label = stringResource(id = R.string.profile_label_phone), value = profile.personalInfo.phoneNumber)
                ProfileInfoItem(label = stringResource(id = R.string.profile_label_id_number), value = profile.personalInfo.nationalId)
                ProfileInfoItem(label = stringResource(id = R.string.profile_label_gender), value = profile.personalInfo.gender)
                ProfileInfoItem(label = stringResource(id = R.string.profile_label_dob), value = profile.personalInfo.dateOfBirth)
            }

            ProfileInfoGroup(title = stringResource(id = R.string.profile_section_address)) {
                ProfileInfoItem(label = stringResource(id = R.string.profile_label_permanent_address), value = profile.addressInfo.permanentAddress)
                ProfileInfoItem(label = stringResource(id = R.string.profile_label_current_address), value = profile.addressInfo.currentAddress)
            }

            ProfileInfoGroup(title = stringResource(id = R.string.profile_section_job_income)) {
                val income = NumberFormat.getNumberInstance(Locale.getDefault()).format(profile.jobInfo.monthlyIncome)
                ProfileInfoItem(label = stringResource(id = R.string.profile_label_job_title), value = profile.jobInfo.jobTitle)
                ProfileInfoItem(
                    label = stringResource(id = R.string.profile_label_income),
                    value = stringResource(
                        id = R.string.profile_income_value,
                        income,
                        stringResource(id = R.string.profile_unit_currency_short)
                    )
                )
                if (profile.jobInfo.jobTitle == "Nhân viên văn phòng công ty" || profile.jobInfo.companyName.isNotBlank()) {
                    ProfileInfoItem(label = stringResource(id = R.string.profile_label_company), value = profile.jobInfo.companyName)
                }
            }

            ProfileInfoGroup(title = stringResource(id = R.string.profile_section_contact)) {
                if (profile.contactInfo.contactName.isBlank() && profile.contactInfo.phoneNumber.isBlank()) {
                    ProfileInfoItem(label = "Tóm tắt", value = "Chưa thêm liên hệ")
                } else {
                    ProfileInfoItem(label = stringResource(id = R.string.profile_label_contact_name), value = profile.contactInfo.contactName)
                    ProfileInfoItem(label = stringResource(id = R.string.profile_label_relationship), value = profile.contactInfo.relationship)
                    ProfileInfoItem(label = stringResource(id = R.string.profile_label_phone), value = profile.contactInfo.phoneNumber)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileHeader(
    avatarUri: String,
    fullName: String,
    phoneNumber: String,
    onAvatarClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Surface(
                modifier = Modifier
                    .size(104.dp)
                    .clickable(onClick = onAvatarClick),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                if (avatarUri.isNotBlank()) {
                    AsyncImage(
                        model = avatarUri,
                        contentDescription = stringResource(id = R.string.profile_avatar_content_desc),
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = stringResource(id = R.string.profile_avatar_content_desc),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(34.dp)
                    .clickable(onClick = onAvatarClick),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 2.dp
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = stringResource(id = R.string.profile_update_avatar),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(7.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = fullName.ifBlank { stringResource(id = R.string.profile_value_not_updated) },
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = phoneNumber.ifBlank { stringResource(id = R.string.profile_value_not_updated) },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onEditClick) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(id = R.string.profile_edit_button))
        }
    }
}

@Composable
private fun IdentityPromptCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.profile_identity_prompt_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = R.string.profile_identity_prompt_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
private fun ProfileInfoGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun ProfileInfoItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        val displayValue = value.ifBlank { stringResource(id = R.string.profile_value_not_updated) }
        Text(
            text = displayValue,
            style = MaterialTheme.typography.bodyLarge,
            color = if (value.isBlank()) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            fontWeight = FontWeight.Medium
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 12.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
        )
    }
}
