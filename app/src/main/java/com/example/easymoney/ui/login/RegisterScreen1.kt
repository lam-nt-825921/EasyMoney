package com.example.easymoney.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.easymoney.R
import com.example.easymoney.ui.account.profile.ProfileInputValidator
import com.example.easymoney.ui.components.AppTextField
import com.example.easymoney.ui.theme.EasyMoneyTheme
import com.example.easymoney.ui.theme.LocalDarkMode

@Composable
fun RegisterScreen1(
    onRegisterClick: (String, String, String) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var acceptedTerms by rememberSaveable { mutableStateOf(false) }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    val normalizedPhone = ProfileInputValidator.normalizePhone(phone)
    val phoneError = when {
        phone.isBlank() || ProfileInputValidator.validatePhone(phone) == null -> null
        else -> stringResource(R.string.error_invalid_phone)
    }
    val fullNameError = when {
        fullName.isBlank() || ProfileInputValidator.validateName(fullName) == null -> null
        else -> stringResource(R.string.profile_error_name_invalid)
    }
    val canRegister = acceptedTerms &&
        fullName.isNotBlank() &&
        ProfileInputValidator.validateName(fullName) == null &&
        password == confirmPassword &&
        password.isNotBlank() &&
        ProfileInputValidator.validatePhone(phone) == null

    val isDarkMode = LocalDarkMode.current
    val scheme = MaterialTheme.colorScheme

    val topGradientColor = scheme.primary.copy(alpha = if (isDarkMode) 0.7f else 0.9f)
    val gradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to topGradientColor,
            0.1f to topGradientColor,
            0.25f to scheme.primary.copy(alpha = if (isDarkMode) 0.15f else 0.35f),
            0.45f to scheme.background
        )
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = scheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            // High contrast title
            Text(
                text = stringResource(id = R.string.welcome_register),
                style = MaterialTheme.typography.headlineMedium,
                color = if (isDarkMode) scheme.onBackground else scheme.primary,
                fontWeight = FontWeight.ExtraBold
            )

            Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
                RegisterField(
                    label = stringResource(id = R.string.register_username_label),
                    value = fullName,
                    onValueChange = { fullName = it },
                    inputSanitizer = { it.filter { char -> char.isLetter() || char.isWhitespace() }.take(50) },
                    placeholder = stringResource(id = R.string.register_username_placeholder),
                    error = if (fullName.isNotBlank()) fullNameError else null
                )

                RegisterField(
                    label = stringResource(id = R.string.register_phone_label),
                    value = phone,
                    onValueChange = { phone = it },
                    inputSanitizer = { it.filter(Char::isDigit).take(10) },
                    placeholder = stringResource(id = R.string.register_phone_placeholder),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    error = if (phone.isNotBlank()) phoneError else null
                )

                RegisterField(
                    label = stringResource(id = R.string.register_password_label),
                    value = password,
                    onValueChange = { password = it },
                    inputSanitizer = { it.take(12) },
                    placeholder = stringResource(id = R.string.register_password_placeholder),
                    isPassword = true,
                    showPassword = showPassword,
                    onTogglePassword = { showPassword = !showPassword }
                )

                RegisterField(
                    label = stringResource(id = R.string.register_confirm_password_label),
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    inputSanitizer = { it.take(12) },
                    placeholder = stringResource(id = R.string.register_confirm_password_placeholder),
                    isPassword = true,
                    showPassword = showPassword,
                    onTogglePassword = { showPassword = !showPassword }
                )
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = scheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = acceptedTerms,
                    onCheckedChange = { acceptedTerms = it },
                    colors = CheckboxDefaults.colors(checkedColor = scheme.primary)
                )
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = R.string.register_terms_text),
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onBackground,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = {
                    if (canRegister) onRegisterClick(normalizedPhone, fullName.trim(), password)
                },
                enabled = !isLoading && canRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(top = 16.dp, bottom = 40.dp)
                    .height(58.dp),
                shape = RoundedCornerShape(29.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = scheme.primary,
                    contentColor = scheme.onPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(26.dp),
                        color = scheme.onPrimary,
                        strokeWidth = 3.dp
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.welcome_register),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun RegisterField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onTogglePassword: () -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    error: String? = null,
    inputSanitizer: (String) -> String = { it }
) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            color = scheme.onBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp)
        )
        AppTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            isPassword = isPassword,
            showPassword = showPassword,
            onTogglePassword = onTogglePassword,
            keyboardOptions = keyboardOptions,
            inputSanitizer = inputSanitizer
        )
        if (!error.isNullOrBlank()) {
            Text(
                text = error,
                color = scheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Preview(name = "Register1 - Light", showBackground = true)
@Composable
private fun RegisterScreen1LightPreview() {
    EasyMoneyTheme {
        RegisterScreen1(onRegisterClick = { _, _, _ -> })
    }
}
