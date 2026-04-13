package com.example.easymoney.ui

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import com.example.easymoney.navigation.AppDestination
import com.example.easymoney.navigation.AppNavHost
import com.example.easymoney.navigation.rememberAppState
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.easymoney.ui.components.AppNavigationBar
import com.example.easymoney.ui.components.AppTopBarController
import com.example.easymoney.ui.components.HomeBottomBar
import com.example.easymoney.ui.components.LocalAppTopBarController
import com.example.easymoney.ui.components.TopBarMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val appState = rememberAppState()
    val navBackStackEntry by appState.navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val destination = appState.currentDestination()
    val canNavigateBack = appState.navController.previousBackStackEntry != null
    val topBarController = remember { AppTopBarController() }
    val loginGradientTopColor = MaterialTheme.colorScheme.primary
        .copy(alpha = if (isDarkTheme) 0.78f else 0.95f)
        .compositeOver(MaterialTheme.colorScheme.background)
    val topBarBackgroundColor = when (destination) {
        AppDestination.LoanFlow -> MaterialTheme.colorScheme.background
        AppDestination.Welcome -> MaterialTheme.colorScheme.background
        AppDestination.Login1,
        AppDestination.Register1 -> loginGradientTopColor
        else -> destination.topBarBackgroundColor ?: MaterialTheme.colorScheme.background
    }

    CompositionLocalProvider(LocalAppTopBarController provides topBarController) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (destination != AppDestination.Home) {
                    val topBarOverride = if (topBarController.ownerRoute == destination.route) {
                        topBarController.topBarOverride
                    } else {
                        null
                    }
                    val resolvedTopBarMode = topBarOverride?.topBarMode ?: destination.defaultTopBarMode

                    // Only render top bar if not HIDDEN
                    if (resolvedTopBarMode != TopBarMode.HIDDEN) {
                        val resolvedTitle = topBarOverride?.title ?: destination.title
                        val resolvedShowBack = topBarOverride?.showBackButton ?: (destination.showBackButton && canNavigateBack)
                        val resolvedShowHelp = topBarOverride?.showHelpButton ?: destination.showHelpButton
                        val resolvedOnBack = topBarOverride?.onBackClick ?: { appState.popBackStack(); Unit }
                        val resolvedOnHelp = topBarOverride?.onHelpClick ?: {
                            appState.navigateTo(AppDestination.PageGuide.createRoute(destination.guideXmlName))
                        }
                        val resolvedBackground = topBarOverride?.backgroundColor ?: topBarBackgroundColor
                        val resolvedContentColor = topBarOverride?.contentColor ?: (destination.topBarContentColor ?: Color.Unspecified)

                        AppNavigationBar(
                            title = resolvedTitle,
                            showBackButton = resolvedShowBack,
                            showHelpButton = resolvedShowHelp,
                            onBackClick = resolvedOnBack,
                            onHelpClick = resolvedOnHelp,
                            backgroundColor = resolvedBackground,
                            contentColor = resolvedContentColor,
                            topBarMode = resolvedTopBarMode
                        )
                    }
                }
            },
            bottomBar = {
                if (destination.isMainTab) {
                    HomeBottomBar(
                        currentRoute = currentRoute,
                        onNavigateToRoute = { route ->
                            appState.navController.navigate(route) {
                                popUpTo(AppDestination.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            },
            containerColor = topBarBackgroundColor
        ) { innerPadding ->
            AppNavHost(
                navController = appState.navController,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize()
            )
        }
    }
}


