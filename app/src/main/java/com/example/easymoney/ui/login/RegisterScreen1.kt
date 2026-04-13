package com.example.easymoney.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.easymoney.R
import com.example.easymoney.ui.theme.EasyMoneyTheme
import com.example.easymoney.ui.theme.LocalDarkMode

@Composable
fun RegisterScreen1(
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var userName by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var acceptedTerms by rememberSaveable { mutableStateOf(false) }

    val isDarkMode = LocalDarkMode.current
    val isPreviewMode = LocalInspectionMode.current
    val usernameLabel = if (isPreviewMode) "Tên đăng nhập" else stringResource(id = R.string.register_username_label)
    val usernamePlaceholder = if (isPreviewMode) "Nhập tên đăng nhập" else stringResource(id = R.string.register_username_placeholder)
    val phoneLabel = if (isPreviewMode) "Số điện thoại" else stringResource(id = R.string.register_phone_label)
    val phonePlaceholder = if (isPreviewMode) "Nhập số điện thoại" else stringResource(id = R.string.register_phone_placeholder)
    val passwordLabel = if (isPreviewMode) "Mật khẩu" else stringResource(id = R.string.register_password_label)
    val passwordPlaceholder = if (isPreviewMode) "Nhập mật khẩu" else stringResource(id = R.string.register_password_placeholder)
    val confirmPasswordLabel = if (isPreviewMode) "Nhập lại mật khẩu" else stringResource(id = R.string.register_confirm_password_label)
    val confirmPasswordPlaceholder = if (isPreviewMode) "Nhập lại mật khẩu" else stringResource(id = R.string.register_confirm_password_placeholder)
    val termsText = if (isPreviewMode) {
        "Tôi đã đọc, hiểu và đồng ý với toàn bộ nội dung điều khoản sử dụng dịch vụ của EasyMoney"
    } else {
        stringResource(id = R.string.register_terms_text)
    }
    val registerText = if (isPreviewMode) "Đăng ký" else stringResource(id = R.string.welcome_register)

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
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            RegisterTextField(
                label = usernameLabel,
                value = userName,
                onValueChange = { userName = it },
                placeholder = usernamePlaceholder
            )

            RegisterTextField(
                label = phoneLabel,
                value = phone,
                onValueChange = { phone = it },
                placeholder = phonePlaceholder
            )

            RegisterTextField(
                label = passwordLabel,
                value = password,
                onValueChange = { password = it },
                placeholder = passwordPlaceholder
            )

            RegisterTextField(
                label = confirmPasswordLabel,
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = confirmPasswordPlaceholder
            )

            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = acceptedTerms,
                    onCheckedChange = { acceptedTerms = it }
                )
                Text(
                    modifier = Modifier.weight(1f),
                    text = termsText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Button(
                onClick = onRegisterClick,
                enabled = acceptedTerms,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(top = 16.dp, bottom = 12.dp)
                    .height(52.dp)
            ) {
                Text(
                    text = registerText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun RegisterTextField(
    label: String,
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

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(text = placeholder) },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = fieldContainerColor,
                unfocusedContainerColor = fieldContainerColor,
                disabledContainerColor = fieldContainerColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
    }
}

@Preview(
    name = "Register1 - Light",
    widthDp = 392,
    heightDp = 824,
    showBackground = true
)
@Composable
private fun RegisterScreen1LightPreview() {
    EasyMoneyTheme {
        RegisterScreen1(onRegisterClick = {})
    }
}

@Preview(
    name = "Register1 - Dark",
    widthDp = 392,
    heightDp = 824,
    showBackground = true
)
@Composable
private fun RegisterScreen1DarkPreview() {
    EasyMoneyTheme(darkTheme = true) {
        RegisterScreen1(onRegisterClick = {})
    }
}





