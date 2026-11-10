package com.pes.travelexplorer.data.remote

import com.pes.travelexplorer.data.model.CityResponse
import com.pes.travelexplorer.data.model.RegionResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface PlaceDbApi {
    @GET("v1/regions/{countryCode}.json")
    suspend fun getRegions(@Path("countryCode") countryCode: String): RegionResponse

    @GET("v1/cities/{countryCode}/{admin1Id}.json")
    suspend fun getCities(
        @Path("countryCode") countryCode: String,
        @Path("admin1Id") admin1Id: String
    ): CityResponse
}
