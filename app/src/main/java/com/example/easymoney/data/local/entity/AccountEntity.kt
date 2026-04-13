package com.example.easymoney.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val accountNumber: String,
    val bankName: String,
    val ownerName: String,
    val balance: Double = 0.0,
    val isActive: Boolean = true,
    val currency: String = "VND",
    val lastUpdate: Long = System.currentTimeMillis()
)
