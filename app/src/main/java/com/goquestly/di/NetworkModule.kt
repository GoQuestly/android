package com.goquestly.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.goquestly.data.remote.ApiService
import com.goquestly.data.remote.interceptor.AuthInterceptor
import com.goquestly.util.API_BASE_URL
import com.goquestly.util.HTTP_REQUEST_TIMEOUT_SECONDS
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        authInterceptor: AuthInterceptor,
    ) = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(ChuckerInterceptor.Builder(context).build())
        .connectTimeout(HTTP_REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(HTTP_REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(HTTP_REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()


    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ) = Retrofit.Builder()
        .baseUrl(API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()


    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit) = retrofit.create(ApiService::class.java)
}
