package com.pes.travelexplorer.data.repository

import android.util.Log
import com.pes.travelexplorer.data.model.City
import com.pes.travelexplorer.data.remote.PlaceDbApi
import com.pes.travelexplorer.data.remote.PlaceDbClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CityRepository(
    private val api: PlaceDbApi = PlaceDbClient.api
) {
    companion object {
        private const val TAG = "CityRepository"
    }

    suspend fun getCitiesForDivision(countryCode: String, admin1Id: String): Result<List<City>> {
        return withContext(Dispatchers.IO) {
            try {
                val alpha2Code = if (countryCode.length == 3) {
                    countryCode.take(2).uppercase()
                } else {
                    countryCode.uppercase()
                }
                Log.d(TAG, "Fetching cities for country: $alpha2Code, admin1Id: $admin1Id")
                val response = api.getCities(alpha2Code, admin1Id)
                val cities = response.data ?: response.cities ?: response.items ?: response.objects ?: response.settlements ?: emptyList()
                Log.d(TAG, "Fetched ${cities.size} cities for $alpha2Code / $admin1Id")
                Result.success(cities)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch cities for $countryCode / $admin1Id: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
}
