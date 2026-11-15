package com.pes.travelexplorer.data.repository

import com.pes.travelexplorer.data.model.OverpassCenter
import com.pes.travelexplorer.data.model.OverpassElement
import com.pes.travelexplorer.data.model.OverpassResponse
import com.pes.travelexplorer.data.remote.OverpassApi
import kotlinx.coroutines.runBlocking
import okhttp3.RequestBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TouristPlaceRepositoryTest {
    @Test
    fun keepsEveryApprovedTourismAndHistoricCategory() = runBlocking {
        val tourism = listOf("attraction", "museum", "viewpoint", "artwork", "gallery", "zoo", "theme_park", "aquarium")
        val historic = listOf("monument", "memorial", "castle", "archaeological_site", "fort")
        val elements = tourism.mapIndexed { index, value ->
            element("node", index.toLong() + 1, 10.0 + index, 20.0, mapOf("name" to value, "tourism" to value))
        } + historic.mapIndexed { index, value ->
            element("way", index.toLong() + 101, null, null, mapOf("name" to value, "historic" to value), OverpassCenter(30.0 + index, 40.0))
        }

        val places = repositoryFor(OverpassResponse(elements)).getTouristPlaces(12.3001, 56.7001).getOrThrow()

        assertEquals((tourism + historic).toSet(), places.map { it.name }.toSet())
    }

    @Test
    fun excludesOrdinaryAndUnknownMappedFeatures() = runBlocking {
        val response = OverpassResponse(listOf(
            element("node", 1, 1.0, 1.0, mapOf("name" to "Restaurant", "amenity" to "restaurant")),
            element("node", 2, 1.0, 1.0, mapOf("name" to "Cafe", "amenity" to "cafe")),
            element("node", 3, 1.0, 1.0, mapOf("name" to "Hotel", "tourism" to "hotel")),
            element("node", 4, 1.0, 1.0, mapOf("name" to "Hostel", "tourism" to "hostel")),
            element("node", 5, 1.0, 1.0, mapOf("name" to "Shop", "shop" to "mall")),
            element("node", 6, 1.0, 1.0, mapOf("name" to "Office", "office" to "company")),
            element("node", 7, 1.0, 1.0, mapOf("name" to "Residence", "building" to "residential")),
            element("node", 8, 1.0, 1.0, mapOf("name" to "Park", "leisure" to "park")),
            element("node", 9, 1.0, 1.0, mapOf("name" to "Ruins", "historic" to "ruins")),
            element("node", 10, 1.0, 1.0, mapOf("name" to "Museum", "tourism" to "museum"))
        ))

        val places = repositoryFor(response).getTouristPlaces(12.3002, 56.7002).getOrThrow()

        assertEquals(listOf("Museum"), places.map { it.name })
    }

    @Test
    fun retainsNodeWayAndRelationIdentityAndRejectsInvalidCoordinates() = runBlocking {
        val response = OverpassResponse(listOf(
            element("node", 42, 12.0, 77.0, mapOf("name" to "Node Museum", "tourism" to "museum")),
            element("way", 42, null, null, mapOf("name" to "Way Castle", "historic" to "castle"), OverpassCenter(13.0, 78.0)),
            element("relation", 42, null, null, mapOf("name" to "Relation Monument", "historic" to "monument"), OverpassCenter(14.0, 79.0)),
            element("node", 43, null, null, mapOf("name" to "Missing", "tourism" to "museum")),
            element("node", 44, 91.0, 0.0, mapOf("name" to "Invalid", "tourism" to "museum"))
        ))

        val places = repositoryFor(response).getTouristPlaces(12.3003, 56.7003).getOrThrow()

        assertEquals(setOf("node" to 42L, "way" to 42L, "relation" to 42L), places.map { it.osmType to it.id }.toSet())
        assertEquals(setOf(12.0 to 77.0, 13.0 to 78.0, 14.0 to 79.0), places.map { it.latitude to it.longitude }.toSet())
    }

    @Test
    fun queryUsesOnlyWhitelistedTagsAndSelectedRadius() = runBlocking {
        val api = RecordingApi(OverpassResponse(emptyList()))

        TouristPlaceRepository(api, api).getTouristPlaces(12.3004, 56.7004, 10_000).getOrThrow()

        val query = api.queries.single()
        assertTrue(query.contains("nwr(around:10000,12.3004,56.7004)"))
        assertTrue(query.contains("tourism~\"^(attraction|museum|viewpoint|artwork|gallery|zoo|theme_park|aquarium)$\""))
        assertTrue(query.contains("historic~\"^(monument|memorial|castle|archaeological_site|fort)$\""))
        assertTrue(query.contains("out center"))
        assertFalse(query.contains("[tourism];"))
        assertFalse(query.contains("[historic];"))
        assertFalse(query.contains("leisure=park"))
    }

    private fun repositoryFor(response: OverpassResponse): TouristPlaceRepository {
        val api = RecordingApi(response)
        return TouristPlaceRepository(api, api)
    }

    private fun element(
        type: String,
        id: Long,
        latitude: Double?,
        longitude: Double?,
        tags: Map<String, String>,
        center: OverpassCenter? = null
    ) = OverpassElement(type, id, latitude, longitude, center, tags)

    private class RecordingApi(private val response: OverpassResponse) : OverpassApi {
        val queries = mutableListOf<String>()

        override suspend fun getTouristPlaces(query: RequestBody): OverpassResponse {
            queries += Buffer().use { buffer ->
                query.writeTo(buffer)
                buffer.readUtf8()
            }
            return response
        }
    }
}
