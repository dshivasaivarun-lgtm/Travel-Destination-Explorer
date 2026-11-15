package com.pes.travelexplorer.data.repository
import com.pes.travelexplorer.data.model.*
import com.pes.travelexplorer.data.remote.CountryApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CancellationException
import org.junit.Assert.*
import org.junit.Test

class CountryRepositoryTest {
    private fun country(code: String) = Country(CountryNames(code, null), CountryCodes(null, code), null, null, "Asia", null, null, null, null)
    private class Api(val fetch: (String, Int) -> CountryResponse) : CountryApi {
        override suspend fun getAllCountries() = error("unused")
        override suspend fun getCountryByName(name: String) = error("unused")
        override suspend fun getCountryByCode(code: String) = fetch("details", 0)
        override suspend fun getCountriesByRegion(region: String, limit: Int, offset: Int) = fetch(region, offset)
    }
    @Test fun paginationIncludesLaterRecordsAndDeduplicates() = runBlocking {
        val api = Api { region, offset ->
            if (region != "Asia") CountryResponse(CountryData(emptyList(), CountryPageMeta()))
            else if (offset == 0) CountryResponse(CountryData(listOf(country("IND")), CountryPageMeta(2,1,1,0,true)))
            else CountryResponse(CountryData(listOf(country("JPN"),country("IND")), CountryPageMeta(2,2,2,1,false)))
        }
        assertEquals(setOf("IND","JPN"), CountryRepository(api) { true }.getAllCountries().getOrThrow().map { it.codes?.alpha3 }.toSet())
    }
    @Test fun laterPageFailureIsNotPartialSuccess() = runBlocking {
        val api = Api { _, offset ->
            if (offset > 0) error("offline")
            CountryResponse(CountryData(listOf(country("IND")), CountryPageMeta(2,1,1,0,true)))
        }
        assertTrue(CountryRepository(api) { true }.getAllCountries().isFailure)
    }
    @Test fun malformedPagingFailsRatherThanLooping() = runBlocking {
        val api = Api { _, _ -> CountryResponse(CountryData(emptyList(), CountryPageMeta(more=true))) }
        assertTrue(CountryRepository(api) { true }.getAllCountries().isFailure)
    }
    @Test fun detailNetworkFailureIsNotCountryNotFound() = runBlocking {
        assertTrue(CountryRepository(Api { _, _ -> error("offline") }) { true }.getCountryByCode("IND").isFailure)
        assertNull(CountryRepository(Api { _, _ -> CountryResponse(CountryData(emptyList())) }) { true }.getCountryByCode("IND").getOrThrow())
    }
    @Test fun cancellationIsPreserved() = runBlocking {
        try {
            CountryRepository(Api { _, _ -> throw CancellationException("cancelled") }) { true }.getAllCountries()
            fail("Cancellation must propagate")
        } catch (_: CancellationException) { }
    }
}
