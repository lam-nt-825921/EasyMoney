package com.example.easymoney.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.easymoney.ui.confirmation.ConfirmInfoScreen
import com.example.easymoney.ui.confirmation.ConfirmInfoUiState
import com.example.easymoney.ui.guide.PageGuideScreen
import com.example.easymoney.ui.home.HomeScreen
import com.example.easymoney.ui.loan.LoanUiState
import com.example.easymoney.ui.loan.configuration.LoanConfigurationContent
import com.example.easymoney.ui.loan.configuration.LoanConfigurationPreview
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
            val uiState = ConfirmInfoUiState.mock()

            ConfirmInfoScreen(
                uiState = uiState,
                onContinueClick = { navController.navigate(AppDestination.LoanInformation.route) },
                onEditInfoClick = { navController.popBackStack() }
            )
        }

        composable(AppDestination.LoanInformation.route) {
            LoanConfigurationPreview()
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

        // Register new scene routes here as the product flow is expanded.
    }
}
