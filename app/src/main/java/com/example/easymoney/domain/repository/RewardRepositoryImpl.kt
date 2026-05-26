package com.example.easymoney.domain.repository

import android.util.Log
import com.example.easymoney.data.local.AppPreferences
import com.example.easymoney.data.local.DataSourceMode
import com.example.easymoney.data.remote.RewardRemoteDataSource
import com.example.easymoney.data.sample.SAMPLE_REWARD_CATALOG
import com.example.easymoney.data.sample.sampleUserRewards
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.RewardCatalogItem
import com.example.easymoney.domain.model.UserRewards
import kotlinx.coroutines.delay
import javax.inject.Inject

private const val TAG = "DataSource"

class RewardRepositoryImpl @Inject constructor(
    private val appPreferences: AppPreferences,
    private val remoteDataSource: RewardRemoteDataSource
) : RewardRepository {

    override suspend fun getRewardCatalogItems(): Resource<List<RewardCatalogItem>> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "RewardRepository.getRewardCatalogItems mode=$mode")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(800)
                Resource.Success(SAMPLE_REWARD_CATALOG, isFromMock = true)
            }
            DataSourceMode.REMOTE -> remoteDataSource.getRewardCatalogItems()
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
            DataSourceMode.REMOTE -> remoteDataSource.getRewardsCatalog()
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
            DataSourceMode.REMOTE -> remoteDataSource.redeemReward(itemId)
        }
    }
}
