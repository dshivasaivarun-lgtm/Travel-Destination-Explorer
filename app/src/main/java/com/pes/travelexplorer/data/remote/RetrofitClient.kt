
package com.pes.travelexplorer.data.remote

import android.util.Log
import com.pes.travelexplorer.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val TAG = "RetrofitClient"

    // Correct REST Countries v5 base URL
    private const val BASE_URL =
        "https://api.restcountries.com/countries/v5/"

    private val authInterceptor = Interceptor { chain ->

        val apiKey = BuildConfig.API_KEY.trim()

        val request = chain.request()
            .newBuilder()
            .header("Authorization", "Bearer $apiKey")
            .build()

        val response = chain.proceed(request)

        Log.d(
            TAG,
            "Request: ${request.url} | HTTP: ${response.code}"
        )

        response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: CountryApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CountryApi::class.java)
    }
}
