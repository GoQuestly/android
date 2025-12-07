package com.goquestly.data.local

import android.content.Context
import android.os.SystemClock
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private val Context.serverTimeDataStore by preferencesDataStore(name = "server_time")

@OptIn(ExperimentalTime::class)
@Singleton
class ServerTimeManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val timeOffsetKey = longPreferencesKey("time_offset")

    suspend fun saveTimeOffset(serverTime: Instant) {
        val elapsedRealtime = SystemClock.elapsedRealtime()
        val offset = serverTime.toEpochMilliseconds() - elapsedRealtime

        context.serverTimeDataStore.edit { preferences ->
            preferences[timeOffsetKey] = offset
        }
    }

    suspend fun getCurrentServerTime(): Instant {
        val offset = context.serverTimeDataStore.data
            .map { preferences -> preferences[timeOffsetKey] }
            .first()

        return if (offset != null) {
            val elapsedRealtime = SystemClock.elapsedRealtime()
            val serverTimeMillis = elapsedRealtime + offset
            Instant.fromEpochMilliseconds(serverTimeMillis)
        } else {
            Clock.System.now()
        }
    }
}
