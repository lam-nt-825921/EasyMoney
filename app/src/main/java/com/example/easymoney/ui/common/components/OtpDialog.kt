package com.example.easymoney.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

@Composable
fun OtpDialog(
    phoneNumber: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onResendOtp: () -> Unit,
    onMaxAttemptsReached: () -> Unit,
    isVerifying: Boolean = false,
    errorMessage: String? = null,
    maxAttempts: Int = 3
) {
    var otpValue by remember { mutableStateOf("") }
    var timeLeft by remember { mutableIntStateOf(60) }
    var attempts by remember { mutableIntStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(true) }
    val focusRequester = remember { FocusRequester() }

    // Timer logic
    LaunchedEffect(isTimerRunning, timeLeft) {
        if (isTimerRunning && timeLeft > 0) {
            delay(1000L)
            timeLeft--
        } else if (timeLeft == 0) {
            isTimerRunning = false
        }
    }

    // Effect to track error attempts
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            attempts++
            if (attempts >= maxAttempts) {
                onMaxAttemptsReached()
            }
        }
    }

    // Auto focus on start
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Xác thực OTP",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                val description = buildAnnotatedString {
                    append("Vui lòng nhập mã OTP được gửi về SĐT ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(phoneNumber)
                    }
                    append(" để ký hợp đồng")
                }

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(6) { index ->
                            val char = when {
                                index < otpValue.length -> otpValue[index].toString()
                                else -> ""
                            }
                            val isFocused = otpValue.length == index

                            OtpCell(
                                value = char,
                                isFocused = isFocused,
                                isError = errorMessage != null
                            )
                        }
                    }

                    // Hidden BasicTextField overlaying the Row
                    BasicTextField(
                        value = otpValue,
                        onValueChange = {
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                otpValue = it
                            }
                        },
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .matchParentSize() // Occupy the same area as Row
                            .alpha(0f), // Invisible but has size
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Timer / Resend Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (timeLeft > 0) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Gửi lại sau ${timeLeft}s",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    } else {
                        TextButton(
                            onClick = {
                                timeLeft = 60
                                isTimerRunning = true
                                otpValue = ""
                                onResendOtp()
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Gửi lại",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Gửi lại mã OTP",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Mã OTP không đúng ($attempts/$maxAttempts lần)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Divider(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Text(
                            text = "Bỏ qua",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(56.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    TextButton(
                        onClick = { onConfirm(otpValue) },
                        enabled = otpValue.length == 6 && !isVerifying,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = "Xác nhận",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (otpValue.length == 6) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OtpCell(
    value: String,
    isFocused: Boolean,
    isError: Boolean
) {
    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        isFocused -> MaterialTheme.colorScheme.primary
        value.isNotEmpty() -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val borderWidth = if (isFocused || isError) 2.dp else 1.dp

    Box(
        modifier = Modifier
            .size(width = 42.dp, height = 50.dp)
            .background(
                color = if (isFocused) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )

        if (value.isEmpty() && isFocused) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(20.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
