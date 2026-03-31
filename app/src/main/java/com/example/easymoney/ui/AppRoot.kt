package com.example.easymoney.ui

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.easymoney.navigation.AppDestination
import com.example.easymoney.navigation.AppNavHost
import com.example.easymoney.navigation.rememberAppState
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.easymoney.ui.components.AppNavigationBar
import com.example.easymoney.ui.components.HomeBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot() {
    val appState = rememberAppState()
    val navBackStackEntry by appState.navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val destination = appState.currentDestination()
    val canNavigateBack = appState.navController.previousBackStackEntry != null
    val topBarBackgroundColor = destination.topBarBackgroundColor ?: MaterialTheme.colorScheme.background

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (destination != AppDestination.Home) {
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
            }
        },
        bottomBar = {
            if (destination == AppDestination.Home) {
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
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
        )
    }
}


