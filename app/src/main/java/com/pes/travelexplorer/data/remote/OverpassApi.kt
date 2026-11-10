package com.pes.travelexplorer.data.remote

import com.pes.travelexplorer.data.model.OverpassResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OverpassApi {
    @GET("interpreter")
    suspend fun getTouristPlaces(
        @Query("data") query: String
    ): OverpassResponse
}
