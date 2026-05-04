package com.mindnote.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPrefs internal constructor(
    private val dataStore: DataStore<Preferences>,
) {
    constructor(context: Context) : this(context.applicationContext.dataStore)

    val usernameFlow: Flow<String> = dataStore.data.map { it[USERNAME].orEmpty() }

    val onboardedFlow: Flow<Boolean> = dataStore.data.map { it[ONBOARDED] == true }

    suspend fun setUsername(name: String) {
        dataStore.edit { it[USERNAME] = name.trim() }
    }

    suspend fun setOnboarded(value: Boolean) {
        dataStore.edit { it[ONBOARDED] = value }
    }

    /** Synchronous read for cold-start nav routing. */
    fun isOnboardedBlocking(): Boolean = runBlocking { onboardedFlow.first() }

    /**
     * Stable per-install device ID. Generated on first read and persisted.
     * Sent as `X-Device-Id` on every API request so the server scopes data per device.
     */
    fun deviceIdBlocking(): String = runBlocking {
        val existing = dataStore.data.map { it[DEVICE_ID] }.first()
        existing ?: UUID.randomUUID().toString().also { generated ->
            dataStore.edit { it[DEVICE_ID] = generated }
        }
    }

    private companion object {
        val USERNAME = stringPreferencesKey("username")
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val DEVICE_ID = stringPreferencesKey("device_id")
    }
}
