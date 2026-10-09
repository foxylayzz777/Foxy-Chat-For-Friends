package com.example.data.api

import com.example.data.session.SessionManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient(private val sessionManager: SessionManager) {

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        // Attach cookie if sessionToken exists
        sessionManager.sessionToken?.let { token ->
            requestBuilder.header("Cookie", "foxy_session=$token")
        }

        val response: Response = chain.proceed(requestBuilder.build())

        // Capture session cookie from response if present
        val cookieHeaders = response.headers("Set-Cookie")
        for (cookieHeader in cookieHeaders) {
            if (cookieHeader.contains("foxy_session=")) {
                val parts = cookieHeader.split(";")
                for (part in parts) {
                    val trimmed = part.trim()
                    if (trimmed.startsWith("foxy_session=")) {
                        val token = trimmed.substring("foxy_session=".length)
                        if (token.isNotBlank()) {
                            sessionManager.sessionToken = token
                        }
                    }
                }
            }
        }

        response
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://nile.hidencloud.com:24697/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: FriendCratApiService = retrofit.create(FriendCratApiService::class.java)
}
