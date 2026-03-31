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
        title = "Vay tú chứcs tái chính",
        showBackButton = false,
        topBarBackgroundColor = Color.White
    )

    data object LoanInformation : AppDestination(
        route = "loan_information",
        title = "Thông tin khoản vay",
        showBackButton = false,
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
    route == AppDestination.Onboarding.route -> AppDestination.Onboarding
    route == AppDestination.LoanInformation.route -> AppDestination.LoanInformation
    route?.startsWith(AppDestination.PageGuide.BASE_ROUTE) == true -> AppDestination.PageGuide
    else -> AppDestination.Onboarding
}


