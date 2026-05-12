package com.example.easymoney.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.easymoney.R
import com.example.easymoney.ui.theme.LocalDarkMode

private data class MenuItem(
    val icon: ImageVector,
    val labelRes: Int,
    val onClick: () -> Unit,
    val badge: String? = null
)


@Composable
fun AccountScreen(
    onLogout: () -> Unit,
    onNavigateToMoneyManagement: () -> Unit,
    onNavigateToPaymentCards: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToRewards: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    val accountMenuItems = listOf(
        MenuItem(Icons.Default.AccountBalanceWallet, R.string.account_money_management, onNavigateToMoneyManagement),
        MenuItem(Icons.Default.CreditCard, R.string.account_payment_cards, onNavigateToPaymentCards),
        MenuItem(Icons.Default.ReceiptLong, R.string.account_transaction_history, onNavigateToHistory),
    )

    val supportMenuItems = listOf(
        MenuItem(Icons.Default.HelpOutline, R.string.account_support_center, onNavigateToSupport),
        MenuItem(Icons.Default.Security, R.string.account_security, onNavigateToSecurity),
        MenuItem(Icons.Default.Settings, R.string.account_settings, onNavigateToSettings),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        UserProfileHeader(onClick = onNavigateToProfile)
        
        Spacer(modifier = Modifier.height(20.dp))
        
        PointsBanner(onNavigateToRewards)
        
        Spacer(modifier = Modifier.height(20.dp))
        
        MenuSection(title = stringResource(R.string.menu_section_account_cards), items = accountMenuItems)
        
        Spacer(modifier = Modifier.height(20.dp))
        
        MenuSection(title = stringResource(R.string.menu_section_support), items = supportMenuItems)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        LogoutButton(onLogout = onLogout)
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun UserProfileHeader(onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val isDarkMode = LocalDarkMode.current
    
    val gradient = if (isDarkMode) {
        Brush.verticalGradient(listOf(scheme.surfaceVariant, scheme.surface))
    } else {
        Brush.verticalGradient(listOf(scheme.primary, scheme.primary.copy(alpha = 0.8f)))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(gradient)
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = if (isDarkMode) scheme.primary else Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = "Nguyễn Lê Minh",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (isDarkMode) scheme.onSurface else Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = (if (isDarkMode) scheme.onSurfaceVariant else Color.White).copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "0987 654 321",
                        style = MaterialTheme.typography.bodyLarge,
                        color = (if (isDarkMode) scheme.onSurfaceVariant else Color.White).copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun PointsBanner(onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = scheme.secondaryContainer.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, scheme.primary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(scheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.home_reward_points),
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant
                )
                Text(
                    text = "1.250 ${stringResource(id = R.string.common_points_unit)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = scheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            TextButton(
                onClick = onClick,
                colors = ButtonDefaults.textButtonColors(contentColor = scheme.primary)
            ) {
                Text(
                    text = stringResource(id = R.string.home_redeem_gift),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MenuSection(title: String, items: List<MenuItem>) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = scheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = scheme.surface,
            tonalElevation = 1.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    MenuItemRow(item = item)
                    if (index < items.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = scheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuItemRow(item: MenuItem) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(scheme.primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(id = item.labelRes),
            style = MaterialTheme.typography.bodyLarge,
            color = scheme.onSurface,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        if (item.badge != null) {
            Surface(
                shape = CircleShape,
                color = scheme.error
            ) {
                Text(
                    text = item.badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onError,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = scheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    OutlinedButton(
        onClick = onLogout,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = scheme.error),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, scheme.error.copy(alpha = 0.5f))
    ) {
        Icon(
            imageVector = Icons.Default.Logout,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(id = R.string.account_logout),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
