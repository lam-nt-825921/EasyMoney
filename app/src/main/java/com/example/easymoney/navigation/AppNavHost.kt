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
import com.example.easymoney.ui.account.AccountScreen
import com.example.easymoney.ui.confirmation.ConfirmInfoScreen
import com.example.easymoney.ui.confirmation.ConfirmInfoViewModel
import com.example.easymoney.ui.guide.PageGuideScreen
import com.example.easymoney.ui.history.TransactionHistoryScreen
import com.example.easymoney.ui.home.HomeScreen

import com.example.easymoney.ui.home.HomeViewModel
import com.example.easymoney.ui.login.LoginScreen1
import com.example.easymoney.ui.login.LoginViewModel
import com.example.easymoney.ui.login.QuickLoginScreen1
import com.example.easymoney.ui.login.QuickLoginAccount
import com.example.easymoney.ui.login.RegisterScreen1
import com.example.easymoney.ui.login.WelcomeScreen
import com.example.easymoney.ui.loan.flow.LoanFlowScreen
import com.example.easymoney.ui.notification.NotificationScreen
import com.example.easymoney.ui.onboarding.OnboardingScreen
import com.example.easymoney.ui.sandbox.SandBoxScreen
import com.example.easymoney.ui.esign.ContractScreen
import com.example.easymoney.ui.esign.EsignSuccessScreen

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
    // Lưu ý: Trong thực tế nên dùng Splash screen để chờ load dữ liệu
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
        composable(AppDestination.Home.route) {
            val viewModel: HomeViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            HomeScreen(
                onLoanRegistrationClick = { navController.navigate(AppDestination.Onboarding.route) },
                onToggleSandbox = { navController.navigate(AppDestination.Sandbox.route) },
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                userName = uiState.userName.ifBlank { "NGUYEN LE MINH" },
                isLoading = uiState.isLoading
            )
        }

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
                    .filter { it.phone != lastAccount?.phone }
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

        composable(AppDestination.Sandbox.route) {
            SandBoxScreen(
                onBack = { navController.popBackStack() },
                onEsign = { navController.navigate(AppDestination.Contract.route) }
            )
        }

        composable(AppDestination.Contract.route) {
            ContractScreen(
                loanId = "MOCK-SANDBOX-123",
                onSignSuccess = {
                    navController.navigate(AppDestination.EsignSuccess.route) {
                        popUpTo(AppDestination.Contract.route) { inclusive = true }
                    }
                },
                onCancel = {
                    navController.popBackStack()
                }
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

        composable(AppDestination.Onboarding.route) {
            OnboardingScreen(
                onContinueClick = { navController.navigate(AppDestination.ConfirmInformation.route) }
            )
        }

        composable(AppDestination.ConfirmInformation.route) {
            val viewModel: ConfirmInfoViewModel = hiltViewModel()

            ConfirmInfoScreen(
                viewModel = viewModel,
                onContinue = { navController.navigate(AppDestination.LoanFlow.route) },
                onEditInfo = { navController.popBackStack() }
            )
        }

        composable(AppDestination.LoanFlow.route) {
            LoanFlowScreen(
                onBack = {
                    navController.popBackStack()
                },
                onCancel = {
                    navController.popBackStack(AppDestination.Onboarding.route, inclusive = false)
                },
                onComplete = {
                    navController.popBackStack(AppDestination.Home.route, inclusive = false)
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
                }
            )
        ) { backStackEntry ->
            val xmlName = backStackEntry.arguments?.getString(AppDestination.PageGuide.XML_ARG)
            PageGuideScreen(xmlName = xmlName)
        }

        composable(AppDestination.TransactionHistory.route) {
            TransactionHistoryScreen()
        }

        composable(AppDestination.Notifications.route) {
            NotificationScreen()
        }

        composable(AppDestination.Account.route) {
            AccountScreen()
        }
    }
}
