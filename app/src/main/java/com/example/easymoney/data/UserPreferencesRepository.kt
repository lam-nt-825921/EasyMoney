package com.example.easymoney.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {
    companion object {
        val CUSTOMER_ID = stringPreferencesKey("customer_id")
        val CUSTOMER_NAME = stringPreferencesKey("customer_name")
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
    }

    val customerId: Flow<String?> = context.dataStore.data.map { it[CUSTOMER_ID] }
    val customerName: Flow<String?> = context.dataStore.data.map { it[CUSTOMER_NAME] }

    suspend fun saveUserInfo(id: String, name: String, token: String) {
        context.dataStore.edit { preferences ->
            preferences[CUSTOMER_ID] = id
            preferences[CUSTOMER_NAME] = name
            preferences[AUTH_TOKEN] = token
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
