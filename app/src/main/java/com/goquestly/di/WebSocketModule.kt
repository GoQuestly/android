package com.goquestly.di

import com.goquestly.data.local.TokenManager
import com.goquestly.data.remote.websocket.LocationSocketService
import com.goquestly.data.remote.websocket.ParticipantSocketService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WebSocketModule {

    @Provides
    @Singleton
    fun provideParticipantSocketService(
        tokenManager: TokenManager,
        json: Json
    ): ParticipantSocketService = ParticipantSocketService(tokenManager, json)

    @Provides
    @Singleton
    fun provideLocationSocketService(
        tokenManager: TokenManager,
        json: Json
    ): LocationSocketService = LocationSocketService(tokenManager, json)
}
