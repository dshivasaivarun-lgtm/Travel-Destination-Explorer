package com.pes.travelexplorer.ui.home

import com.pes.travelexplorer.data.model.Country
import com.pes.travelexplorer.data.model.CountryCodes
import com.pes.travelexplorer.data.model.CountryNames
import org.junit.Assert.*
import org.junit.Test

class CountryFiltersTest {
    private val india = country("India", "Asia", "IND")
    private val japan = country("Japan", "Asia", "JPN")
    private val france = country("France", "Europe", "FRA")
    private val missing = country(null, null, null)
    private val countries = listOf(india, japan, france, missing)

    @Test fun nameSearchIsCaseInsensitiveAndTrimsWhitespace() {
        assertEquals(listOf(india), filterCountries(countries, "  iNd  ", CountryRegion.All))
    }

    @Test fun searchDoesNotMatchRegionInsteadOfCountryName() {
        assertTrue(filterCountries(countries, "Asia", CountryRegion.All).isEmpty())
    }

    @Test fun nameAndRegionMustBothMatch() {
        assertEquals(listOf(india), filterCountries(countries, "ind", CountryRegion.Asia))
        assertTrue(filterCountries(countries, "ind", CountryRegion.Europe).isEmpty())
    }

    @Test fun allAndBlankQueryIncludeMissingOptionalFields() {
        assertEquals(countries, filterCountries(countries, "  ", CountryRegion.All))
        assertEquals(listOf(france), filterCountries(countries, "", CountryRegion.Europe))
    }

    @Test fun everyRegionUsesExactCaseInsensitiveMatch() {
        CountryRegion.entries.filterNot { it == CountryRegion.All }.forEach { region ->
            val match = country("Example", region.name.lowercase(), "ABC")
            assertEquals(listOf(match), filterCountries(listOf(match), "", region))
        }
        assertTrue(filterCountries(listOf(country("Example", "South Asia", "ABC")), "", CountryRegion.Asia).isEmpty())
    }

    @Test fun detailsCodeUsesAlpha3RatherThanNameOrAlpha2() {
        assertEquals("IND", country("India", "Asia", " ind ").detailsCode())
        listOf(null, "", "IN", "IND/", "123").forEach { code ->
            assertNull(country("India", "Asia", code).detailsCode())
        }
    }

    private fun country(name: String?, region: String?, code: String?) = Country(
        names = CountryNames(name, null), codes = CountryCodes("XX", code),
        capitals = null, flag = null, region = region, subregion = null,
        population = null, currencies = null, languages = null
    )
}
