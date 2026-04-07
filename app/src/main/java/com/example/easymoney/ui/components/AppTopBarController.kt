package com.example.easymoney.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppTopBarOverride(
    val title: String? = null,
    val showBackButton: Boolean? = null,
    val showHelpButton: Boolean? = null,
    val onBackClick: (() -> Unit)? = null,
    val onHelpClick: (() -> Unit)? = null,
    val backgroundColor: Color? = null,
    val contentColor: Color? = null,
    val topBarMode: TopBarMode? = null,
    val systemBarMode: SystemBarMode? = null,
    val screenColorMode: ScreenColorMode? = null
)

@Stable
class AppTopBarController {
    var ownerRoute: String? by mutableStateOf(null)
        private set

    var topBarOverride: AppTopBarOverride? by mutableStateOf(null)
        private set

    fun setOverride(ownerRoute: String, override: AppTopBarOverride?) {
        this.ownerRoute = if (override == null) null else ownerRoute
        topBarOverride = override
    }

    fun clearOverride(ownerRoute: String) {
        if (this.ownerRoute == ownerRoute) {
            this.ownerRoute = null
            topBarOverride = null
        }
    }

    fun setOnBackOverride(callback: (() -> Unit)?) {
        topBarOverride = if (callback == null) {
            null
        } else {
            AppTopBarOverride(onBackClick = callback)
        }
    }
}

val LocalAppTopBarController = staticCompositionLocalOf<AppTopBarController?> { null }

@Composable
fun RegisterTopBarBackOverride(
    ownerRoute: String,
    onBack: (() -> Unit)?
) {
    RegisterTopBarOverride(
        ownerRoute = ownerRoute,
        override = if (onBack == null) null else AppTopBarOverride(onBackClick = onBack)
    )
}

@Composable
fun RegisterTopBarOverride(
    ownerRoute: String,
    override: AppTopBarOverride?
) {
    val controller = LocalAppTopBarController.current

    DisposableEffect(controller, ownerRoute, override) {
        controller?.setOverride(ownerRoute, override)
        onDispose { controller?.clearOverride(ownerRoute) }
    }
}

