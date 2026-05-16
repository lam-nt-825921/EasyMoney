package com.example.easymoney.domain.model

data class TransactionItem(
    val description: String,
    val transactionCode: String,
    val amount: Long,
    val balance: Long,
    val time: String
)

data class TransactionGroup(
    val date: String,
    val items: List<TransactionItem>
)
