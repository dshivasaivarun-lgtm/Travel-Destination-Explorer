package com.pes.travelexplorer.data.remote

import com.pes.travelexplorer.data.model.CountryResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface CountryApi {
    @GET("all")
    suspend fun getAllCountries(): CountryResponse

    @GET("names.common/{name}")
    suspend fun getCountryByName(@Path("name") name: String): CountryResponse

    @GET("codes.alpha_3/{code}")
    suspend fun getCountryByCode(@Path("code") code: String): CountryResponse

    @GET("region/{region}")
    suspend fun getCountriesByRegion(@Path("region") region: String): CountryResponse
}
