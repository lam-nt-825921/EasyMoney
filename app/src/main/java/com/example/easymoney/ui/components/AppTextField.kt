package com.example.easymoney.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.easymoney.R
import com.example.easymoney.ui.theme.LocalDarkMode

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onTogglePassword: () -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    inputSanitizer: (String) -> String = { it }
) {
    val isDarkMode = LocalDarkMode.current
    val fieldContainerColor = if (isDarkMode) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
    } else {
        // Light grey for better visibility on white/gradient backgrounds
        Color(0xFFF1F4F9)
    }
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(value, selection = TextRange(value.length)))
    }

    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            textFieldValue = TextFieldValue(value, selection = TextRange(value.length))
        }
    }

    TextField(
        value = textFieldValue,
        onValueChange = { candidate ->
            val sanitized = inputSanitizer(candidate.text)
            val selection = if (sanitized == candidate.text) {
                candidate.selection
            } else {
                TextRange(sanitized.length)
            }
            textFieldValue = TextFieldValue(sanitized, selection = selection)
            onValueChange(sanitized)
        },
        placeholder = { 
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge
            ) 
        },
        shape = RoundedCornerShape(16.dp),
        singleLine = singleLine,
        visualTransformation = if (isPassword && !showPassword) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = stringResource(R.string.field_toggle_password_visibility)
                    )
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
        textStyle = MaterialTheme.typography.bodyLarge,
        keyboardOptions = keyboardOptions,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = fieldContainerColor,
            unfocusedContainerColor = fieldContainerColor,
            disabledContainerColor = fieldContainerColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    )
}
