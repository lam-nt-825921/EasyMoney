package com.example.easymoney.domain.repository

import android.util.Log
import com.example.easymoney.data.local.AppPreferences
import com.example.easymoney.data.local.DataSourceMode
import com.example.easymoney.data.remote.HomeRemoteDataSource
import com.example.easymoney.data.sample.SAMPLE_BANNERS
import com.example.easymoney.data.sample.SAMPLE_EKYC_STATUS
import com.example.easymoney.data.sample.SAMPLE_HOT_LOANS
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.Banner
import com.example.easymoney.domain.model.EKycStatus
import com.example.easymoney.domain.model.LoanProduct
import com.example.easymoney.domain.model.SupportLink
import kotlinx.coroutines.delay
import javax.inject.Inject

private const val TAG = "DataSource"

class HomeRepositoryImpl @Inject constructor(
    private val appPreferences: AppPreferences,
    private val remoteDataSource: HomeRemoteDataSource
) : HomeRepository {

    override suspend fun getBanners(): Resource<List<Banner>> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "HomeRepository.getBanners mode=$mode")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(500)
                Resource.Success(SAMPLE_BANNERS, isFromMock = true)
            }
            DataSourceMode.REMOTE -> remoteDataSource.getBanners()
        }
    }

    override suspend fun getHotLoans(): Resource<List<LoanProduct>> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "HomeRepository.getHotLoans mode=$mode")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(500)
                Resource.Success(SAMPLE_HOT_LOANS, isFromMock = true)
            }
            DataSourceMode.REMOTE -> remoteDataSource.getHotLoans()
        }
    }

    override suspend fun getEKycStatus(): Resource<EKycStatus> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "HomeRepository.getEKycStatus mode=$mode")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(300)
                Resource.Success(SAMPLE_EKYC_STATUS, isFromMock = true)
            }
            DataSourceMode.REMOTE -> remoteDataSource.getEKycStatus()
        }
    }

    override suspend fun getCustomerSupportLink(): Resource<SupportLink> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "HomeRepository.getCustomerSupportLink mode=$mode")
        return when (mode) {
            DataSourceMode.MOCK -> Resource.Success(
                SupportLink(
                    title = "Chăm sóc khách hàng",
                    path = "/cskh",
                    url = appPreferences.apiBaseUrl.trimEnd('/') + "/cskh",
                    phone = "19001234",
                    email = "cskh@lamgd.dev",
                    availableTime = "24/7"
                ),
                isFromMock = true
            )
            DataSourceMode.REMOTE -> remoteDataSource.getCustomerSupportLink()
        }
    }
}
