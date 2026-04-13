package com.example.easymoney.data.local.dao

import androidx.room.*
import com.example.easymoney.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE userId = :userId LIMIT 1")
    fun getAccountByUserId(userId: String): Flow<AccountEntity?>

    @Query("SELECT * FROM accounts WHERE isActive = 1 LIMIT 1")
    fun getActiveAccount(): Flow<AccountEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("UPDATE accounts SET balance = :newBalance, lastUpdate = :timestamp WHERE id = :id")
    suspend fun updateBalance(id: Long, newBalance: Double, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("DELETE FROM accounts")
    suspend fun clearAll()
}
