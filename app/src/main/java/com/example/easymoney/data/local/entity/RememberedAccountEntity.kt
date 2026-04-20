package com.example.easymoney.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.easymoney.domain.model.RememberedAccount

@Entity(tableName = "remembered_accounts")
data class RememberedAccountEntity(
    @PrimaryKey val phone: String,
    val fullName: String,
    val lastLoginTimestamp: Long
) {
    fun toDomain() = RememberedAccount(
        phone = phone,
        fullName = fullName,
        lastLoginTimestamp = lastLoginTimestamp
    )

    companion object {
        fun fromDomain(account: RememberedAccount) = RememberedAccountEntity(
            phone = account.phone,
            fullName = account.fullName,
            lastLoginTimestamp = account.lastLoginTimestamp
        )
    }
}
