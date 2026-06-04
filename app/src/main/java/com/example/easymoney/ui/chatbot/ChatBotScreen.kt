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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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
    val typingListState = rememberLazyListState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var isInputFocused by remember { mutableStateOf(false) }
    val greeting = stringResource(R.string.chatbot_greeting)
    val hideKeyboard = {
        isInputFocused = false
        focusManager.clearFocus()
        keyboardController?.hide()
        Unit
    }

    LaunchedEffect(Unit) { viewModel.greetIfNeeded(greeting) }

    if (isInputFocused) {
        val typingMessages = uiState.messages.latestConversationTurn()
        LaunchedEffect(typingMessages.size, uiState.isThinking, uiState.errorMessage) {
            if (typingMessages.isNotEmpty()) {
                typingListState.animateScrollToItem(typingMessages.size - 1)
            }
        }
        FocusedChatLayout(
            messages = typingMessages,
            isThinking = uiState.isThinking,
            errorMessage = uiState.errorMessage,
            listState = typingListState,
            input = uiState.input,
            onInputChange = viewModel::onInputChange,
            onSend = viewModel::onSend,
            onInputFocused = { isInputFocused = true },
            onAction = { target ->
                when (target) {
                    is ChatActionTarget.NavigateRoute -> onNavigateRoute(target.route)
                    is ChatActionTarget.DialPhone -> LinkHandler.dial(context, target.phone)
                }
            },
            onDismissKeyboard = hideKeyboard
        )
    } else {
        LaunchedEffect(uiState.messages.size) {
            if (uiState.messages.isNotEmpty()) {
                listState.animateScrollToItem(uiState.messages.size - 1)
            }
        }
        DefaultChatLayout(
            messages = uiState.messages,
            isThinking = uiState.isThinking,
            errorMessage = uiState.errorMessage,
            listState = listState,
            input = uiState.input,
            onInputChange = viewModel::onInputChange,
            onSend = viewModel::onSend,
            onInputFocused = { isInputFocused = true },
            onAction = { target ->
                when (target) {
                    is ChatActionTarget.NavigateRoute -> onNavigateRoute(target.route)
                    is ChatActionTarget.DialPhone -> LinkHandler.dial(context, target.phone)
                }
            },
            onDismissKeyboard = hideKeyboard
        )
    }
}

@Composable
private fun DefaultChatLayout(
    messages: List<ChatMessage>,
    isThinking: Boolean,
    errorMessage: String?,
    listState: androidx.compose.foundation.lazy.LazyListState,
    input: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onInputFocused: () -> Unit,
    onAction: (ChatActionTarget) -> Unit,
    onDismissKeyboard: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        ChatMessageList(
            messages = messages,
            isThinking = isThinking,
            errorMessage = errorMessage,
            listState = listState,
            onAction = onAction,
            onDismissKeyboard = onDismissKeyboard,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        )

        InputRow(
            input = input,
            onChange = onInputChange,
            onSend = onSend,
            onFocused = onInputFocused,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}

@Composable
private fun FocusedChatLayout(
    messages: List<ChatMessage>,
    isThinking: Boolean,
    errorMessage: String?,
    listState: androidx.compose.foundation.lazy.LazyListState,
    input: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onInputFocused: () -> Unit,
    onAction: (ChatActionTarget) -> Unit,
    onDismissKeyboard: () -> Unit
) {
    val inputFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        inputFocusRequester.requestFocus()
        keyboardController?.show()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ChatMessageList(
            messages = messages,
            isThinking = isThinking,
            errorMessage = errorMessage,
            listState = listState,
            onAction = onAction,
            onDismissKeyboard = onDismissKeyboard,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Bottom)
        )

        InputRow(
            input = input,
            onChange = onInputChange,
            onSend = onSend,
            onFocused = onInputFocused,
            focusRequester = inputFocusRequester,
            modifier = Modifier.navigationBarsPadding()
        )
    }
}

@Composable
private fun ChatMessageList(
    messages: List<ChatMessage>,
    isThinking: Boolean,
    errorMessage: String?,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onAction: (ChatActionTarget) -> Unit,
    onDismissKeyboard: () -> Unit,
    modifier: Modifier,
    contentPadding: PaddingValues,
    verticalArrangement: Arrangement.Vertical
) {
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onDismissKeyboard() })
            }
            .padding(horizontal = 12.dp),
        verticalArrangement = verticalArrangement,
        contentPadding = contentPadding
    ) {
        items(messages, key = { it.id }) { msg ->
            MessageRow(msg, onAction = onAction)
        }
        if (isThinking) {
            item {
                Text(stringResource(R.string.chatbot_typing), style = MaterialTheme.typography.bodySmall)
            }
        }
        errorMessage?.let { err ->
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
}

private fun List<ChatMessage>.latestConversationTurn(): List<ChatMessage> {
    if (isEmpty()) return emptyList()
    val latestBotIndex = indexOfLast { it.role == ChatRole.BOT }
    val anchorIndex = if (latestBotIndex >= 0) latestBotIndex else lastIndex
    val previousUserIndex = subList(0, anchorIndex + 1).indexOfLast { it.role == ChatRole.USER }
    val startIndex = previousUserIndex.takeIf { it >= 0 } ?: anchorIndex
    return drop(startIndex)
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
private fun InputRow(
    input: String,
    onChange: (String) -> Unit,
    onSend: () -> Unit,
    onFocused: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null
) {
    Surface(tonalElevation = 2.dp, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = onChange,
                placeholder = { Text(stringResource(R.string.chatbot_placeholder)) },
                modifier = Modifier
                    .weight(1f)
                    .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                    .onFocusChanged { if (it.isFocused) onFocused() },
                singleLine = true
            )
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onSend) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.chatbot_send))
            }
        }
    }
}
