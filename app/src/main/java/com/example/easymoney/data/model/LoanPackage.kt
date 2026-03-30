package com.example.easymoney.data.model

import android.os.Parcel
import android.os.Parcelable

data class LoanPackage(
    val id: String,
    val packageName: String,
    val tenorRange: String, // format "6,12,18,24" or "6-24"
    val minAmount: Long,
    val maxAmount: Long,
    val interest: Double,
    val overdueCost: Double,
    val eligibleCreditScore: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readLong(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(packageName)
        parcel.writeString(tenorRange)
        parcel.writeLong(minAmount)
        parcel.writeLong(maxAmount)
        parcel.writeDouble(interest)
        parcel.writeDouble(overdueCost)
        parcel.writeInt(eligibleCreditScore)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<LoanPackage> {
        override fun createFromParcel(parcel: Parcel): LoanPackage = LoanPackage(parcel)
        override fun newArray(size: Int): Array<LoanPackage?> = arrayOfNulls(size)
    }

    fun getTenorList(): List<Int> {
        return try {
            tenorRange.split(",").map { it.trim().toInt() }
        } catch (e: Exception) {
            listOf(6, 12, 18, 24) // Default fallback
        }
    }
}
