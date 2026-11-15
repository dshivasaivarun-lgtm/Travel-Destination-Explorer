package com.pes.travelexplorer.data.repository

import android.util.Log
import com.pes.travelexplorer.data.model.OverpassResponse
import com.pes.travelexplorer.data.model.TouristPlace
import com.pes.travelexplorer.data.remote.OverpassApi
import com.pes.travelexplorer.data.remote.OverpassClient
import com.pes.travelexplorer.utils.validCoordinates
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
        private val cache = java.util.concurrent.ConcurrentHashMap<String, List<TouristPlace>>()
        private val tourismCategories = setOf(
            "attraction", "museum", "viewpoint", "artwork", "gallery", "zoo", "theme_park", "aquarium"
        )
        private val historicCategories = setOf(
            "monument", "memorial", "castle", "archaeological_site", "fort"
        )
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
                  nwr(around:$radiusMeters,$lat,$lon)[tourism~"^(attraction|museum|viewpoint|artwork|gallery|zoo|theme_park|aquarium)$"];
                  nwr(around:$radiusMeters,$lat,$lon)[historic~"^(monument|memorial|castle|archaeological_site|fort)$"];
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
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
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
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
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
            val osmType = element.type?.takeIf { it in setOf("node", "way", "relation") }
                ?: return@mapNotNull null
            val id = element.id ?: return@mapNotNull null
            val tags = element.tags ?: emptyMap()
            val name = tags["name"] ?: return@mapNotNull null

            val latitude = element.lat ?: element.center?.lat ?: return@mapNotNull null
            val longitude = element.lon ?: element.center?.lon ?: return@mapNotNull null
            if (!validCoordinates(latitude, longitude)) return@mapNotNull null

            val category = when {
                tags["tourism"] in tourismCategories -> tags.getValue("tourism")
                tags["historic"] in historicCategories -> tags.getValue("historic")
                else -> return@mapNotNull null
            }

            val address = tags["addr:full"] ?: tags["addr:street"]
            val website = tags["website"] ?: tags["contact:website"]
            val openingHours = tags["opening_hours"]

            TouristPlace(
                osmType = osmType,
                id = id,
                name = name,
                category = category.replaceFirstChar { it.uppercase() },
                latitude = latitude,
                longitude = longitude,
                address = address,
                website = website,
                openingHours = openingHours
            )
        }.distinctBy { it.osmType to it.id }
    }
}
