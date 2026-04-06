package com.example.easymoney.navigation

import androidx.compose.ui.graphics.Color

/**
 * Central route contracts and top-bar metadata for the app.
 */
sealed class AppDestination(
    val route: String,
    val title: String,
    val showBackButton: Boolean,
    val showHelpButton: Boolean = true,
    val guideXmlName: String? = null,
    val topBarBackgroundColor: Color? = null,
    val topBarContentColor: Color? = null
) {
    data object Onboarding : AppDestination(
        route = "onboarding",
        title = "Vay tổ chức tài chính",
        showBackButton = true,
        topBarBackgroundColor = Color.White
    )

    data object ConfirmInformation : AppDestination(
        route = "confirm_information",
        title = "Xác nhận thông tin",
        showBackButton = true,
        topBarBackgroundColor = Color.White
    )

    // Single destination for the whole internal LoanFlow (all steps are managed inside LoanFlowScreen).
    data object LoanFlow : AppDestination(
        route = "loan_information",
        title = "Thông tin khoản vay",
        showBackButton = true,
        showHelpButton = false,
        topBarBackgroundColor = Color.White
    )

    data object Home : AppDestination(
        route = "home",
        title = "",
        showBackButton = false,
        showHelpButton = false,
        topBarBackgroundColor = Color.White
    )

    data object PageGuide : AppDestination(
        route = "page_guide?xml={xml}",
        title = "Hướng dẫn",
        showBackButton = true,
        showHelpButton = false,
        topBarBackgroundColor = Color.White
    ) {
        const val BASE_ROUTE = "page_guide"
        const val XML_ARG = "xml"
        const val DEFAULT_XML_NAME = "guide_default_updating"

        fun createRoute(xmlName: String? = null): String {
            val normalized = xmlName?.trim().orEmpty()
            return if (normalized.isEmpty()) BASE_ROUTE else "$BASE_ROUTE?$XML_ARG=$normalized"
        }
    }
}

fun appDestinationFromRoute(route: String?): AppDestination = when {
    route == AppDestination.Home.route -> AppDestination.Home
    route == AppDestination.Onboarding.route -> AppDestination.Onboarding
    route == AppDestination.ConfirmInformation.route -> AppDestination.ConfirmInformation
    route == AppDestination.LoanFlow.route -> AppDestination.LoanFlow
    route?.startsWith(AppDestination.PageGuide.BASE_ROUTE) == true -> AppDestination.PageGuide
    else -> AppDestination.Home
}


