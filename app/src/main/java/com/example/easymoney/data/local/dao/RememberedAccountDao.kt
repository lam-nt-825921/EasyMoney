package com.example.easymoney.data.local.dao

import androidx.room.*
import com.example.easymoney.data.local.entity.RememberedAccountEntity

@Dao
interface RememberedAccountDao {
    @Query("SELECT * FROM remembered_accounts ORDER BY lastLoginTimestamp DESC")
    suspend fun getAll(): List<RememberedAccountEntity>

    @Query("SELECT * FROM remembered_accounts ORDER BY lastLoginTimestamp DESC LIMIT 1")
    suspend fun getLastAccount(): RememberedAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: RememberedAccountEntity)

    @Query("DELETE FROM remembered_accounts WHERE phone = :phone")
    suspend fun deleteByPhone(phone: String)

    @Query("DELETE FROM remembered_accounts")
    suspend fun deleteAll()
}
