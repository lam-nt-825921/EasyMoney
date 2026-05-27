package com.example.easymoney.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import com.example.easymoney.data.local.AppPreferences
import com.example.easymoney.ui.account.AccountScreen
import com.example.easymoney.ui.account.GeneralSettingsScreen
import com.example.easymoney.ui.account.profile.ProfileCompletionScreen
import com.example.easymoney.ui.account.profile.ProfileScreen
import com.example.easymoney.ui.account.profile.EditPersonalInfoScreen
import com.example.easymoney.ui.account.profile.EditJobInfoScreen
import com.example.easymoney.ui.account.profile.EditContactInfoScreen
import com.example.easymoney.ui.confirmation.ConfirmInfoScreen
import com.example.easymoney.ui.confirmation.ConfirmInfoViewModel
import com.example.easymoney.ui.guide.PageGuideScreen
import com.example.easymoney.ui.history.TransactionHistoryScreen
import com.example.easymoney.ui.home.HomeScreen
import com.example.easymoney.ui.home.HomeViewModel
import com.example.easymoney.ui.home.EventDetailScreen
import com.example.easymoney.ui.login.*
import com.example.easymoney.ui.loan.flow.LoanFlowScreen
import com.example.easymoney.ui.loan.discovery.LoanListScreen
import com.example.easymoney.ui.loan.discovery.LoanDetailScreen
import com.example.easymoney.ui.reward.RewardScreen
import com.example.easymoney.ui.notification.NotificationScreen
import com.example.easymoney.ui.onboarding.OnboardingScreen
import com.example.easymoney.ui.sandbox.SandBoxScreen
import com.example.easymoney.ui.esign.ContractScreen
import com.example.easymoney.ui.esign.EsignSuccessScreen
import com.example.easymoney.ui.payment.MoneyManagementScreen
import com.example.easymoney.ui.payment.PaymentCardsScreen
import com.example.easymoney.ui.account.changepassword.ChangePasswordScreen
import com.example.easymoney.ui.security.SecuritySettingsScreen
import com.example.easymoney.ui.web.WebContentScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    appNotificationsEnabled: Boolean,
    onAppNotificationsChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val appContext = LocalContext.current
    val appPreferences = remember(appContext) { AppPreferences(appContext) }
    val loginViewModel: LoginViewModel = hiltViewModel()
    val loginUiState by loginViewModel.uiState.collectAsState()

    // Xử lý chuyển hướng khi login thành công
    LaunchedEffect(loginUiState.loginSuccess) {
        if (loginUiState.loginSuccess) {
            navController.navigate(AppDestination.Home.route) {
                popUpTo(0) { inclusive = true }
            }
            loginViewModel.resetLoginState()
        }
    }

    // Xác định màn hình bắt đầu dựa trên dữ liệu tài khoản ghi nhớ
    val startRoute = if (loginUiState.lastAccount != null) {
        AppDestination.QuickLogin1.route
    } else {
        AppDestination.Welcome.route
    }

    NavHost(
        navController = navController,
        startDestination = startRoute,
        modifier = modifier
    ) {
        // --- MAIN TABS ---
        composable(AppDestination.Home.route) {
            val viewModel: HomeViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()
            val context = LocalContext.current

            HomeScreen(
                uiState = uiState,
                onToggleSandbox = { navController.navigate(AppDestination.Sandbox.route) },
                onBannerClick = { type, id ->
                    when (type) {
                        "EVENT" -> navController.navigate(AppDestination.EventDetail.createRoute(id))
                        "LOAN" -> navController.navigate(AppDestination.LoanDetail.createRoute(id))
                        // Workflow #51 — banner WEB mở URL (targetId) qua LinkHandler.
                        "WEB" -> com.example.easymoney.utils.LinkHandler.openUrl(context, id)
                    }
                },
                onRedeemClick = { navController.navigate(AppDestination.Rewards.route) },
                onVerifyEkycClick = { navController.navigate(AppDestination.IdentityVerification.route) },
                onLoanProductClick = { id -> 
                    if (id == "ALL") {
                        navController.navigate(AppDestination.LoanList.route)
                    } else {
                        navController.navigate(AppDestination.LoanDetail.createRoute(id))
                    }
                },
                onConsultLoanClick = { navController.navigate(AppDestination.ChatBot.route) },
                onManageLoanClick = {
                    android.util.Log.d("Analytics", "event=home_banner_click banner=loan_management")
                    navController.navigate(AppDestination.LoanManagement.route)
                },
                isDarkTheme = isDarkTheme,
                onToggleTheme = { onDarkThemeChange(!isDarkTheme) },
                modifier = Modifier.fillMaxSize()
            )
        }

        composable(AppDestination.TransactionHistory.route) {
            TransactionHistoryScreen(modifier = Modifier.fillMaxSize())
        }

        composable(AppDestination.Notifications.route) {
            NotificationScreen(
                onNavigateToHistory = { 
                    navController.navigate(AppDestination.TransactionHistory.route) {
                        popUpTo(AppDestination.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToEvent = { id -> navController.navigate(AppDestination.EventDetail.createRoute(id)) },
                modifier = Modifier.fillMaxSize()
            )
        }

        composable(AppDestination.Account.route) {
            AccountScreen(
                onLogout = {
                    loginViewModel.logout()
                    navController.navigate(AppDestination.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToMoneyManagement = { navController.navigate(AppDestination.MoneyManagement.route) },
                onNavigateToPaymentCards = { navController.navigate(AppDestination.PaymentCards.route) },
                onNavigateToHistory = { 
                    navController.navigate(AppDestination.TransactionHistory.route) {
                        popUpTo(AppDestination.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToRewards = { navController.navigate(AppDestination.Rewards.route) },
                onNavigateToChangePassword = { navController.navigate(AppDestination.ChangePassword.route) },
                onNavigateToSettings = { navController.navigate(AppDestination.GeneralSettings.route) },
                onNavigateToProfile = { navController.navigate(AppDestination.Profile.route) },
                onNavigateToSupport = { url, title ->
                    navController.navigate(
                        AppDestination.WebContent.createRoute(
                            normalizeWebUrl(url, appPreferences.apiBaseUrl),
                            title.ifBlank { "Chăm sóc khách hàng" }
                        )
                    )
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // --- AUTH FLOW ---
        composable(AppDestination.Welcome.route) {
            WelcomeScreen(
                onLoginClick = { navController.navigate(AppDestination.Login1.route) },
                onRegisterClick = { navController.navigate(AppDestination.Register1.route) }
            )
        }

        composable(AppDestination.Login1.route) {
            LoginScreen1(
                onLoginClick = { account, password, remember -> 
                    loginViewModel.login(account, password, remember) 
                },
                onRegisterClick = { navController.navigate(AppDestination.Register1.route) },
                isLoading = loginUiState.isLoading,
                errorMessage = loginUiState.error?.asString()
            )
        }

        composable(AppDestination.QuickLogin1.route) {
            val lastAccount = loginUiState.lastAccount
            QuickLoginScreen1(
                displayName = lastAccount?.fullName ?: "",
                onBackClick = { navController.navigate(AppDestination.Welcome.route) },
                onLoginClick = { password -> 
                    loginViewModel.login(lastAccount?.phone ?: "", password, true) 
                },
                onLoginWithOtherAccountClick = {
                    navController.navigate(AppDestination.Login1.route)
                },
                isLoading = loginUiState.isLoading,
                errorMessage = loginUiState.error?.asString(),
                onSwitchAccountClick = { /* Handled inside via showAccountSheet */ },
                otherAccounts = loginUiState.rememberedAccounts
                    .map { QuickLoginAccount(it.phone, it.fullName, it.phone) },
                onSelectAccount = { account ->
                    loginViewModel.selectAccount(loginUiState.rememberedAccounts.first { it.phone == account.id })
                },
                onDeleteAccount = { account ->
                    loginViewModel.deleteAccount(account.id)
                }
            )
        }

        composable(AppDestination.Register1.route) {
            RegisterScreen1(
                onRegisterClick = { phone, fullName, password ->
                    loginViewModel.register(phone, fullName, password)
                },
                isLoading = loginUiState.isLoading,
                errorMessage = loginUiState.error?.asString()
            )
        }

        // --- ONBOARDING & LOAN FLOW ---
        composable(
            route = AppDestination.Onboarding.route,
            arguments = listOf(
                navArgument(AppDestination.Onboarding.PACKAGE_ID_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument(AppDestination.Onboarding.PACKAGE_NAME_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val packageId = backStackEntry.arguments?.getString(AppDestination.Onboarding.PACKAGE_ID_ARG)
            OnboardingScreen(
                onContinueClick = { navController.navigate(AppDestination.ConfirmInformation.createRoute(packageId)) }
            )
        }

        composable(
            route = AppDestination.ConfirmInformation.route,
            arguments = listOf(
                navArgument(AppDestination.ConfirmInformation.PACKAGE_ID_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val packageId = backStackEntry.arguments?.getString(AppDestination.ConfirmInformation.PACKAGE_ID_ARG)
            val viewModel: ConfirmInfoViewModel = hiltViewModel()
            ConfirmInfoScreen(
                viewModel = viewModel,
                onContinue = { navController.navigate(AppDestination.LoanFlow.createRoute(packageId)) },
                onEditInfo = { navController.navigate(AppDestination.IdentityVerification.route) }
            )
        }

        composable(
            route = AppDestination.LoanFlow.route,
            arguments = listOf(
                navArgument(AppDestination.LoanFlow.PACKAGE_ID_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            LoanFlowScreen(
                onBack = { navController.popBackStack() },
                onCancel = { navController.popBackStack(AppDestination.Onboarding.route, inclusive = false) },
                onComplete = { navController.popBackStack(AppDestination.Home.route, inclusive = false) }
            )
        }

        // --- ACCOUNT SUB-SCREENS ---
        composable(AppDestination.MoneyManagement.route) {
            MoneyManagementScreen(
                onBack = { navController.popBackStack() },
                onNavigateToTopUp = { navController.navigate(AppDestination.TopUp.route) },
                onNavigateToWithdraw = { navController.navigate(AppDestination.Withdraw.route) }
            )
        }

        composable(AppDestination.PaymentCards.route) {
            PaymentCardsScreen(
                onBack = { navController.popBackStack() },
                onAddCard = { /* Handle add card form */ }
            )
        }

        composable(AppDestination.SecuritySettings.route) {
            SecuritySettingsScreen(
                onBack = { navController.popBackStack() },
                onChangePassword = { navController.navigate(AppDestination.ChangePassword.route) }
            )
        }

        composable(AppDestination.ChangePassword.route) {
            ChangePasswordScreen(
                onPasswordChanged = { navController.popBackStack() }
            )
        }

        // --- OTHERS ---
        composable(AppDestination.Sandbox.route) {
            SandBoxScreen(
                onBack = { navController.popBackStack() },
                onEsign = { navController.navigate(AppDestination.Contract.route) }
            )
        }

        composable(
            route = AppDestination.Contract.route,
            arguments = listOf(
                androidx.navigation.navArgument(AppDestination.Contract.CONTRACT_ID_ARG) {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val contractId = backStackEntry.arguments?.getString(AppDestination.Contract.CONTRACT_ID_ARG)
            ContractScreen(
                loanId = contractId ?: "MOCK-SANDBOX-123",
                onSignSuccess = {
                    navController.navigate(AppDestination.EsignSuccess.route) {
                        popUpTo(AppDestination.Contract.BASE_ROUTE) { inclusive = true }
                    }
                },
                onCancel = { navController.popBackStack() },
                // Workflow #31 — link "điều khoản" trong hợp đồng điều hướng sang Terms.
                onTermsClick = { navController.navigate(AppDestination.Terms.route) }
            )
        }

        composable(AppDestination.EsignSuccess.route) {
            EsignSuccessScreen(
                onBackToHome = {
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = AppDestination.PageGuide.route,
            arguments = listOf(
                navArgument(AppDestination.PageGuide.XML_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = AppDestination.PageGuide.DEFAULT_XML_NAME
                },
                navArgument(AppDestination.PageGuide.TITLE_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val xmlName = backStackEntry.arguments?.getString(AppDestination.PageGuide.XML_ARG)
            val title = backStackEntry.arguments?.getString(AppDestination.PageGuide.TITLE_ARG)
            PageGuideScreen(xmlName = xmlName, title = title)
        }

        composable(AppDestination.EventDetail.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString(AppDestination.EventDetail.ID_ARG) ?: ""
            EventDetailScreen(
                eventId = id,
                onBack = { navController.popBackStack() },
                onOpenEventPage = { url, title ->
                    navController.navigate(
                        AppDestination.WebContent.createRoute(
                            normalizeWebUrl(url, appPreferences.apiBaseUrl),
                            title
                        )
                    )
                }
            )
        }

        composable(
            route = AppDestination.WebContent.route,
            arguments = listOf(
                navArgument(AppDestination.WebContent.URL_ARG) {
                    type = NavType.StringType
                    nullable = false
                },
                navArgument(AppDestination.WebContent.TITLE_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val url = Uri.decode(backStackEntry.arguments?.getString(AppDestination.WebContent.URL_ARG).orEmpty())
            WebContentScreen(
                url = normalizeWebUrl(url, appPreferences.apiBaseUrl),
                modifier = Modifier.fillMaxSize()
            )
        }

        composable(AppDestination.Rewards.route) {
            RewardScreen(onBack = { navController.popBackStack() })
        }

        composable(AppDestination.LoanList.route) {
            LoanListScreen(
                onBack = { navController.popBackStack() },
                onPackageClick = { id -> 
                    navController.navigate(AppDestination.LoanDetail.createRoute(id))
                }
            )
        }

        composable(AppDestination.LoanDetail.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString(AppDestination.LoanDetail.ID_ARG) ?: ""
            LoanDetailScreen(
                packageId = id,
                onBack = { navController.popBackStack() },
                onRegisterSuccess = { packageId, packageName -> 
                    navController.navigate(AppDestination.Onboarding.createRoute(packageId, packageName))
                },
                onNavigateToProfile = {
                    navController.navigate(AppDestination.Profile.route)
                }
            )
        }

        composable(AppDestination.Profile.route) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onEditProfile = { navController.navigate(AppDestination.IdentityVerification.route) },
                onVerifyIdentity = { navController.navigate(AppDestination.IdentityVerification.route) }
            )
        }

        composable(AppDestination.GeneralSettings.route) {
            GeneralSettingsScreen(
                isDarkTheme = isDarkTheme,
                onDarkThemeChange = onDarkThemeChange,
                appNotificationsEnabled = appNotificationsEnabled,
                onAppNotificationsChange = onAppNotificationsChange,
                onTermsClick = { navController.navigate(AppDestination.Terms.route) }
            )
        }

        composable(AppDestination.ChatBot.route) {
            com.example.easymoney.ui.chatbot.ChatBotScreen(
                onNavigateRoute = { route -> navController.navigate(route) }
            )
        }

        composable(AppDestination.Terms.route) {
            com.example.easymoney.ui.terms.TermsScreen()
        }

        composable(AppDestination.TopUp.route) {
            com.example.easymoney.ui.payment.TopUpScreen(
                onTopUpSuccess = { navController.popBackStack() }
            )
        }

        composable(AppDestination.Withdraw.route) {
            com.example.easymoney.ui.payment.WithdrawScreen(
                onWithdrawSuccess = { navController.popBackStack() }
            )
        }

        composable(AppDestination.LoanManagement.route) {
            com.example.easymoney.ui.loan.management.LoanManagementScreen(
                onSignContract = { contractId ->
                    navController.navigate(AppDestination.Contract.createRoute(contractId))
                }
            )
        }

        composable(AppDestination.IdentityVerification.route) {
            ProfileCompletionScreen(
                onBack = { navController.popBackStack() },
                onNavigateToEditPersonalInfo = { navController.navigate(AppDestination.EditPersonalInfo.route) },
                onNavigateToEditJobInfo = { navController.navigate(AppDestination.EditJobInfo.route) },
                onNavigateToEditContactInfo = { navController.navigate(AppDestination.EditContactInfo.route) }
            )
        }

        composable(AppDestination.EditPersonalInfo.route) {
            EditPersonalInfoScreen(onBack = { navController.popBackStack() })
        }

        composable(AppDestination.EditJobInfo.route) {
            EditJobInfoScreen(onBack = { navController.popBackStack() })
        }

        composable(AppDestination.EditContactInfo.route) {
            EditContactInfoScreen(onBack = { navController.popBackStack() })
        }
    }
}

private fun normalizeWebUrl(rawUrl: String, baseUrl: String): String {
    val trimmed = rawUrl.trim()
    if (trimmed.startsWith("https://") || trimmed.startsWith("http://")) {
        return trimmed
    }
    val normalizedBase = baseUrl.trimEnd('/')
    return if (trimmed.startsWith("/")) {
        normalizedBase + trimmed
    } else {
        "$normalizedBase/$trimmed"
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title)
    }
}
