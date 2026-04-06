package com.example.easymoney.domain.repository

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.LoanPackageModel
import com.example.easymoney.domain.model.LoanProviderInfoModel
import com.example.easymoney.domain.model.MyInfoModel
import kotlinx.coroutines.delay
import javax.inject.Inject

class LoanRepositoryImpl @Inject constructor() : LoanRepository {

    private val myPackageId = "1"

    private val mockLoanPackages = listOf(
        LoanPackageModel(
            id = "1",
            packageName = "Vay Nhanh",
            tenorRange = "6,12,18,24",
            minAmount = 6_000_000L,
            maxAmount = 100_000_000L,
            interest = 12.0,
            overdueCost = 5.0,
            eligibleCreditScore = 600
        ),
        LoanPackageModel(
            id = "2",
            packageName = "Vay Linh Hoat",
            tenorRange = "3,6,9,12",
            minAmount = 3_000_000L,
            maxAmount = 50_000_000L,
            interest = 10.5,
            overdueCost = 4.0,
            eligibleCreditScore = 550
        )
    )

    private val mockMyInfo = MyInfoModel(
        fullName = "Nguyen Duc Minh",
        gender = "Nam",
        dateOfBirth = "01/05/2005",
        phoneNumber = "0936-552-900",
        nationalId = "093201403413",
        issueDate = "03/11/2020"
    )

    private val mockLoanProviderInfo = LoanProviderInfoModel(
        organizationName = "To chuc tai chinh EASY MONEY",
        hotline = "9999 9999",
        address = "114 Xuan Thuy, Phuong Cau Giay, Thanh pho Ha Noi."
    )

    override suspend fun getLoanPackageById(id: String): Resource<LoanPackageModel> {
        delay(300)

        if (id.isBlank()) {
            return Resource.Error(message = "Loan package id is empty")
        }

        val packageData = mockLoanPackages.firstOrNull { it.id == id }
            ?: return Resource.Error(message = "Loan package not found")

        return Resource.Success(data = packageData, isFromMock = true)
    }

    override suspend fun getMyPackage(): Resource<LoanPackageModel> {
        delay(300)

        val packageData = mockLoanPackages.firstOrNull { it.id == myPackageId }
            ?: return Resource.Error(message = "My loan package not found")

        return Resource.Success(data = packageData, isFromMock = true)
    }

    override suspend fun getMyInfo(): Resource<MyInfoModel> {
        delay(300)

        return Resource.Success(data = mockMyInfo, isFromMock = true)
    }

    override suspend fun getLoanProviderInfo(): Resource<LoanProviderInfoModel> {
        delay(300)

        return Resource.Success(data = mockLoanProviderInfo, isFromMock = true)
    }
}