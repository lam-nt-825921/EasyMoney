package com.example.easymoney.domain.repository

import com.example.easymoney.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getActiveAccount(): Flow<AccountEntity?>
    suspend fun saveAccount(account: AccountEntity)
    suspend fun updateBalance(newBalance: Double)
    suspend fun clearAccountData()
}
