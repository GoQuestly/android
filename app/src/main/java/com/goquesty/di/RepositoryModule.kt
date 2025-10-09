package com.goquesty.di

import com.goquesty.data.repository.AuthRepositoryImpl
import com.goquesty.domain.repository.AuthRepository
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
    abstract fun bindUserRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}
