package com.pes.travelexplorer.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object OverpassClient {
    private const val TAG = "OverpassClient"
    private const val BASE_URL = "https://overpass-api.de/api/"

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
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: OverpassApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OverpassApi::class.java)
    }
}
