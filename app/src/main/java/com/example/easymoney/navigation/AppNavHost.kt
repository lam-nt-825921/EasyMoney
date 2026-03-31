package com.example.easymoney.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.easymoney.data.model.LoanPackage
import com.example.easymoney.ui.guide.PageGuideScreen
import com.example.easymoney.ui.loan.LoanScreenMock
import com.example.easymoney.ui.onboarding.OnboardingScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Onboarding.route,
        modifier = modifier
    ) {
        composable(AppDestination.Onboarding.route) {
            OnboardingScreen(
                onNavigateToLoan = {
                    navController.navigate(AppDestination.LoanInformation.route) {
                        popUpTo(AppDestination.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestination.LoanInformation.route) {
            val mockPackage = LoanPackage(
                id = "1",
                packageName = "Vay Nhanh",
                tenorRange = "6,12,18,24",
                minAmount = 6_000_000,
                maxAmount = 100_000_000,
                interest = 12.0,
                overdueCost = 5.0,
                eligibleCreditScore = 600
            )

            LoanScreenMock(
                mockPackage = mockPackage,
                initialAmount = 70_000_000,
                initialTenor = 6,
                initialInsuranceSelected = true
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

        // Register new scene routes here as the product flow is expanded.
    }
}

