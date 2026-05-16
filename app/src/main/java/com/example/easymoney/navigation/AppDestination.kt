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
        showBackButton = false,
        showHelpButton = false,
        defaultTopBarMode = TopBarMode.HIDDEN
    )

    data object Login1 : AppDestination(
        route = "login_1",
        title = "",
        showBackButton = true,
        showHelpButton = false,
        defaultTopBarMode = TopBarMode.NO_TITLE
    )

    data object Register1 : AppDestination(
        route = "register_1",
        title = "",
        showBackButton = true,
        showHelpButton = false,
        defaultTopBarMode = TopBarMode.NO_TITLE
    )

    data object QuickLogin1 : AppDestination(
        route = "quick_login_1",
        title = "",
        showBackButton = false,
        showHelpButton = false,
        defaultTopBarMode = TopBarMode.HIDDEN
    )

    data object Onboarding : AppDestination(
        route = "onboarding?packageId={packageId}&packageName={packageName}",
        title = "",
        showBackButton = true,
        guideXmlName = "guide_onboarding"
    ) {
        const val BASE_ROUTE = "onboarding"
        const val PACKAGE_ID_ARG = "packageId"
        const val PACKAGE_NAME_ARG = "packageName"

        fun createRoute(packageId: String? = null, packageName: String? = null): String {
            val builder = StringBuilder(BASE_ROUTE)
            var hasParam = false
            if (!packageId.isNullOrEmpty()) {
                builder.append("?").append(PACKAGE_ID_ARG).append("=").append(packageId)
                hasParam = true
            }
            if (!packageName.isNullOrEmpty()) {
                builder.append(if (hasParam) "&" else "?").append(PACKAGE_NAME_ARG).append("=").append(packageName)
            }
            return builder.toString()
        }
    }

    data object ConfirmInformation : AppDestination(
        route = "confirm_information",
        title = "Xác nhận thông tin",
        showBackButton = true,
        guideXmlName = "guide_confirm_information"
    )

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
        showHelpButton = true,
        guideXmlName = "guide_home",
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
        route = "page_guide?xml={xml}&title={title}",
        title = "Hướng dẫn",
        showBackButton = true,
        showHelpButton = false
    ) {
        const val BASE_ROUTE = "page_guide"
        const val XML_ARG = "xml"
        const val TITLE_ARG = "title"
        const val DEFAULT_XML_NAME = "guide_default_updating"

        fun createRoute(xmlName: String? = null, title: String? = null): String {
            val builder = StringBuilder(BASE_ROUTE)
            var hasParam = false
            if (!xmlName.isNullOrBlank()) {
                builder.append("?").append(XML_ARG).append("=").append(xmlName.trim())
                hasParam = true
            }
            if (!title.isNullOrBlank()) {
                builder.append(if (hasParam) "&" else "?").append(TITLE_ARG).append("=").append(title.trim())
            }
            return builder.toString()
        }
    }

    data object Sandbox : AppDestination(
        route = "sandbox",
        title = "Sandbox Developer",
        showBackButton = true,
        showHelpButton = false
    )

    data object Contract : AppDestination(
        route = "contract?contractId={contractId}",
        title = "Hợp đồng của bạn",
        showBackButton = true,
        showHelpButton = true,
        guideXmlName = "guide_contract"
    ) {
        const val BASE_ROUTE = "contract"
        const val CONTRACT_ID_ARG = "contractId"
        fun createRoute(contractId: String? = null): String =
            if (contractId.isNullOrEmpty()) BASE_ROUTE else "$BASE_ROUTE?contractId=$contractId"
    }

    data object EsignSuccess : AppDestination(
        route = "esign_success",
        title = "Ký hợp đồng thành công",
        showBackButton = false,
        showHelpButton = false
    )

    // New Destinations from Phase 1
    data object EventDetail : AppDestination(
        route = "event_detail/{id}",
        title = "Chi tiết sự kiện",
        showBackButton = true,
        guideXmlName = "guide_event_detail"
    ) {
        const val BASE_ROUTE = "event_detail"
        const val ID_ARG = "id"
        fun createRoute(id: String) = "$BASE_ROUTE/$id"
    }

    data object Rewards : AppDestination(
        route = "rewards",
        title = "Đổi điểm thưởng",
        showBackButton = true,
        guideXmlName = "guide_rewards"
    )

    data object LoanList : AppDestination(
        route = "loan_list",
        title = "Gói vay ưu đãi",
        showBackButton = true,
        guideXmlName = "guide_loan_list"
    )

    data object LoanDetail : AppDestination(
        route = "loan_detail/{id}",
        title = "Chi tiết gói vay",
        showBackButton = true,
        guideXmlName = "guide_loan_detail"
    ) {
        const val BASE_ROUTE = "loan_detail"
        const val ID_ARG = "id"
        fun createRoute(id: String) = "$BASE_ROUTE/$id"
    }

    data object Profile : AppDestination(
        route = "profile",
        title = "Hồ sơ cá nhân",
        showBackButton = true,
        guideXmlName = "guide_profile"
    )

    data object MoneyManagement : AppDestination(
        route = "money_management",
        title = "Quản lý nguồn tiền",
        showBackButton = true,
        guideXmlName = "guide_money_management"
    )

    data object PaymentCards : AppDestination(
        route = "payment_cards",
        title = "Thẻ thanh toán",
        showBackButton = true,
        guideXmlName = "guide_payment_cards"
    )

    data object GeneralSettings : AppDestination(
        route = "general_settings",
        title = "Cài đặt",
        showBackButton = true,
        guideXmlName = "guide_general_settings"
    )

    data object SecuritySettings : AppDestination(
        route = "security_settings",
        title = "Bảo mật tài khoản",
        showBackButton = true,
        guideXmlName = "guide_security_settings"
    )

    data object ChatBot : AppDestination(
        route = "chatbot",
        title = "Tư vấn tài chính",
        showBackButton = true,
        showHelpButton = false
    )

    data object IdentityVerification : AppDestination(
        route = "identity_verification",
        title = "Định danh tài khoản",
        showBackButton = true,
        showHelpButton = true,
        guideXmlName = "guide_identity_verification"
    )

    data object TopUp : AppDestination(
        route = "top_up",
        title = "Nạp tiền",
        showBackButton = true,
        guideXmlName = "guide_top_up"
    )

    data object Withdraw : AppDestination(
        route = "withdraw",
        title = "Rút tiền",
        showBackButton = true,
        guideXmlName = "guide_withdraw"
    )

    data object Terms : AppDestination(
        route = "terms",
        title = "Điều khoản và chính sách",
        showBackButton = true,
        showHelpButton = false
    )

    data object LoanManagement : AppDestination(
        route = "loan_management",
        title = "Quản lý khoản vay",
        showBackButton = true,
        showHelpButton = true,
        guideXmlName = "guide_loan_management"
    )

    data object EditPersonalInfo : AppDestination(
        route = "edit_personal_info",
        title = "Thông tin cá nhân",
        showBackButton = true,
        guideXmlName = "guide_edit_personal_info"
    )

    data object EditJobInfo : AppDestination(
        route = "edit_job_info",
        title = "Công việc & Thu nhập",
        showBackButton = true,
        guideXmlName = "guide_edit_job_info"
    )

    data object EditContactInfo : AppDestination(
        route = "edit_contact_info",
        title = "Thông tin người liên hệ",
        showBackButton = true,
        guideXmlName = "guide_edit_contact_info"
    )
}

fun appDestinationFromRoute(route: String?): AppDestination = when {
    route == AppDestination.Home.route -> AppDestination.Home
    route == AppDestination.Welcome.route -> AppDestination.Welcome
    route == AppDestination.Login1.route -> AppDestination.Login1
    route == AppDestination.QuickLogin1.route -> AppDestination.QuickLogin1
    route == AppDestination.Register1.route -> AppDestination.Register1
    route?.startsWith(AppDestination.Onboarding.BASE_ROUTE) == true -> AppDestination.Onboarding
    route == AppDestination.ConfirmInformation.route -> AppDestination.ConfirmInformation
    route == AppDestination.LoanFlow.route -> AppDestination.LoanFlow
    route == AppDestination.TransactionHistory.route -> AppDestination.TransactionHistory
    route == AppDestination.Notifications.route -> AppDestination.Notifications
    route == AppDestination.Account.route -> AppDestination.Account
    route == AppDestination.Sandbox.route -> AppDestination.Sandbox
    route?.startsWith(AppDestination.Contract.BASE_ROUTE) == true -> AppDestination.Contract
    route == AppDestination.EsignSuccess.route -> AppDestination.EsignSuccess
    route == AppDestination.Rewards.route -> AppDestination.Rewards
    route == AppDestination.LoanList.route -> AppDestination.LoanList
    route == AppDestination.Profile.route -> AppDestination.Profile
    route == AppDestination.MoneyManagement.route -> AppDestination.MoneyManagement
    route == AppDestination.PaymentCards.route -> AppDestination.PaymentCards
    route == AppDestination.GeneralSettings.route -> AppDestination.GeneralSettings
    route == AppDestination.SecuritySettings.route -> AppDestination.SecuritySettings
    route == AppDestination.ChatBot.route -> AppDestination.ChatBot
    route == AppDestination.IdentityVerification.route -> AppDestination.IdentityVerification
    route == AppDestination.LoanManagement.route -> AppDestination.LoanManagement
    route == AppDestination.Terms.route -> AppDestination.Terms
    route == AppDestination.TopUp.route -> AppDestination.TopUp
    route == AppDestination.Withdraw.route -> AppDestination.Withdraw
    route == AppDestination.EditPersonalInfo.route -> AppDestination.EditPersonalInfo
    route == AppDestination.EditJobInfo.route -> AppDestination.EditJobInfo
    route == AppDestination.EditContactInfo.route -> AppDestination.EditContactInfo
    route?.startsWith(AppDestination.EventDetail.BASE_ROUTE) == true -> AppDestination.EventDetail
    route?.startsWith(AppDestination.LoanDetail.BASE_ROUTE) == true -> AppDestination.LoanDetail
    route?.startsWith(AppDestination.PageGuide.BASE_ROUTE) == true -> AppDestination.PageGuide
    else -> AppDestination.Home
}
