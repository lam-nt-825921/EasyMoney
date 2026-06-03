package com.example.easymoney.navigation

import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.example.easymoney.R
import com.example.easymoney.ui.components.TopBarMode
import com.example.easymoney.ui.components.SystemBarMode
import com.example.easymoney.ui.components.ScreenColorMode

/**
 * Central route contracts and top-bar metadata for the app.
 */
sealed class AppDestination(
    val route: String,
    @StringRes val titleResId: Int? = null,
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
        showBackButton = false,
        showHelpButton = false,
        defaultTopBarMode = TopBarMode.HIDDEN
    )

    data object Login1 : AppDestination(
        route = "login_1",
        showBackButton = true,
        showHelpButton = false,
        defaultTopBarMode = TopBarMode.NO_TITLE
    )

    data object Register1 : AppDestination(
        route = "register_1",
        showBackButton = true,
        showHelpButton = false,
        defaultTopBarMode = TopBarMode.NO_TITLE
    )

    data object QuickLogin1 : AppDestination(
        route = "quick_login_1",
        showBackButton = false,
        showHelpButton = false,
        defaultTopBarMode = TopBarMode.HIDDEN
    )

    data object Onboarding : AppDestination(
        route = "onboarding?packageId={packageId}&packageName={packageName}",
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
        route = "confirm_information?packageId={packageId}",
        titleResId = R.string.nav_confirm_information,
        showBackButton = true,
        guideXmlName = "guide_confirm_information"
    ) {
        const val BASE_ROUTE = "confirm_information"
        const val PACKAGE_ID_ARG = "packageId"
        fun createRoute(packageId: String? = null): String =
            if (packageId.isNullOrBlank()) BASE_ROUTE else "$BASE_ROUTE?packageId=${Uri.encode(packageId)}"
    }

    data object LoanFlow : AppDestination(
        route = "loan_information?packageId={packageId}",
        titleResId = R.string.nav_loan_information,
        showBackButton = true,
        showHelpButton = false
    ) {
        const val BASE_ROUTE = "loan_information"
        const val PACKAGE_ID_ARG = "packageId"
        fun createRoute(packageId: String? = null): String =
            if (packageId.isNullOrBlank()) BASE_ROUTE else "$BASE_ROUTE?packageId=${Uri.encode(packageId)}"
    }

    data object Home : AppDestination(
        route = "home",
        showBackButton = false,
        showHelpButton = true,
        guideXmlName = "guide_home",
        isMainTab = true
    )

    data object TransactionHistory : AppDestination(
        route = "history",
        titleResId = R.string.nav_transaction_history,
        showBackButton = false,
        showHelpButton = false,
        isMainTab = true
    )

    data object Notifications : AppDestination(
        route = "notifications",
        titleResId = R.string.nav_notifications,
        showBackButton = false,
        showHelpButton = false,
        isMainTab = true
    )

    data object Account : AppDestination(
        route = "account",
        titleResId = R.string.nav_account,
        showBackButton = false,
        showHelpButton = false,
        isMainTab = true
    )

    data object PageGuide : AppDestination(
        route = "page_guide?xml={xml}&title={title}",
        titleResId = R.string.nav_page_guide,
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
        titleResId = R.string.nav_sandbox,
        showBackButton = true,
        showHelpButton = false
    )

    data object Contract : AppDestination(
        route = "contract?contractId={contractId}&readOnly={readOnly}",
        titleResId = R.string.nav_contract,
        showBackButton = true,
        showHelpButton = true,
        guideXmlName = "guide_contract"
    ) {
        const val BASE_ROUTE = "contract"
        const val CONTRACT_ID_ARG = "contractId"
        // Workflow #86 — readOnly=true mở hợp đồng đã giải ngân ở chế độ chỉ xem (ẩn ký/huỷ/resend).
        const val READ_ONLY_ARG = "readOnly"
        fun createRoute(contractId: String? = null, readOnly: Boolean = false): String {
            val params = buildList {
                if (!contractId.isNullOrEmpty()) add("$CONTRACT_ID_ARG=$contractId")
                if (readOnly) add("$READ_ONLY_ARG=true")
            }
            return if (params.isEmpty()) BASE_ROUTE else "$BASE_ROUTE?${params.joinToString("&")}"
        }
    }

    data object EsignSuccess : AppDestination(
        route = "esign_success",
        titleResId = R.string.nav_esign_success,
        showBackButton = false,
        showHelpButton = false
    )

    // New Destinations from Phase 1
    data object EventDetail : AppDestination(
        route = "event_detail/{id}",
        titleResId = R.string.nav_event_detail,
        showBackButton = true,
        guideXmlName = "guide_event_detail"
    ) {
        const val BASE_ROUTE = "event_detail"
        const val ID_ARG = "id"
        fun createRoute(id: String) = "$BASE_ROUTE/$id"
    }

    data object WebContent : AppDestination(
        route = "web_content?url={url}&title={title}",
        titleResId = R.string.nav_web_content,
        showBackButton = true,
        showHelpButton = false
    ) {
        const val BASE_ROUTE = "web_content"
        const val URL_ARG = "url"
        const val TITLE_ARG = "title"

        fun createRoute(url: String, title: String? = null): String {
            val encodedUrl = Uri.encode(url)
            val encodedTitle = Uri.encode(title.orEmpty())
            return "$BASE_ROUTE?url=$encodedUrl&title=$encodedTitle"
        }
    }

    data object Rewards : AppDestination(
        route = "rewards",
        titleResId = R.string.nav_rewards,
        showBackButton = true,
        guideXmlName = "guide_rewards"
    )

    data object LoanList : AppDestination(
        route = "loan_list",
        titleResId = R.string.nav_loan_list,
        showBackButton = true,
        guideXmlName = "guide_loan_list"
    )

    data object LoanDetail : AppDestination(
        route = "loan_detail/{id}",
        titleResId = R.string.nav_loan_detail,
        showBackButton = true,
        guideXmlName = "guide_loan_detail"
    ) {
        const val BASE_ROUTE = "loan_detail"
        const val ID_ARG = "id"
        fun createRoute(id: String) = "$BASE_ROUTE/$id"
    }

    data object Profile : AppDestination(
        route = "profile",
        titleResId = R.string.nav_profile,
        showBackButton = true,
        guideXmlName = "guide_profile"
    )

    data object MoneyManagement : AppDestination(
        route = "money_management",
        titleResId = R.string.nav_money_management,
        showBackButton = true,
        guideXmlName = "guide_money_management"
    )

    data object PaymentCards : AppDestination(
        route = "payment_cards",
        titleResId = R.string.nav_payment_cards,
        showBackButton = true,
        guideXmlName = "guide_payment_cards"
    )

    data object GeneralSettings : AppDestination(
        route = "general_settings",
        titleResId = R.string.nav_general_settings,
        showBackButton = true,
        guideXmlName = "guide_general_settings"
    )

    data object SecuritySettings : AppDestination(
        route = "security_settings",
        titleResId = R.string.nav_security_settings,
        showBackButton = true,
        guideXmlName = "guide_security_settings"
    )

    data object ChangePassword : AppDestination(
        route = "change_password",
        titleResId = R.string.nav_change_password,
        showBackButton = true,
        showHelpButton = false
    )

    data object ChatBot : AppDestination(
        route = "chatbot",
        titleResId = R.string.nav_chatbot,
        showBackButton = true,
        showHelpButton = false
    )

    data object IdentityVerification : AppDestination(
        route = "identity_verification",
        titleResId = R.string.nav_identity_verification,
        showBackButton = true,
        showHelpButton = true,
        guideXmlName = "guide_identity_verification"
    )

    data object TopUp : AppDestination(
        route = "top_up",
        titleResId = R.string.nav_top_up,
        showBackButton = true,
        guideXmlName = "guide_top_up"
    )

    data object Withdraw : AppDestination(
        route = "withdraw",
        titleResId = R.string.nav_withdraw,
        showBackButton = true,
        guideXmlName = "guide_withdraw"
    )

    data object Terms : AppDestination(
        route = "terms",
        titleResId = R.string.nav_terms,
        showBackButton = true,
        showHelpButton = false
    )

    data object LoanManagement : AppDestination(
        route = "loan_management?debtId={debtId}",
        titleResId = R.string.nav_loan_management,
        showBackButton = true,
        showHelpButton = true,
        guideXmlName = "guide_loan_management"
    ) {
        const val BASE_ROUTE = "loan_management"
        const val DEBT_ID_ARG = "debtId"
        fun createRoute(debtId: String? = null): String =
            if (debtId.isNullOrBlank()) BASE_ROUTE else "$BASE_ROUTE?debtId=${Uri.encode(debtId)}"
    }

    data object EditPersonalInfo : AppDestination(
        route = "edit_personal_info",
        titleResId = R.string.nav_edit_personal_info,
        showBackButton = true,
        guideXmlName = "guide_edit_personal_info"
    )

    data object EditJobInfo : AppDestination(
        route = "edit_job_info",
        titleResId = R.string.nav_edit_job_info,
        showBackButton = true,
        guideXmlName = "guide_edit_job_info"
    )

    data object EditContactInfo : AppDestination(
        route = "edit_contact_info",
        titleResId = R.string.nav_edit_contact_info,
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
    route?.startsWith(AppDestination.ConfirmInformation.BASE_ROUTE) == true -> AppDestination.ConfirmInformation
    route?.startsWith(AppDestination.LoanFlow.BASE_ROUTE) == true -> AppDestination.LoanFlow
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
    route == AppDestination.ChangePassword.route -> AppDestination.ChangePassword
    route == AppDestination.ChatBot.route -> AppDestination.ChatBot
    route == AppDestination.IdentityVerification.route -> AppDestination.IdentityVerification
    route?.startsWith(AppDestination.LoanManagement.BASE_ROUTE) == true -> AppDestination.LoanManagement
    route == AppDestination.Terms.route -> AppDestination.Terms
    route == AppDestination.TopUp.route -> AppDestination.TopUp
    route == AppDestination.Withdraw.route -> AppDestination.Withdraw
    route == AppDestination.EditPersonalInfo.route -> AppDestination.EditPersonalInfo
    route == AppDestination.EditJobInfo.route -> AppDestination.EditJobInfo
    route == AppDestination.EditContactInfo.route -> AppDestination.EditContactInfo
    route?.startsWith(AppDestination.EventDetail.BASE_ROUTE) == true -> AppDestination.EventDetail
    route?.startsWith(AppDestination.WebContent.BASE_ROUTE) == true -> AppDestination.WebContent
    route?.startsWith(AppDestination.LoanDetail.BASE_ROUTE) == true -> AppDestination.LoanDetail
    route?.startsWith(AppDestination.PageGuide.BASE_ROUTE) == true -> AppDestination.PageGuide
    else -> AppDestination.Home
}
