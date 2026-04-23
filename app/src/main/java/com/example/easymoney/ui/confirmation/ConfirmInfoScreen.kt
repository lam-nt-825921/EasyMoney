package com.example.easymoney.ui.confirmation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.easymoney.domain.repository.LoanRepositoryImpl
import com.example.easymoney.ui.theme.EasyMoneyTheme

@Composable
fun ConfirmInfoScreen(
    viewModel: ConfirmInfoViewModel,
    onContinue: () -> Unit,
    onEditInfo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    ConfirmInfoContent(
        uiState = uiState,
        onContinueClick = onContinue,
        onEditInfoClick = onEditInfo,
        modifier = modifier
    )
}

@Composable
private fun ConfirmInfoContent(
    uiState: ConfirmInfoUiState,
    onContinueClick: () -> Unit,
    onEditInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLoading = uiState.loadState !in listOf(ConfirmInfoLoadState.Success)
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            ConfirmInfoBottomBar(
                uiState = uiState,
                onContinueClick = onContinueClick,
                onEditInfoClick = onEditInfoClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Spacer to fix "shifted upwards" issue
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = uiState.sectionTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                if (isLoading) {
                    ConfirmInfoLoadingBody()
                } else {
                    when (uiState.loadState) {
                        is ConfirmInfoLoadState.Error -> {
                            Text(
                                text = (uiState.loadState as ConfirmInfoLoadState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(20.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        else -> {
                            ConfirmInfoDataBody(uiState = uiState)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
private fun ConfirmInfoLoadingBody() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ConfirmInfoDataBody(uiState: ConfirmInfoUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        InfoRow(label = "Họ và tên", value = uiState.userInfo?.fullName ?: "N/A")
        InfoRow(label = "Giới tính", value = uiState.userInfo?.gender ?: "N/A")
        InfoRow(label = "Ngày sinh", value = uiState.userInfo?.dateOfBirth ?: "N/A")
        InfoRow(label = "Số điện thoại", value = uiState.userInfo?.phoneNumber ?: "N/A")
        InfoRow(label = "CMND/CCCD", value = uiState.userInfo?.nationalId ?: "N/A")
        InfoRow(label = "Ngày cấp", value = uiState.userInfo?.issueDate ?: "N/A")
    }
}


@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ConfirmInfoBottomBar(
    uiState: ConfirmInfoUiState,
    onContinueClick: () -> Unit,
    onEditInfoClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onContinueClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = uiState.continueButtonText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = uiState.editInfoText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable(onClick = onEditInfoClick)
                .padding(vertical = 4.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Confirm Info")
@Composable
private fun ConfirmInfoScreenPreview() {
    val viewModel = remember { ConfirmInfoViewModel(LoanRepositoryImpl(null, null, null)) }
    EasyMoneyTheme {
        ConfirmInfoScreen(
            viewModel = viewModel,
            onContinue = {},
            onEditInfo = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Confirm Info Dark")
@Composable
private fun ConfirmInfoScreenDarkPreview() {
    val viewModel = remember { ConfirmInfoViewModel(LoanRepositoryImpl(null, null, null)) }
    EasyMoneyTheme(darkTheme = true) {
        ConfirmInfoScreen(
            viewModel = viewModel,
            onContinue = {},
            onEditInfo = {}
        )
    }
}

