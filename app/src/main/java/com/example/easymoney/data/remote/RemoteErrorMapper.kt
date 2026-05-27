package com.example.easymoney.data.remote

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.HttpException

fun userFriendlyErrorMessage(throwable: Throwable, fallback: String = NETWORK_ERROR_MESSAGE): String {
    return when (throwable) {
        is HttpException -> userFriendlyHttpExceptionMessage(throwable)
        is UnknownHostException -> NO_INTERNET_MESSAGE
        is SocketTimeoutException -> TIMEOUT_MESSAGE
        is IOException -> NETWORK_ERROR_MESSAGE
        else -> userFriendlyErrorMessage(throwable.message ?: fallback, fallback)
    }
}

private fun userFriendlyHttpExceptionMessage(exception: HttpException): String {
    val body = exception.response()?.errorBody()?.string().orEmpty()
    if (body.isNotBlank()) {
        extractBackendDetailMessage(body)?.let { return it }
    }
    return userFriendlyHttpMessage(exception.code())
}

private fun extractBackendDetailMessage(body: String): String? {
    if ("CARD_REQUIRED" in body || "NAVIGATE_ADD_CARD" in body) {
        val message = Regex(""""message"\s*:\s*"([^"]+)"""")
            .find(body)
            ?.groupValues
            ?.getOrNull(1)
        return listOfNotNull(message, "NAVIGATE_ADD_CARD").joinToString(" | ")
    }
    return Regex(""""detail"\s*:\s*"([^"]+)"""")
        .find(body)
        ?.groupValues
        ?.getOrNull(1)
}

fun userFriendlyErrorMessage(rawMessage: String?, fallback: String = UNKNOWN_ERROR_MESSAGE): String {
    val message = rawMessage?.trim().orEmpty()
    if (message.isBlank()) return fallback.toFriendlyFallback()

    httpStatusRegex.find(message)?.groupValues?.getOrNull(1)?.toIntOrNull()?.let { statusCode ->
        return userFriendlyHttpMessage(statusCode)
    }

    val normalized = message.lowercase()
    return when {
        "unauthorized" in normalized || "unauthenticated" in normalized || "token" in normalized && "expired" in normalized ->
            SESSION_EXPIRED_MESSAGE
        "forbidden" in normalized -> FORBIDDEN_MESSAGE
        "timeout" in normalized || "timed out" in normalized -> TIMEOUT_MESSAGE
        "unable to resolve host" in normalized || "failed to connect" in normalized || "connection reset" in normalized ->
            NO_INTERNET_MESSAGE
        "service unavailable" in normalized -> SERVICE_UNAVAILABLE_MESSAGE
        "internal server error" in normalized -> SERVER_ERROR_MESSAGE
        else -> message
    }
}

private fun userFriendlyHttpMessage(statusCode: Int): String {
    return when (statusCode) {
        400 -> "Yêu cầu chưa hợp lệ. Vui lòng kiểm tra lại thông tin."
        401 -> SESSION_EXPIRED_MESSAGE
        403 -> FORBIDDEN_MESSAGE
        404 -> "Không tìm thấy dữ liệu cần xử lý."
        408 -> TIMEOUT_MESSAGE
        409, 422 -> "Thông tin chưa hợp lệ hoặc trạng thái hiện tại không cho phép tiếp tục."
        429 -> "Bạn đang thao tác quá nhanh. Vui lòng thử lại sau ít phút."
        500 -> SERVER_ERROR_MESSAGE
        502, 503, 504 -> SERVICE_UNAVAILABLE_MESSAGE
        else -> if (statusCode >= 500) SERVER_ERROR_MESSAGE else UNKNOWN_ERROR_MESSAGE
    }
}

private fun String.toFriendlyFallback(): String {
    val normalized = lowercase()
    return when {
        "login" in normalized -> "Không thể đăng nhập. Vui lòng thử lại."
        "register" in normalized -> "Không thể đăng ký tài khoản. Vui lòng thử lại."
        "profile" in normalized -> "Không thể tải thông tin hồ sơ. Vui lòng thử lại."
        "otp" in normalized -> "Không thể xử lý mã OTP. Vui lòng thử lại."
        "contract" in normalized -> "Không thể tải thông tin hợp đồng. Vui lòng thử lại."
        "loan" in normalized -> "Không thể tải thông tin khoản vay. Vui lòng thử lại."
        "wallet" in normalized || "card" in normalized || "payment" in normalized -> "Không thể xử lý thông tin thanh toán. Vui lòng thử lại."
        "reward" in normalized -> "Không thể tải thông tin ưu đãi. Vui lòng thử lại."
        "notification" in normalized -> "Không thể xử lý thông báo. Vui lòng thử lại."
        "message" in normalized || "chat" in normalized -> "Không thể gửi tin nhắn. Vui lòng thử lại."
        else -> UNKNOWN_ERROR_MESSAGE
    }
}

private val httpStatusRegex = Regex("""\b(?:HTTP\s*)?([1-5]\d{2})\b""", RegexOption.IGNORE_CASE)

private const val SESSION_EXPIRED_MESSAGE = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
private const val FORBIDDEN_MESSAGE = "Tài khoản của bạn không có quyền thực hiện thao tác này."
private const val SERVER_ERROR_MESSAGE = "Hệ thống đang gặp sự cố. Vui lòng thử lại sau."
private const val SERVICE_UNAVAILABLE_MESSAGE = "Dịch vụ đang tạm thời gián đoạn. Vui lòng thử lại sau."
private const val TIMEOUT_MESSAGE = "Kết nối quá thời gian chờ. Vui lòng thử lại."
private const val NO_INTERNET_MESSAGE = "Không thể kết nối máy chủ. Vui lòng kiểm tra kết nối mạng."
private const val NETWORK_ERROR_MESSAGE = "Kết nối mạng không ổn định. Vui lòng thử lại."
private const val UNKNOWN_ERROR_MESSAGE = "Đã có lỗi xảy ra. Vui lòng thử lại."
