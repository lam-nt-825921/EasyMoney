package com.example.easymoney.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.easymoney.R
import com.example.easymoney.ui.home.components.GridSection
import com.example.easymoney.ui.home.components.MainBanner
import com.example.easymoney.ui.home.components.WideBanner
import com.example.easymoney.ui.theme.EasyMoneyTheme

@Composable
fun HomeScreen(
    onLoanRegistrationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        HeaderSection(userName = "NGUYỄN LÊ MINH")
        
        MainBanner(onRegistrationClick = onLoanRegistrationClick)
        
        GridSection()
        
        WideBanner()
    }
}

@Composable
private fun HeaderSection(userName: String) {
    Text(
        text = stringResource(id = R.string.home_welcome, userName),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    EasyMoneyTheme {
        HomeScreen(onLoanRegistrationClick = {})
    }
}
