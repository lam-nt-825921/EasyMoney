package com.example.easymoney.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun rememberAppState(
    navController: NavHostController = rememberNavController()
): AppState = AppState(navController)

@Stable
class AppState(
    val navController: NavHostController
) {
    @Composable
    fun currentDestination(): AppDestination {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val route = navBackStackEntry?.destination?.route
        return appDestinationFromRoute(route)
    }

    fun navigateTo(route: String) {
        navController.navigate(route)
    }

    fun navigateUp(): Boolean = navController.navigateUp()

    fun popBackStack(): Boolean = navController.popBackStack()
}

