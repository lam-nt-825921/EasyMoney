package com.example.easymoney.navigation

import androidx.compose.runtime.Composable
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
import com.example.easymoney.ui.loan.flow.LoanFlowScreen
import com.example.easymoney.ui.notification.NotificationScreen
import com.example.easymoney.ui.onboarding.OnboardingScreen
import com.example.easymoney.ui.sandbox.SandBoxScreen
import com.example.easymoney.ui.esign.ContractScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Home.route,
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
                    // Logic after successful signing
                    navController.popBackStack()
                },
                onCancel = {
                    navController.popBackStack()
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
                    // Quay lại một bước tiêu chuẩn (ví dụ về ConfirmInformation)
                    navController.popBackStack()
                },
                onCancel = {
                    // Quay về Onboarding khi huỷ đăng ký
                    navController.popBackStack(AppDestination.Onboarding.route, inclusive = false)
                },
                onComplete = {
                    // Xử lý khi hoàn thành (ví dụ: về Home)
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
