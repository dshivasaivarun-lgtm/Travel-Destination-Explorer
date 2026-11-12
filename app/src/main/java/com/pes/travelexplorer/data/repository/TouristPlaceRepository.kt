package com.pes.travelexplorer.data.repository

import android.util.Log
import com.pes.travelexplorer.data.model.OverpassResponse
import com.pes.travelexplorer.data.model.TouristPlace
import com.pes.travelexplorer.data.remote.OverpassApi
import com.pes.travelexplorer.data.remote.OverpassClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

class TouristPlaceRepository(
    private val api: OverpassApi = OverpassClient.api,
    private val mirrorApi: OverpassApi = OverpassClient.mirrorApi
) {
    companion object {
        private const val TAG = "TouristPlaceRepository"
        private val cache = mutableMapOf<String, List<TouristPlace>>()
    }

    suspend fun getTouristPlaces(
        lat: Double,
        lon: Double,
        radiusMeters: Int = 5000
    ): Result<List<TouristPlace>> {
        val cacheKey = "${lat}_${lon}_$radiusMeters"
        cache[cacheKey]?.let { cached ->
            Log.d(TAG, "Returning cached tourist places for key: $cacheKey")
            return Result.success(cached)
        }

        return withContext(Dispatchers.IO) {
            val query = """
                [out:json][timeout:35];
                (
                  node(around:$radiusMeters,$lat,$lon)[tourism];
                  node(around:$radiusMeters,$lat,$lon)[historic];
                  node(around:$radiusMeters,$lat,$lon)[leisure=park];
                  way(around:$radiusMeters,$lat,$lon)[tourism];
                  way(around:$radiusMeters,$lat,$lon)[historic];
                  way(around:$radiusMeters,$lat,$lon)[leisure=park];
                );
                out center;
            """.trimIndent()

            val requestBody = query.toRequestBody("text/plain; charset=utf-8".toMediaTypeOrNull())

            var primaryError: Exception? = null

            // Try primary server
            try {
                val response = api.getTouristPlaces(requestBody)
                val places = parsePlaces(response)
                cache[cacheKey] = places
                return@withContext Result.success(places)
            } catch (e: Exception) {
                Log.w(TAG, "Primary Overpass server failed (${e.message}), attempting mirror server...")
                primaryError = e
            }

            // Fallback to secondary mirror server
            try {
                val response = mirrorApi.getTouristPlaces(requestBody)
                val places = parsePlaces(response)
                cache[cacheKey] = places
                return@withContext Result.success(places)
            } catch (e: Exception) {
                Log.e(TAG, "Mirror Overpass server also failed: ${e.message}", e)
                val err = primaryError ?: e
                val message = when {
                    err is HttpException && err.code() == 502 ->
                        "Overpass server is temporarily busy (HTTP 502 Bad Gateway). Please tap Retry in a few seconds."
                    err is HttpException && err.code() == 429 ->
                        "Rate limit exceeded (HTTP 429). Please wait a moment."
                    else -> err.localizedMessage ?: "Network error occurred. Please try again."
                }
                Result.failure(Exception(message, err))
            }
        }
    }

    private fun parsePlaces(response: OverpassResponse): List<TouristPlace> {
        val elements = response.elements ?: emptyList()
        return elements.mapNotNull { element ->
            val tags = element.tags ?: emptyMap()
            val name = tags["name"] ?: return@mapNotNull null

            val latitude = element.lat ?: element.center?.lat ?: return@mapNotNull null
            val longitude = element.lon ?: element.center?.lon ?: return@mapNotNull null

            val category = tags["tourism"] ?: tags["historic"] ?: tags["leisure"] ?: "Attraction"

            val address = tags["addr:full"] ?: tags["addr:street"]
            val website = tags["website"] ?: tags["contact:website"]
            val openingHours = tags["opening_hours"]

            TouristPlace(
                id = element.id ?: 0L,
                name = name,
                category = category.replaceFirstChar { it.uppercase() },
                latitude = latitude,
                longitude = longitude,
                address = address,
                website = website,
                openingHours = openingHours
            )
        }.distinctBy { it.id }
    }
}
