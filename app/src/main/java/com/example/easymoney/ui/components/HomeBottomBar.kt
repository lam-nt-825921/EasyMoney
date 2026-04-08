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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.easymoney.R
import com.example.easymoney.ui.theme.TealPrimary

private val unselectedColor = Color(0xFF98A2B3)

@Composable
fun HomeBottomBar(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = Color.White,
        contentColor = TealPrimary
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
            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
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
    selectedIconColor = TealPrimary,
    selectedTextColor = TealPrimary,
    unselectedIconColor = unselectedColor,
    unselectedTextColor = unselectedColor,
    indicatorColor = Color(0xFFE8F4F6) // TealSecondary as subtle indicator
)
