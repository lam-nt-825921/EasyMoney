package com.example.easymoney.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.easymoney.data.local.dao.AccountDao
import com.example.easymoney.data.local.dao.NotificationDao
import com.example.easymoney.data.local.entity.AccountEntity
import com.example.easymoney.data.local.entity.NotificationEntity

@Database(
    entities = [NotificationEntity::class, AccountEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
    abstract fun accountDao(): AccountDao

    companion object {
        const val DATABASE_NAME = "easy_money_db"
    }
}
