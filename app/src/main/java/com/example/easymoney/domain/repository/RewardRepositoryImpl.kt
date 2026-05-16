package com.example.easymoney.domain.repository

import android.util.Log
import com.example.easymoney.data.local.AppPreferences
import com.example.easymoney.data.local.DataSourceMode
import com.example.easymoney.data.sample.SAMPLE_REWARD_CATALOG
import com.example.easymoney.data.sample.sampleUserRewards
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.RewardCatalogItem
import com.example.easymoney.domain.model.UserRewards
import kotlinx.coroutines.delay
import javax.inject.Inject

private const val TAG = "DataSource"
private const val REMOTE_NOT_READY = "Endpoint REMOTE chưa sẵn sàng — vui lòng chuyển Sandbox sang MOCK"

class RewardRepositoryImpl @Inject constructor(
    private val appPreferences: AppPreferences
) : RewardRepository {

    override suspend fun getRewardCatalogItems(): Resource<List<RewardCatalogItem>> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "RewardRepository.getRewardCatalogItems mode=$mode")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(800)
                Resource.Success(SAMPLE_REWARD_CATALOG, isFromMock = true)
            }
            DataSourceMode.REMOTE -> {
                // TODO(workflow_20): wire real /rewards/catalog endpoint
                Resource.Error(REMOTE_NOT_READY)
            }
        }
    }

    override suspend fun getRewardsCatalog(): Resource<UserRewards> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "RewardRepository.getRewardsCatalog mode=$mode")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(500)
                Resource.Success(sampleUserRewards(), isFromMock = true)
            }
            DataSourceMode.REMOTE -> {
                // TODO(workflow_20): wire real /rewards/user endpoint
                Resource.Error(REMOTE_NOT_READY)
            }
        }
    }

    override suspend fun redeemReward(itemId: String): Resource<Unit> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "RewardRepository.redeemReward mode=$mode itemId=$itemId")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(800)
                Resource.Success(Unit, isFromMock = true)
            }
            DataSourceMode.REMOTE -> {
                // TODO(workflow_20): wire real POST /rewards/redeem endpoint
                Resource.Error(REMOTE_NOT_READY)
            }
        }
    }
}
