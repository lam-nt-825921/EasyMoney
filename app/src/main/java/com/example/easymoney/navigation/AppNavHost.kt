package com.example.easymoney.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.example.easymoney.ui.security.SecuritySettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
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

            HomeScreen(
                uiState = uiState,
                onLoanRegistrationClick = { packageId, skipDetail -> 
                    if (skipDetail) {
                        val packageName = uiState.hotLoans.find { it.id == packageId }?.name
                        navController.navigate(AppDestination.Onboarding.createRoute(packageId, packageName))
                    } else {
                        navController.navigate(AppDestination.LoanDetail.createRoute(packageId)) 
                    }
                },
                onToggleSandbox = { navController.navigate(AppDestination.Sandbox.route) },
                onBannerClick = { type, id ->
                    when (type) {
                        "EVENT" -> navController.navigate(AppDestination.EventDetail.createRoute(id))
                        "LOAN" -> navController.navigate(AppDestination.LoanDetail.createRoute(id))
                        "WEB" -> { /* Open URL in custom tab or webview */ }
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
                onNavigateToProfile = { navController.navigate(AppDestination.Profile.route) },
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
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
                onNavigateToSecurity = { navController.navigate(AppDestination.SecuritySettings.route) },
                onNavigateToSettings = { navController.navigate(AppDestination.GeneralSettings.route) },
                onNavigateToSupport = { /* Open web center */ },
                onNavigateToProfile = { navController.navigate(AppDestination.Profile.route) },
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
        ) {
            OnboardingScreen(
                onContinueClick = { navController.navigate(AppDestination.ConfirmInformation.route) }
            )
        }

        composable(AppDestination.ConfirmInformation.route) {
            val viewModel: ConfirmInfoViewModel = hiltViewModel()
            ConfirmInfoScreen(
                viewModel = viewModel,
                onContinue = { navController.navigate(AppDestination.LoanFlow.route) },
                onEditInfo = { navController.navigate(AppDestination.IdentityVerification.route) }
            )
        }

        composable(AppDestination.LoanFlow.route) {
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
                onChangePassword = { /* Handle change password */ }
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
                onBack = { navController.popBackStack() }
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
            ProfileScreen(onBack = { navController.popBackStack() })
        }

        composable(AppDestination.GeneralSettings.route) {
            GeneralSettingsScreen(onBack = { navController.popBackStack() })
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

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title)
    }
}
