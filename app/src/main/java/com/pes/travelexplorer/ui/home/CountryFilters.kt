package com.pes.travelexplorer.ui.home

import com.pes.travelexplorer.data.model.Country
import java.util.Locale

enum class CountryRegion {
    All, Africa, Americas, Asia, Europe, Oceania
}

fun filterCountries(
    countries: List<Country>,
    query: String,
    region: CountryRegion
): List<Country> {
    val nameQuery = query.trim()
    return countries.filter { country ->
        (nameQuery.isEmpty() || country.names?.common?.contains(nameQuery, ignoreCase = true) == true) &&
            (region == CountryRegion.All || country.region.equals(region.name, ignoreCase = true))
    }
}

// The API model calls the team's canonical cca3 identifier codes.alpha3.
fun Country.detailsCode(): String? = codes?.alpha3?.trim()
    ?.takeIf { it.matches(Regex("[A-Za-z]{3}")) }
    ?.uppercase(Locale.ROOT)
