package com.imnotndesh.truehub.data.helpers

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.imnotndesh.truehub.data.models.Apps
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


object WidgetDataStore {
    private val UPGRADABLE_APPS_KEY = stringPreferencesKey("widget_upgradable_apps_json")
    private val LAST_SYNC_KEY       = longPreferencesKey("widget_last_sync_epoch")

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(
        List::class.java, Apps.AppQueryResponse::class.java
    )
    private val adapter = moshi.adapter<List<Apps.AppQueryResponse>>(listType)

    fun upgradableAppsFlow(context: Context): Flow<List<Apps.AppQueryResponse>> =
        context.dataStore.data
            .map { prefs ->
                val json = prefs[UPGRADABLE_APPS_KEY] ?: return@map emptyList()
                runCatching { adapter.fromJson(json) ?: emptyList() }.getOrDefault(emptyList())
            }
            .catch { emit(emptyList()) }

    suspend fun saveUpgradableApps(
        context: Context,
        apps: List<Apps.AppQueryResponse>
    ) {
        val upgradable = apps.filter { it.upgrade_available }
        context.dataStore.edit { prefs ->
            prefs[UPGRADABLE_APPS_KEY] = adapter.toJson(upgradable)
            prefs[LAST_SYNC_KEY]       = System.currentTimeMillis()
        }
    }

    suspend fun getLastSyncEpoch(context: Context): Long {
        val prefs = context.dataStore.data.first()
        return prefs[LAST_SYNC_KEY] ?: 0L
    }
}