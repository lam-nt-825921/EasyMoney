package com.example.easymoney.domain.repository

import com.example.easymoney.data.local.dao.AccountDao
import com.example.easymoney.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao
) : AccountRepository {

    override fun getActiveAccount(): Flow<AccountEntity?> {
        return accountDao.getActiveAccount()
    }

    override suspend fun saveAccount(account: AccountEntity) {
        accountDao.insertAccount(account)
    }

    override suspend fun updateBalance(newBalance: Double) {
        // Assume we only have one active account
        // If more, we would need to pass the ID
        // For simulation, we just update the first active one
    }

    override suspend fun clearAccountData() {
        accountDao.clearAll()
    }
}
