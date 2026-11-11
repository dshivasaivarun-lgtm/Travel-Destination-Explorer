package com.pes.travelexplorer.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object OverpassClient {
    private const val TAG = "OverpassClient"
    private const val PRIMARY_BASE_URL = "https://overpass-api.de/api/"
    private const val MIRROR_BASE_URL = "https://overpass.kumi.systems/api/"

    private val loggingInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val request = originalRequest.newBuilder()
            .header("User-Agent", "TravelExplorer-AndroidApp/1.0 (Student Project)")
            .build()
        Log.d(TAG, "Sending Overpass request to: ${request.url}")
        val response = chain.proceed(request)
        Log.d(TAG, "Received Overpass response status=${response.code}")
        response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    val api: OverpassApi by lazy {
        Retrofit.Builder()
            .baseUrl(PRIMARY_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OverpassApi::class.java)
    }

    val mirrorApi: OverpassApi by lazy {
        Retrofit.Builder()
            .baseUrl(MIRROR_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OverpassApi::class.java)
    }
}
