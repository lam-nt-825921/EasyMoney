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
    version = 2,
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
