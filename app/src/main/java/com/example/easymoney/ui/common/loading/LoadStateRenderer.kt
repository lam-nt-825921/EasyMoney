package com.example.easymoney.ui.common.loading

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LoadStateRenderer(
    state: UiLoadState,
    modifier: Modifier = Modifier,
    initialLoading: @Composable (Modifier) -> Unit,
    loading: @Composable (Modifier) -> Unit = initialLoading,
    content: @Composable (Modifier) -> Unit,
    error: (@Composable (message: String?, modifier: Modifier) -> Unit)? = null
) {
    when (state) {
        UiLoadState.Idle -> content(modifier)
        UiLoadState.InitialLoading -> initialLoading(modifier)
        UiLoadState.Refreshing,
        UiLoadState.Submitting -> loading(modifier)

        is UiLoadState.Error -> {
            if (error != null) {
                error(state.message, modifier)
            } else {
                content(modifier)
            }
        }
    }
}

