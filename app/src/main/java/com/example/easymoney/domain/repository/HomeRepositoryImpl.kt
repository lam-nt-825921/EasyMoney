package com.example.easymoney.domain.repository

import android.util.Log
import com.example.easymoney.data.local.AppPreferences
import com.example.easymoney.data.local.DataSourceMode
import com.example.easymoney.data.sample.SAMPLE_BANNERS
import com.example.easymoney.data.sample.SAMPLE_EKYC_STATUS
import com.example.easymoney.data.sample.SAMPLE_HOT_LOANS
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.Banner
import com.example.easymoney.domain.model.EKycStatus
import com.example.easymoney.domain.model.LoanProduct
import kotlinx.coroutines.delay
import javax.inject.Inject

private const val TAG = "DataSource"
private const val REMOTE_NOT_READY = "Endpoint REMOTE chưa sẵn sàng — vui lòng chuyển Sandbox sang MOCK"

class HomeRepositoryImpl @Inject constructor(
    private val appPreferences: AppPreferences
) : HomeRepository {

    override suspend fun getBanners(): Resource<List<Banner>> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "HomeRepository.getBanners mode=$mode")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(500)
                Resource.Success(SAMPLE_BANNERS, isFromMock = true)
            }
            DataSourceMode.REMOTE -> {
                // TODO(workflow_20): wire real /home/banners endpoint khi backend sẵn sàng
                Resource.Error(REMOTE_NOT_READY)
            }
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
            DataSourceMode.REMOTE -> {
                // TODO(workflow_20): wire real /home/hot-loans endpoint
                Resource.Error(REMOTE_NOT_READY)
            }
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
            DataSourceMode.REMOTE -> {
                // TODO(workflow_20): wire real /ekyc/status endpoint
                Resource.Error(REMOTE_NOT_READY)
            }
        }
    }
}
