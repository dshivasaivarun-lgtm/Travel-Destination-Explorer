package com.pes.travelexplorer.data.repository

import android.util.Log
import com.pes.travelexplorer.data.photos.DestinationPhotoInfo
import com.pes.travelexplorer.data.photos.DestinationPhotos
import com.pes.travelexplorer.data.remote.WikimediaApi
import com.pes.travelexplorer.data.remote.WikimediaClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class DestinationPhotoRepository(
    private val api: WikimediaApi = WikimediaClient.api
) {
    companion object {
        private const val TAG = "DestinationPhotoRepo"
        private val cache = ConcurrentHashMap<String, DestinationPhotoInfo>()

        // Keywords to filter out non-destination images (flags, maps, coats of arms, logos, icons, diagrams)
        private val EXCLUDED_KEYWORDS = setOf(
            "flag", "map", "coat_of_arms", "coat of arms", "logo", "location", "symbol",
            "diagram", "chart", "icon", "stamp", "stamps", "shield", "emblem", "locator",
            "subdivision", "province", "orthographic", "globe", "wiki", "portal", "blank"
        )
    }

    suspend fun getPhotoForCountry(code: String?, countryName: String?): Result<DestinationPhotoInfo?> {
        val normalizedCode = code?.uppercase(Locale.ROOT)?.trim()
        val normalizedName = countryName?.trim()

        val cacheKey = normalizedCode ?: normalizedName ?: return Result.success(null)

        // 1. Check in-memory cache
        cache[cacheKey]?.let {
            logD(TAG, "Returning cached photo for key: $cacheKey")
            return Result.success(it)
        }

        // 2. Check curated photos catalogue (Requirement 10: keep existing curated photos for supported countries)
        val curated = DestinationPhotos.find(normalizedCode)
        if (curated != null) {
            cache[cacheKey] = curated
            return Result.success(curated)
        }

        // If no country name, cannot perform dynamic search
        if (normalizedName.isNullOrBlank()) {
            return Result.success(null)
        }

        return withContext(Dispatchers.IO) {
            try {
                // Try searching "Tourism in {countryName}"
                var photo = fetchFromWikimedia("Tourism in $normalizedName", normalizedCode)

                // Secondary search attempt if "Tourism in {countryName}" yields no photo
                if (photo == null) {
                    photo = fetchFromWikimedia("Landmarks in $normalizedName", normalizedCode)
                }
                if (photo == null) {
                    photo = fetchFromWikimedia(normalizedName, normalizedCode)
                }

                if (photo != null) {
                    cache[cacheKey] = photo
                    Result.success(photo)
                } else {
                    Result.success(null)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logE(TAG, "Error fetching photo for $normalizedName: ${e.message}")
                Result.failure(e)
            }
        }
    }

    private suspend fun fetchFromWikimedia(searchQuery: String, countryCode: String?): DestinationPhotoInfo? {
        val response = api.searchPhotos(gsrsearch = searchQuery)
        val pages = response.query?.pages?.values ?: return null

        for (page in pages) {
            val title = page.title ?: continue
            val lowerTitle = title.lowercase(Locale.ROOT)

            // Requirement 4: Filter out flags, maps, coats of arms, svgs
            if (EXCLUDED_KEYWORDS.any { lowerTitle.contains(it) }) continue

            val imageInfo = page.imageInfo?.firstOrNull() ?: continue
            val imageUrl = imageInfo.url ?: continue

            if (imageUrl.lowercase(Locale.ROOT).endsWith(".svg") || imageUrl.isBlank()) continue

            val extMeta = imageInfo.extMetadata ?: emptyMap()

            // Extract caption
            val rawCaption = extMeta["ObjectName"]?.value
                ?: title.removePrefix("File:").removeSuffix(".jpg").removeSuffix(".jpeg").removeSuffix(".png").replace('_', ' ')
            val caption = cleanHtml(rawCaption)

            // Extract author
            val rawArtist = extMeta["Artist"]?.value ?: imageInfo.user ?: "Wikimedia Commons"
            val author = cleanHtml(rawArtist).ifBlank { "Wikimedia Commons" }

            // Extract license
            val license = extMeta["LicenseShortName"]?.value ?: "CC BY-SA 4.0"
            val licenseUrl = extMeta["LicenseUrl"]?.value ?: ""

            // Extract source URL
            val sourceUrl = imageInfo.descriptionUrl ?: "https://commons.wikimedia.org/wiki/$title"

            return DestinationPhotoInfo(
                code = countryCode ?: "CUSTOM",
                caption = caption,
                imageUrl = imageUrl,
                sourceUrl = sourceUrl,
                author = author,
                license = license,
                licenseUrl = licenseUrl
            )
        }
        return null
    }

    private fun cleanHtml(text: String): String {
        return text.replace(Regex("<[^>]*>"), "").trim()
    }

    fun clearCache() {
        cache.clear()
    }

    private fun logD(tag: String, msg: String) {
        try { Log.d(tag, msg) } catch (_: Throwable) { println("[$tag] D: $msg") }
    }

    private fun logE(tag: String, msg: String) {
        try { Log.e(tag, msg) } catch (_: Throwable) { println("[$tag] E: $msg") }
    }
}
