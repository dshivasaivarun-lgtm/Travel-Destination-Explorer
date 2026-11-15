package com.pes.travelexplorer.ui.tourist

import com.pes.travelexplorer.data.model.TouristPlace

fun filterNearbyPlaces(places: List<TouristPlace>, query: String): List<TouristPlace> {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isEmpty()) return places
    return places.filter { place ->
        place.name.contains(normalizedQuery, ignoreCase = true) ||
            place.category.contains(normalizedQuery, ignoreCase = true)
    }
}
