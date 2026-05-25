package com.example.easymoney.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.easymoney.R

@Composable
fun HomeBottomBar(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = {
                Text(
                    text = stringResource(id = R.string.home_nav_home),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 10.sp
                )
            },
            selected = currentRoute == "home",
            onClick = { onNavigateToRoute("home") },
            colors = navItemColors()
        )
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null) },
            label = {
                Text(
                    text = stringResource(id = R.string.home_nav_history_short),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 10.sp
                )
            },
            selected = currentRoute == "history",
            onClick = { onNavigateToRoute("history") },
            colors = navItemColors()
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.NotificationsActive, contentDescription = null) },
            label = {
                Text(
                    text = stringResource(id = R.string.home_nav_notify),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 10.sp
                )
            },
            selected = currentRoute == "notifications",
            onClick = { onNavigateToRoute("notifications") },
            colors = navItemColors()
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
            label = {
                Text(
                    text = stringResource(id = R.string.home_nav_account),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 10.sp
                )
            },
            selected = currentRoute == "account",
            onClick = { onNavigateToRoute("account") },
            colors = navItemColors()
        )
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.primary,
    selectedTextColor = MaterialTheme.colorScheme.primary,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    indicatorColor = MaterialTheme.colorScheme.secondary
)
