package com.pes.travelexplorer.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object WikimediaClient {
    private const val TAG = "WikimediaClient"
    private const val BASE_URL = "https://commons.wikimedia.org/w/"

    private val userAgentInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val request = originalRequest.newBuilder()
            .header("User-Agent", "TravelExplorerApp/1.0 (Android Student Project; contact@travelexplorer.org)")
            .build()
        val response = chain.proceed(request)
        Log.d(TAG, "Request: ${request.url} | Status: ${response.code}")
        response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(userAgentInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    val api: WikimediaApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WikimediaApi::class.java)
    }
}
