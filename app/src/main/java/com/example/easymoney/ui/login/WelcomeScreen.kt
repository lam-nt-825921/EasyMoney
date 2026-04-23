package com.example.easymoney.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.easymoney.R
import com.example.easymoney.ui.theme.EasyMoneyTheme
import com.example.easymoney.ui.theme.LocalDarkMode

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkMode = LocalDarkMode.current
    val illustrationRes = if (isDarkMode) R.drawable.main_dark else R.drawable.main

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Image(
                painter = painterResource(id = illustrationRes),
                contentDescription = stringResource(id = R.string.welcome_illustration_content_desc),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.7f)
                    .padding(top = 40.dp),
                contentScale = ContentScale.Fit
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.welcome_login),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    onClick = onRegisterClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.welcome_register),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Preview(
    name = "Welcome - Light",
    showSystemUi = true,
    showBackground = true
)
@Composable
private fun WelcomeScreenLightPreview() {
    EasyMoneyTheme {
        WelcomeScreen(
            onLoginClick = {},
            onRegisterClick = {}
        )
    }
}

@Preview(
    name = "Welcome - Dark",
    showSystemUi = true,
    showBackground = true
)
@Composable
private fun WelcomeScreenDarkPreview() {
    EasyMoneyTheme(darkTheme = true) {
        WelcomeScreen(
            onLoginClick = {},
            onRegisterClick = {}
        )
    }
}









