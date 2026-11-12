package com.pes.travelexplorer.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object PlaceDbClient {
    private const val TAG = "PlaceDbClient"
    private const val BASE_URL = "https://api.placedb.org/"

    private val loggingInterceptor = Interceptor { chain ->
        val request = chain.request()
        Log.d(TAG, "Sending request to: ${request.url}")
        val response = chain.proceed(request)
        val bodyString = response.peekBody(2 * 1024 * 1024).string()
        Log.d(TAG, "Response status=${response.code} for ${request.url}")
        bodyString.chunked(3000).forEach { chunk ->
            Log.d(TAG, "BODY: $chunk")
        }
        response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: PlaceDbApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PlaceDbApi::class.java)
    }
}
