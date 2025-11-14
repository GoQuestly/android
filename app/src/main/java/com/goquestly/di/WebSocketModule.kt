package com.goquestly.di

import com.goquestly.data.local.TokenManager
import com.goquestly.data.remote.websocket.ActiveSessionSocketService
import com.goquestly.data.remote.websocket.SessionEventsSocketService
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
    fun provideSessionEventsSocketService(
        tokenManager: TokenManager,
        json: Json
    ): SessionEventsSocketService = SessionEventsSocketService(tokenManager, json)

    @Provides
    @Singleton
    fun provideActiveSessionSocketService(
        tokenManager: TokenManager,
        json: Json
    ): ActiveSessionSocketService = ActiveSessionSocketService(tokenManager, json)
}
