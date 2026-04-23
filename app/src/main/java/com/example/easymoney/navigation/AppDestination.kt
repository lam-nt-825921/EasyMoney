package com.example.easymoney.navigation

import androidx.compose.ui.graphics.Color
import com.example.easymoney.ui.components.TopBarMode
import com.example.easymoney.ui.components.SystemBarMode
import com.example.easymoney.ui.components.ScreenColorMode

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
    val topBarContentColor: Color? = null,
    /** True for the four main bottom-tab destinations. */
    val isMainTab: Boolean = false,
    val defaultTopBarMode: TopBarMode = TopBarMode.STANDARD,
    val defaultSystemBarMode: SystemBarMode = SystemBarMode.THEME_DEFAULT,
    val defaultScreenColorMode: ScreenColorMode = ScreenColorMode.THEME_AWARE
) {
    data object Welcome : AppDestination(
        route = "welcome",
        title = "",
        showBackButton = true,
        showHelpButton = false,
        topBarBackgroundColor = Color.White,
        defaultTopBarMode = TopBarMode.HIDDEN
    )

    data object Login1 : AppDestination(
        route = "login_1",
        title = "",
        showBackButton = true,
        showHelpButton = false,
        topBarBackgroundColor = Color.White,
        defaultTopBarMode = TopBarMode.HIDDEN
    )

    data object QuickLogin1 : AppDestination(
        route = "quick_login_1",
        title = "",
        showBackButton = false,
        showHelpButton = false,
        topBarBackgroundColor = Color.White,
        defaultTopBarMode = TopBarMode.HIDDEN
    )

    data object Register1 : AppDestination(
        route = "register_1",
        title = "Đăng ký tài khoản",
        showBackButton = true,
        showHelpButton = true,
        topBarBackgroundColor = Color.White
    )

    data object Onboarding : AppDestination(
        route = "onboarding",
        title = "Vay tổ chức tài chính",
        showBackButton = true
    )

    data object ConfirmInformation : AppDestination(
        route = "confirm_information",
        title = "Xác nhận thông tin",
        showBackButton = true
    )

    // Single destination for the whole internal LoanFlow (all steps are managed inside LoanFlowScreen).
    data object LoanFlow : AppDestination(
        route = "loan_information",
        title = "Thông tin khoản vay",
        showBackButton = true,
        showHelpButton = false
    )

    data object Home : AppDestination(
        route = "home",
        title = "",
        showBackButton = false,
        showHelpButton = false,
        isMainTab = true
    )

    data object TransactionHistory : AppDestination(
        route = "history",
        title = "Lịch sử giao dịch",
        showBackButton = false,
        showHelpButton = false,
        isMainTab = true
    )

    data object Notifications : AppDestination(
        route = "notifications",
        title = "Thông báo",
        showBackButton = false,
        showHelpButton = false,
        isMainTab = true
    )

    data object Account : AppDestination(
        route = "account",
        title = "Tài khoản",
        showBackButton = false,
        showHelpButton = false,
        isMainTab = true
    )

    data object PageGuide : AppDestination(
        route = "page_guide?xml={xml}",
        title = "Hướng dẫn",
        showBackButton = true,
        showHelpButton = false
    ) {
        const val BASE_ROUTE = "page_guide"
        const val XML_ARG = "xml"
        const val DEFAULT_XML_NAME = "guide_default_updating"

        fun createRoute(xmlName: String? = null): String {
            val normalized = xmlName?.trim().orEmpty()
            return if (normalized.isEmpty()) BASE_ROUTE else "$BASE_ROUTE?$XML_ARG=$normalized"
        }
    }

    data object Sandbox : AppDestination(
        route = "sandbox",
        title = "Sandbox Developer",
        showBackButton = true,
        showHelpButton = false
    )

    data object Contract : AppDestination(
        route = "contract",
        title = "Hợp đồng của bạn",
        showBackButton = true,
        showHelpButton = true
    )

    data object EsignSuccess : AppDestination(
        route = "esign_success",
        title = "Ký hợp đồng thành công",
        showBackButton = false,
        showHelpButton = false
    )
}

fun appDestinationFromRoute(route: String?): AppDestination = when {
    route == AppDestination.Home.route -> AppDestination.Home
    route == AppDestination.Welcome.route -> AppDestination.Welcome
    route == AppDestination.Login1.route -> AppDestination.Login1
    route == AppDestination.QuickLogin1.route -> AppDestination.QuickLogin1
    route == AppDestination.Register1.route -> AppDestination.Register1
    route == AppDestination.Onboarding.route -> AppDestination.Onboarding
    route == AppDestination.ConfirmInformation.route -> AppDestination.ConfirmInformation
    route == AppDestination.LoanFlow.route -> AppDestination.LoanFlow
    route == AppDestination.TransactionHistory.route -> AppDestination.TransactionHistory
    route == AppDestination.Notifications.route -> AppDestination.Notifications
    route == AppDestination.Account.route -> AppDestination.Account
    route == AppDestination.Sandbox.route -> AppDestination.Sandbox
    route == AppDestination.Contract.route -> AppDestination.Contract
    route == AppDestination.EsignSuccess.route -> AppDestination.EsignSuccess
    route?.startsWith(AppDestination.PageGuide.BASE_ROUTE) == true -> AppDestination.PageGuide
    else -> AppDestination.Home
}
