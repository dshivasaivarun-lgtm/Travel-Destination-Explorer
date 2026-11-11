package com.pes.travelexplorer.data.repository

import android.util.Log
import com.pes.travelexplorer.data.model.TouristPlace
import com.pes.travelexplorer.data.remote.OverpassApi
import com.pes.travelexplorer.data.remote.OverpassClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException

class TouristPlaceRepository(
    private val api: OverpassApi = OverpassClient.api
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
            try {
                // Optimized Overpass QL query using indexed tags for fast response
                val query = """
                    [out:json][timeout:45];
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
                val response = api.getTouristPlaces(requestBody)
                val elements = response.elements ?: emptyList()

                val places = elements.mapNotNull { element ->
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

                cache[cacheKey] = places
                Result.success(places)
            } catch (e: HttpException) {
                val message = when (e.code()) {
                    429 -> "Rate limit exceeded (HTTP 429). Please wait a moment before trying again."
                    504 -> "Overpass server timeout (HTTP 504). Please try again later."
                    else -> "Network error (${e.code()}): ${e.message}"
                }
                Log.e(TAG, message, e)
                Result.failure(Exception(message, e))
            } catch (e: IOException) {
                val message = "Server timeout or network connection error. Please try again."
                Log.e(TAG, message, e)
                Result.failure(Exception(message, e))
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error fetching tourist places: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
}
