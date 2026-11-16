package com.pes.travelexplorer.data.remote

import com.pes.travelexplorer.data.model.WikimediaQueryResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WikimediaApi {
    @GET("api.php")
    suspend fun searchPhotos(
        @Query("action") action: String = "query",
        @Query("generator") generator: String = "search",
        @Query("gsrnamespace") gsrnamespace: Int = 6,
        @Query("gsrsearch") gsrsearch: String,
        @Query("gsrlimit") gsrlimit: Int = 10,
        @Query("prop") prop: String = "imageinfo",
        @Query("iiprop") iiprop: String = "url|user|extmetadata",
        @Query("format") format: String = "json"
    ): WikimediaQueryResponse
}
