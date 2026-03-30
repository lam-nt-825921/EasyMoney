package com.example.easymoney.ui

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.easymoney.navigation.AppDestination
import com.example.easymoney.navigation.AppNavHost
import com.example.easymoney.navigation.rememberAppState
import com.example.easymoney.ui.components.AppNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot() {
    val appState = rememberAppState()
    val destination = appState.currentDestination()
    val canNavigateBack = appState.navController.previousBackStackEntry != null
    val topBarBackgroundColor = destination.topBarBackgroundColor ?: MaterialTheme.colorScheme.background

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppNavigationBar(
                title = destination.title,
                showBackButton = destination.showBackButton && canNavigateBack,
                showHelpButton = destination.showHelpButton,
                onBackClick = { appState.popBackStack() },
                onHelpClick = {
                    appState.navigateTo(AppDestination.PageGuide.createRoute(destination.guideXmlName))
                },
                backgroundColor = topBarBackgroundColor,
                contentColor = destination.topBarContentColor ?: Color.Unspecified
            )
        },
        containerColor = topBarBackgroundColor
    ) { innerPadding ->
        AppNavHost(
            navController = appState.navController,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
        )
    }
}


