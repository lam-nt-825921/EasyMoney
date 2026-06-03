package com.example.easymoney.ui.chatbot

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.utils.LinkHandler
import com.example.easymoney.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBotScreen(
    viewModel: ChatBotViewModel = hiltViewModel(),
    onNavigateRoute: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val greeting = stringResource(R.string.chatbot_greeting)
    val hideKeyboard = {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    LaunchedEffect(Unit) { viewModel.greetIfNeeded(greeting) }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { hideKeyboard() })
                }
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(uiState.messages, key = { it.id }) { msg ->
                MessageRow(msg, onAction = { target ->
                    when (target) {
                        is ChatActionTarget.NavigateRoute -> onNavigateRoute(target.route)
                        is ChatActionTarget.DialPhone -> LinkHandler.dial(context, target.phone)
                    }
                })
            }
            if (uiState.isThinking) {
                item {
                    Text(stringResource(R.string.chatbot_typing), style = MaterialTheme.typography.bodySmall)
                }
            }
            uiState.errorMessage?.let { err ->
                item {
                    // Workflow #33 — surface chatbot error inline.
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        InputRow(
            input = uiState.input,
            onChange = viewModel::onInputChange,
            onSend = viewModel::onSend
        )
    }
}

@Composable
private fun MessageRow(msg: ChatMessage, onAction: (ChatActionTarget) -> Unit) {
    val isUser = msg.role == ChatRole.USER
    // Workflow #92 — user messages render as a clearly distinct right-aligned bubble; bot messages
    // stay left-aligned in a container surface with their existing card/action structure.
    val bubbleColor = if (isUser) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (isUser) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onPrimaryContainer
    val bubbleShape = if (isUser) {
        // Sharper corner on the speaker (bottom-end) side.
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }
    // User bubble wraps content up to a max width; bot keeps its wider fixed fraction for cards.
    val bubbleModifier = if (isUser) Modifier.widthIn(max = 300.dp) else Modifier.fillMaxWidth(0.85f)

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start) {
        Surface(
            shape = bubbleShape,
            color = bubbleColor,
            contentColor = contentColor,
            modifier = bubbleModifier
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                when (msg) {
                    is ChatMessage.Text -> Text(msg.content, color = contentColor)
                    is ChatMessage.Action -> Button(onClick = { onAction(msg.target) }) {
                        Text(msg.label)
                    }
                    is ChatMessage.Card -> {
                        Text(msg.title, style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(4.dp))
                        Text(msg.body, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        msg.actions.forEach { action ->
                            Button(onClick = { onAction(action.target) }, modifier = Modifier.fillMaxWidth()) {
                                Text(action.label)
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InputRow(input: String, onChange: (String) -> Unit, onSend: () -> Unit) {
    Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = onChange,
                placeholder = { Text(stringResource(R.string.chatbot_placeholder)) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onSend) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.chatbot_send))
            }
        }
    }
}
