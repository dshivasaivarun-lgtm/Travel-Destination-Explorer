package com.pes.travelexplorer.data.repository

import android.util.Log
import com.pes.travelexplorer.BuildConfig
import com.pes.travelexplorer.data.model.Country
import com.pes.travelexplorer.data.remote.CountryApi
import com.pes.travelexplorer.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CountryRepository(
    private val api: CountryApi = RetrofitClient.api
) {
    companion object {
        private const val TAG = "CountryRepository"
    }

    suspend fun getAllCountries(): Result<List<Country>> {
        return withContext(Dispatchers.IO) {
            try {
                if (BuildConfig.API_KEY.isBlank()) {
                    return@withContext Result.failure(
                        IllegalStateException("API key is missing. Please configure 'API_KEY' in local.properties.")
                    )
                }
                val response = api.getAllCountries()
                val countries = response.data?.objects ?: emptyList()
                Result.success(countries)
            } catch (e: Exception) {
                Log.e(TAG, "getAllCountries failed: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun searchCountryByName(name: String): Result<List<Country>> {
        return withContext(Dispatchers.IO) {
            try {
                if (BuildConfig.API_KEY.isBlank()) {
                    return@withContext Result.failure(
                        IllegalStateException("API key is missing. Please configure 'API_KEY' in local.properties.")
                    )
                }
                val response = api.getCountryByName(name)
                val countries = response.data?.objects ?: emptyList()
                Result.success(countries)
            } catch (e: Exception) {
                Log.e(TAG, "searchCountryByName failed for $name: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getCountryByCode(alpha3: String): Result<Country?> {
        return withContext(Dispatchers.IO) {
            try {
                if (BuildConfig.API_KEY.isBlank()) {
                    return@withContext Result.failure(
                        IllegalStateException("API key is missing. Please configure 'API_KEY' in local.properties.")
                    )
                }
                val response = api.getCountryByCode(alpha3)
                val country = response.data?.objects?.firstOrNull()
                if (country != null) {
                    Result.success(country)
                } else {
                    val allResponse = api.getAllCountries()
                    val found = allResponse.data?.objects?.find {
                        it.codes?.alpha3.equals(alpha3, ignoreCase = true)
                    }
                    Result.success(found)
                }
            } catch (e: Exception) {
                Log.e(TAG, "getCountryByCode failed for $alpha3: ${e.message}", e)
                try {
                    val allResponse = api.getAllCountries()
                    val found = allResponse.data?.objects?.find {
                        it.codes?.alpha3.equals(alpha3, ignoreCase = true)
                    }
                    Result.success(found)
                } catch (ex: Exception) {
                    Log.e(TAG, "Fallback getAllCountries also failed: ${ex.message}", ex)
                    Result.failure(e)
                }
            }
        }
    }
}
