package com.pes.travelexplorer.ui.tourist

import com.pes.travelexplorer.data.model.TouristPlace
import org.junit.Assert.assertEquals
import org.junit.Test

class NearbyPlaceFiltersTest {
    @Test
    fun queryMatchesNameOrCategoryIgnoringCase() {
        val museum = place("City Museum", "Museum")
        val park = place("Riverside", "Park")

        assertEquals(listOf(museum), filterNearbyPlaces(listOf(museum, park), "MUSEUM"))
        assertEquals(listOf(park), filterNearbyPlaces(listOf(museum, park), "park"))
    }

    @Test
    fun blankQueryPreservesAllPlaces() {
        val places = listOf(place("Museum", "Museum"), place("Park", "Park"))

        assertEquals(places, filterNearbyPlaces(places, "   "))
    }

    private fun place(name: String, category: String) = TouristPlace(
        osmType = "node", id = name.hashCode().toLong(), name = name, category = category,
        latitude = 1.0, longitude = 1.0, address = null, website = null, openingHours = null
    )
}
