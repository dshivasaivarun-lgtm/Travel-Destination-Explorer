package com.pes.travelexplorer.data.repository

import com.pes.travelexplorer.BuildConfig
import com.pes.travelexplorer.data.model.Country
import com.pes.travelexplorer.data.remote.CountryApi
import com.pes.travelexplorer.data.remote.RetrofitClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class CountryRepository(private val api: CountryApi = RetrofitClient.api,
    private val hasApiKey: () -> Boolean = { BuildConfig.API_KEY.isNotBlank() }) {
    private suspend fun <T> request(block: suspend () -> T): Result<T> = withContext(Dispatchers.IO) {
        try {
            check(hasApiKey()) { "Country service is not configured." }
            Result.success(block())
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) { Result.failure(e) }
    }
    suspend fun getAllCountries(): Result<List<Country>> = request {
        val regions = listOf("Africa", "Americas", "Asia", "Europe", "Oceania")
        // A failed page fails the load, never an invisible partial list.
        kotlinx.coroutines.coroutineScope {
            regions.map { region -> async { loadRegion(region) } }.awaitAll().flatten()
        }.filter { it.codes?.alpha3?.matches(Regex("[A-Za-z]{3}")) == true }
            .distinctBy { it.codes?.alpha3?.uppercase(java.util.Locale.ROOT) }
    }
    private suspend fun loadRegion(region: String): List<Country> {
        val countries = mutableListOf<Country>()
        var offset = 0
        do {
            val data = api.getCountriesByRegion(region, limit = 100, offset = offset).data
                ?: error("Missing country response data")
            val page = data.objects ?: error("Missing country response objects")
            countries.addAll(page)
            val meta = data.meta ?: error("Missing country pagination metadata")
            if (!meta.more) break
            check(page.isNotEmpty() && meta.offset == offset && meta.count == page.size) {
                "Country pagination did not advance"
            }
            offset += page.size
            check(offset <= 1000) { "Unexpected country pagination size" }
        } while (true)
        return countries
    }
    suspend fun searchCountryByName(name: String): Result<List<Country>> = request {
        api.getCountryByName(name).data?.objects ?: error("Missing country response")
    }
    suspend fun getCountryByCode(alpha3: String): Result<Country?> = request {
        require(alpha3.matches(Regex("[A-Za-z]{3}"))) { "Invalid country identifier" }
        val data = api.getCountryByCode(alpha3).data ?: error("Missing country response")
        val objects = data.objects ?: error("Missing country response objects")
        objects.firstOrNull { it.codes?.alpha3.equals(alpha3, ignoreCase = true) }
    }
}
