package com.example.easymoney.domain.repository

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.TransactionGroup

interface TransactionHistoryRepository {
    suspend fun getTransactionHistory(): Resource<List<TransactionGroup>>
}
