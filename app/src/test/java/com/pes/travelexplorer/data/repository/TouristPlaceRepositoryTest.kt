package com.pes.travelexplorer.data.repository

import com.pes.travelexplorer.data.model.OverpassElement
import com.pes.travelexplorer.data.model.OverpassCenter
import com.pes.travelexplorer.data.model.OverpassResponse
import com.pes.travelexplorer.data.remote.OverpassApi
import kotlinx.coroutines.runBlocking
import okhttp3.RequestBody
import org.junit.Assert.assertEquals
import org.junit.Test

class TouristPlaceRepositoryTest {
    @Test
    fun keepsNodeAndWayWithTheSameNumericId() = runBlocking {
        val response = OverpassResponse(
            listOf(
                OverpassElement("node", 42, 12.0, 77.0, null, mapOf("name" to "Museum", "tourism" to "museum")),
                OverpassElement("way", 42, null, null, OverpassCenter(13.0, 78.0), mapOf("name" to "Park", "leisure" to "park"))
            )
        )
        val api = object : OverpassApi {
            override suspend fun getTouristPlaces(query: RequestBody) = response
        }

        val places = TouristPlaceRepository(api, api).getTouristPlaces(12.345, 56.789).getOrThrow()

        assertEquals(setOf("node" to 42L, "way" to 42L), places.map { it.osmType to it.id }.toSet())
    }
}
