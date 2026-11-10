package com.pes.travelexplorer.data.remote

import com.pes.travelexplorer.data.model.OverpassResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface OverpassApi {
    @FormUrlEncoded
    @POST("interpreter")
    suspend fun getTouristPlaces(
        @Field("data") query: String
    ): OverpassResponse
}
