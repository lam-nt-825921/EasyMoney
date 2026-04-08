package com.example.easymoney.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.easymoney.ui.loan.LoanViewModel
import com.example.easymoney.ui.loan.configuration.LoanConfigurationScreen
import com.example.easymoney.ui.notification.NotificationScreen
import com.example.easymoney.ui.onboarding.OnboardingScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Home.route,
        modifier = modifier
    ) {
        composable(AppDestination.Home.route) {
            HomeScreen(
                onLoanRegistrationClick = { navController.navigate(AppDestination.Onboarding.route) }
            )
        }

        composable(AppDestination.Onboarding.route) {
            OnboardingScreen(
                onContinueClick = { navController.navigate(AppDestination.ConfirmInformation.route) }
            )
        }

        composable(AppDestination.ConfirmInformation.route) {
            val viewModel: ConfirmInfoViewModel = hiltViewModel()

            LaunchedEffect(viewModel) {
                viewModel.navigationEvent.collect { event ->
                    when (event) {
                        ConfirmInfoViewModel.NavigationEvent.ToLoanInformation -> {
                            navController.navigate(AppDestination.LoanInformation.route)
                        }

                        ConfirmInfoViewModel.NavigationEvent.ToEditInformation -> {
                            navController.popBackStack()
                        }
                    }
                }
            }

            ConfirmInfoScreen(
                viewModel = viewModel,
                onBackButton = { navController.popBackStack() }
            )
        }

        composable(AppDestination.LoanInformation.route) {
            val viewModel: LoanViewModel = hiltViewModel()

            LaunchedEffect(viewModel) {
                viewModel.loadLoanPackage()
            }

            LaunchedEffect(viewModel) {
                viewModel.navigationEvent.collect { event ->
                    when (event) {
                        LoanViewModel.NavigationEvent.ToNextStep -> {
                            navController.navigate(AppDestination.PageGuide.createRoute())
                        }
                    }
                }
            }

            LoanConfigurationScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
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
