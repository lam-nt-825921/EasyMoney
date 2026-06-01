package com.example.easymoney.data.remote.dto

import com.example.easymoney.domain.model.TransactionGroup
import com.example.easymoney.domain.model.TransactionItem
import com.google.gson.annotations.SerializedName

/**
 * Workflow #60 — DTO riêng cho `/api/v1/transactions`, bao gồm field `timestamp`
 * mà domain trước đây bỏ qua. Backend có thể trả các nhóm/items không sort —
 * mapper bảo đảm newest-first ở cả group lẫn item.
 */
data class TransactionGroupDto(
    @SerializedName("date") val date: String? = null,
    @SerializedName("items") val items: List<TransactionItemDto> = emptyList()
)

data class TransactionItemDto(
    @SerializedName("description") val description: String? = null,
    @SerializedName("transaction_code") val transactionCode: String? = null,
    @SerializedName("amount") val amount: Double? = null,
    @SerializedName("balance") val balance: Double? = null,
    @SerializedName("time") val time: String? = null,
    @SerializedName("timestamp") val timestamp: Long = 0L
)

fun TransactionItemDto.toDomain(): TransactionItem = TransactionItem(
    description = description.orEmpty(),
    transactionCode = transactionCode.orEmpty(),
    amount = (amount ?: 0.0).toLong(),
    balance = (balance ?: 0.0).toLong(),
    time = time.orEmpty(),
    timestamp = timestamp
)

fun List<TransactionGroupDto>.toSortedDomain(): List<TransactionGroup> {
    val mapped = map { dto ->
        val sortedItems = dto.items
            .map { it.toDomain() }
            .sortedByDescending { it.timestamp }
        TransactionGroup(
            date = dto.date.orEmpty(),
            items = sortedItems
        )
    }
    return mapped.sortedByDescending { group ->
        group.items.maxOfOrNull { it.timestamp } ?: 0L
    }
}
