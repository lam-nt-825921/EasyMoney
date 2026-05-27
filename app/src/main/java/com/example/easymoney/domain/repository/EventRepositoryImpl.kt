package com.example.easymoney.domain.repository

import android.util.Log
import com.example.easymoney.data.local.AppPreferences
import com.example.easymoney.data.local.DataSourceMode
import com.example.easymoney.data.remote.EventRemoteDataSource
import com.example.easymoney.data.sample.sampleEvent
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.Event
import kotlinx.coroutines.delay
import javax.inject.Inject

private const val TAG = "DataSource"

class EventRepositoryImpl @Inject constructor(
    private val appPreferences: AppPreferences,
    private val remoteDataSource: EventRemoteDataSource
) : EventRepository {

    override suspend fun getEventDetail(id: String): Resource<Event> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "EventRepository.getEventDetail mode=$mode id=$id")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(500)
                Resource.Success(sampleEvent(id), isFromMock = true)
            }
            DataSourceMode.REMOTE -> remoteDataSource.getEventDetail(id)
        }
    }

    override suspend fun joinEvent(id: String): Resource<Unit> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "EventRepository.joinEvent mode=$mode id=$id")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(300)
                Resource.Success(Unit, isFromMock = true)
            }
            DataSourceMode.REMOTE -> remoteDataSource.joinEvent(id)
        }
    }
}
