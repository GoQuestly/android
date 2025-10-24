package com.goquesty.data.remote.interceptor

import com.goquesty.data.local.TokenManager
import com.goquesty.data.remote.annotation.RequiresAuth
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val invocation = request.tag(Invocation::class.java)
        val requiresAuth = invocation?.method()?.getAnnotation(RequiresAuth::class.java) != null

        if (!requiresAuth) {
            return chain.proceed(request)
        }

        val token = runBlocking { tokenManager.getToken() }

        if (token.isNullOrBlank()) {
            return chain.proceed(request)
        }

        val authenticatedRequest = request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}