package com.example.easymoney.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import com.example.easymoney.R
import com.example.easymoney.ui.home.components.GridSection
import com.example.easymoney.ui.home.components.HomeLoadingContent
import com.example.easymoney.ui.home.components.MainBanner
import com.example.easymoney.ui.home.components.WideBanner
import com.example.easymoney.ui.theme.EasyMoneyTheme

@Composable
fun HomeScreen(
    onLoanRegistrationClick: () -> Unit,
    onToggleSandbox: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    userName: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        HeaderSection(
            userName = userName,
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,
            onDevClick = onToggleSandbox
        )

        if (isLoading) {
            HomeLoadingContent()
        } else {
            MainBanner(onRegistrationClick = onLoanRegistrationClick)
            GridSection()
            WideBanner()
        }
    }
}

@Composable
private fun HeaderSection(
    userName: String,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onDevClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(id = R.string.home_welcome, userName),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Small Dev badge/button for sandbox
            TextButton(
                onClick = onDevClick,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.height(24.dp)
            ) {
                Text(
                    text = "Developer Mode",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
        }

        IconButton(onClick = onToggleTheme) {
            Icon(
                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = if (isDarkTheme) "Chuyển sang light mode" else "Chuyển sang dark mode"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    EasyMoneyTheme {
        HomeScreen(
            onLoanRegistrationClick = {},
            onToggleSandbox = {},
            isDarkTheme = false,
            onToggleTheme = {},
            userName = "Nguyen Duc Minh"
        )
    }
}
