package com.example.easymoney.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.easymoney.R

@Composable
fun HomeBottomBar(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = Color.White,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text(stringResource(id = R.string.home_nav_home)) },
            selected = currentRoute == "home",
            onClick = { onNavigateToRoute("home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFD32F2F), // Approximate red from image
                selectedTextColor = Color(0xFFD32F2F),
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
            label = { Text(stringResource(id = R.string.home_nav_history)) },
            selected = currentRoute == "history",
            onClick = { /* Navigate to history */ },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.NotificationsActive, contentDescription = null) },
            label = { Text(stringResource(id = R.string.home_nav_notify)) },
            selected = currentRoute == "notifications",
            onClick = { /* Navigate to notifications */ },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
            label = { Text(stringResource(id = R.string.home_nav_account)) },
            selected = currentRoute == "account",
            onClick = { /* Navigate to account */ },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )
    }
}
