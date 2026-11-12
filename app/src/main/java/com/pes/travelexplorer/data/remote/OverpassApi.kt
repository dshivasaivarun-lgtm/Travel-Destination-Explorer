package com.pes.travelexplorer.data.remote

import com.pes.travelexplorer.data.model.OverpassResponse
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface OverpassApi {
    @POST("interpreter")
    suspend fun getTouristPlaces(
        @Body query: RequestBody
    ): OverpassResponse
}
