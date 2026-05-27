package com.example.easymoney.domain.repository

import android.util.Log
import com.example.easymoney.data.local.AppPreferences
import com.example.easymoney.data.local.DataSourceMode
import com.example.easymoney.data.remote.TransactionHistoryRemoteDataSource
import com.example.easymoney.data.sample.SAMPLE_TRANSACTION_HISTORY
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.TransactionGroup
import kotlinx.coroutines.delay
import javax.inject.Inject

private const val TAG = "DataSource"

class TransactionHistoryRepositoryImpl @Inject constructor(
    private val appPreferences: AppPreferences,
    private val remoteDataSource: TransactionHistoryRemoteDataSource
) : TransactionHistoryRepository {

    override suspend fun getTransactionHistory(): Resource<List<TransactionGroup>> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "TransactionHistoryRepository.getTransactionHistory mode=$mode")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(400)
                Resource.Success(SAMPLE_TRANSACTION_HISTORY, isFromMock = true)
            }
            DataSourceMode.REMOTE -> remoteDataSource.getTransactionHistory()
        }
    }
}
