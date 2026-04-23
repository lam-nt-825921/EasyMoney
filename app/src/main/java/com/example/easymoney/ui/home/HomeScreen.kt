package com.example.easymoney.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
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
            .padding(top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(id = R.string.home_welcome, userName),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(6.dp))

            // Professional Badge for Developer Mode
            Surface(
                onClick = onDevClick,
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Text(
                    text = stringResource(id = R.string.home_developer_mode),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        IconButton(
            onClick = onToggleTheme,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = stringResource(
                    id = if (isDarkTheme) R.string.home_theme_light else R.string.home_theme_dark
                ),
                modifier = Modifier.size(24.dp)
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
