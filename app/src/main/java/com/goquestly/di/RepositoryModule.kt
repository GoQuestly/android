package com.goquestly.di

import com.goquestly.data.repository.AuthRepositoryImpl
import com.goquestly.data.repository.NotificationRepositoryImpl
import com.goquestly.data.repository.SessionRepositoryImpl
import com.goquestly.data.repository.UserRepositoryImpl
import com.goquestly.domain.repository.AuthRepository
import com.goquestly.domain.repository.NotificationRepository
import com.goquestly.domain.repository.SessionRepository
import com.goquestly.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        sessionRepositoryImpl: SessionRepositoryImpl
    ): SessionRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository
}
