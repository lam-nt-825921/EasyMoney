package com.example.easymoney.domain.model

data class TransactionItem(
    val description: String,
    val transactionCode: String,
    val amount: Long,
    val balance: Long,
    val time: String,
    // Workflow #60 — timestamp là khóa sort newest-first khi có.
    // Default 0L để sample/MOCK không phải khai báo lại.
    val timestamp: Long = 0L
)

data class TransactionGroup(
    val date: String,
    val items: List<TransactionItem>
)
