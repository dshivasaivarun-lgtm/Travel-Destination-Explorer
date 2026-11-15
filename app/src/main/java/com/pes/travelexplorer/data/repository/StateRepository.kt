package com.pes.travelexplorer.data.repository

import android.util.Log
import com.pes.travelexplorer.data.model.AdministrativeDivision
import com.pes.travelexplorer.data.remote.PlaceDbApi
import com.pes.travelexplorer.data.remote.PlaceDbClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StateRepository(
    private val api: PlaceDbApi = PlaceDbClient.api
) {
    companion object {
        private const val TAG = "StateRepository"
    }

    suspend fun getStatesForCountry(countryCode: String): Result<List<AdministrativeDivision>> {
        return withContext(Dispatchers.IO) {
            try {
                require(countryCode.matches(Regex("[A-Za-z]{2}"))) { "A valid Alpha-2 country code is required" }
                val alpha2Code = countryCode.uppercase(java.util.Locale.ROOT)

                val response = api.getRegions(alpha2Code)
                val divisions = response.data ?: response.regions ?: response.items ?: emptyList()
                Log.d(TAG, "Fetched ${divisions.size} divisions for $alpha2Code. First division: ${divisions.firstOrNull()}")
                Result.success(divisions)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch regions for $countryCode: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
}
