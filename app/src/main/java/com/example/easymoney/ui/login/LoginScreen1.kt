package com.example.easymoney.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.easymoney.R
import com.example.easymoney.ui.theme.EasyMoneyTheme
import com.example.easymoney.ui.theme.LocalDarkMode

@Composable
fun LoginScreen1(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var account by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var rememberAccount by rememberSaveable { mutableStateOf(false) }

    val isDarkMode = LocalDarkMode.current
    val topGradientColor = MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkMode) 0.78f else 0.95f)
    val gradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to topGradientColor,
            0.08f to topGradientColor,
            0.24f to MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkMode) 0.48f else 0.70f),
            0.38f to MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkMode) 0.22f else 0.34f),
            0.48f to MaterialTheme.colorScheme.background
        )
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(180.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.login_account_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                LoginTextField(
                    value = account,
                    onValueChange = { account = it },
                    placeholder = stringResource(id = R.string.login_account_placeholder)
                )

                Text(
                    text = stringResource(id = R.string.login_password_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                LoginTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = stringResource(id = R.string.login_password_placeholder)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberAccount,
                        onCheckedChange = { rememberAccount = it }
                    )
                    Text(
                        text = stringResource(id = R.string.login_remember_account),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.welcome_login),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                TextButton(
                    onClick = onRegisterClick,
                    modifier = Modifier.fillMaxWidth()
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

@Composable
private fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val isDarkMode = LocalDarkMode.current
    val fieldContainerColor = if (isDarkMode) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
    } else {
        Color(0xFFD4DCE4)
    }

    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = placeholder) },
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = fieldContainerColor,
            unfocusedContainerColor = fieldContainerColor,
            disabledContainerColor = fieldContainerColor,
            focusedIndicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0f),
            unfocusedIndicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0f),
            disabledIndicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0f)
        )
    )
}

@Preview(
    name = "Login1 - Light",
    showSystemUi = true,
    showBackground = true
)
@Composable
private fun LoginScreen1LightPreview() {
    EasyMoneyTheme {
        LoginScreen1(
            onLoginClick = {},
            onRegisterClick = {}
        )
    }
}

@Preview(
    name = "Login1 - Dark",
    showSystemUi = true,
    showBackground = true
)
@Composable
private fun LoginScreen1DarkPreview() {
    EasyMoneyTheme(darkTheme = true) {
        LoginScreen1(
            onLoginClick = {},
            onRegisterClick = {}
        )
    }
}




