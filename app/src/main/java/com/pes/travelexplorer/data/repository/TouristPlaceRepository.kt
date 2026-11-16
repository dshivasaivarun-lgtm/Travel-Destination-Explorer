package com.pes.travelexplorer.data.repository

import android.util.Log
import com.pes.travelexplorer.data.model.OverpassResponse
import com.pes.travelexplorer.data.model.TouristPlace
import com.pes.travelexplorer.data.remote.OverpassApi
import com.pes.travelexplorer.data.remote.OverpassClient
import com.pes.travelexplorer.utils.validCoordinates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException

class TouristPlaceRepository(
    private val api: OverpassApi = OverpassClient.api,
    private val mirrorApi: OverpassApi = OverpassClient.mirrorApi,
    private val maxRetries: Int = 1,
    private val initialDelayMs: Long = 500L
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
            logD(TAG, "Returning cached tourist places for key: $cacheKey")
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

            // Try primary server with retries
            try {
                val response = executeWithRetry {
                    api.getTouristPlaces(requestBody)
                }
                val places = parsePlaces(response)
                cache[cacheKey] = places
                return@withContext Result.success(places)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                logW(TAG, "Primary Overpass server failed (${e.message}), attempting mirror server...")
            }

            // Fallback to secondary mirror server with retries
            try {
                val response = executeWithRetry {
                    mirrorApi.getTouristPlaces(requestBody)
                }
                val places = parsePlaces(response)
                cache[cacheKey] = places
                return@withContext Result.success(places)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                logE(TAG, "Mirror Overpass server also failed: ${e.message}")
                val finalError = e
                val message = formatErrorMessage(finalError)
                Result.failure(Exception(message, finalError))
            }
        }
    }

    private fun logD(tag: String, msg: String) {
        try { Log.d(tag, msg) } catch (_: Throwable) { println("[$tag] D: $msg") }
    }

    private fun logW(tag: String, msg: String) {
        try { Log.w(tag, msg) } catch (_: Throwable) { println("[$tag] W: $msg") }
    }

    private fun logE(tag: String, msg: String) {
        try { Log.e(tag, msg) } catch (_: Throwable) { println("[$tag] E: $msg") }
    }

    private suspend fun <T> executeWithRetry(
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelayMs
        var lastException: Exception? = null
        for (attempt in 0..maxRetries) {
            try {
                return block()
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                lastException = e
                if (attempt == maxRetries || !isTransientError(e)) {
                    throw e
                }
                if (currentDelay > 0) {
                    delay(currentDelay)
                }
                currentDelay *= 2
            }
        }
        throw lastException ?: Exception("Request failed after retries")
    }

    private fun isTransientError(e: Throwable): Boolean {
        return when (e) {
            is HttpException -> e.code() in setOf(429, 502, 503, 504)
            is IOException -> true
            else -> false
        }
    }

    private fun formatErrorMessage(err: Throwable): String {
        return when (err) {
            is HttpException -> when (err.code()) {
                502 -> "Overpass server is temporarily busy (HTTP 502 Bad Gateway). Please tap Retry in a few seconds."
                503 -> "Overpass server is temporarily unavailable (HTTP 503 Service Unavailable). Please tap Retry in a few seconds."
                504 -> "Overpass server response timed out (HTTP 504 Gateway Timeout). Please tap Retry in a few seconds."
                429 -> "Rate limit exceeded (HTTP 429). Please wait a moment."
                400 -> "Invalid query request (HTTP 400 Bad Request)."
                else -> "Server error occurred (HTTP ${err.code()}). Please try again."
            }
            is IOException -> "Network connection problem. Please check your connection and try again."
            else -> err.localizedMessage ?: "Network error occurred. Please try again."
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
