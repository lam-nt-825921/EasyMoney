package com.example.easymoney.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.easymoney.data.local.dao.AccountDao
import com.example.easymoney.data.local.dao.NotificationDao
import com.example.easymoney.data.local.dao.RememberedAccountDao
import com.example.easymoney.data.local.entity.AccountEntity
import com.example.easymoney.data.local.entity.NotificationEntity
import com.example.easymoney.data.local.entity.RememberedAccountEntity

@Database(
    entities = [NotificationEntity::class, AccountEntity::class, RememberedAccountEntity::class],
    // Workflow #83 — bump cho `remoteId` mới (destructive migration đã bật trong DatabaseModule).
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
    abstract fun accountDao(): AccountDao
    abstract fun rememberedAccountDao(): RememberedAccountDao

    companion object {
        const val DATABASE_NAME = "easy_money_db"
    }
}
