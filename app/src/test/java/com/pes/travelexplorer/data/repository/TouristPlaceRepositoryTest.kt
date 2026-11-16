package com.pes.travelexplorer.data.repository

import com.pes.travelexplorer.data.model.OverpassCenter
import com.pes.travelexplorer.data.model.OverpassElement
import com.pes.travelexplorer.data.model.OverpassResponse
import com.pes.travelexplorer.data.remote.OverpassApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

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

        TouristPlaceRepository(api, api, maxRetries = 0, initialDelayMs = 0L).getTouristPlaces(12.3004, 56.7004, 10_000).getOrThrow()

        val query = api.queries.single()
        assertTrue(query.contains("nwr(around:10000,12.3004,56.7004)"))
        assertTrue(query.contains("tourism~\"^(attraction|museum|viewpoint|artwork|gallery|zoo|theme_park|aquarium)$\""))
        assertTrue(query.contains("historic~\"^(monument|memorial|castle|archaeological_site|fort)$\""))
        assertTrue(query.contains("out center"))
        assertFalse(query.contains("[tourism];"))
        assertFalse(query.contains("[historic];"))
        assertFalse(query.contains("leisure=park"))
    }

    @Test
    fun primaryEndpointSuccessDoesNotCallMirror() = runBlocking {
        val primaryResponse = OverpassResponse(listOf(element("node", 1, 10.0, 20.0, mapOf("name" to "Primary Art", "tourism" to "artwork"))))
        val primaryApi = MockApi(responses = listOf(Result.success(primaryResponse)))
        val mirrorApi = MockApi(responses = emptyList())
        val repo = TouristPlaceRepository(primaryApi, mirrorApi, maxRetries = 1, initialDelayMs = 0L)

        val result = repo.getTouristPlaces(10.0, 20.0, 3000)

        assertTrue(result.isSuccess)
        assertEquals("Primary Art", result.getOrThrow().single().name)
        assertEquals(1, primaryApi.callCount)
        assertEquals(0, mirrorApi.callCount)
    }

    @Test
    fun primaryFailureTriggersSuccessfulMirrorResponse() = runBlocking {
        val primaryApi = MockApi(responses = listOf(Result.failure(createHttpException(502)), Result.failure(createHttpException(502))))
        val mirrorResponse = OverpassResponse(listOf(element("node", 2, 10.0, 20.0, mapOf("name" to "Mirror Castle", "historic" to "castle"))))
        val mirrorApi = MockApi(responses = listOf(Result.success(mirrorResponse)))
        val repo = TouristPlaceRepository(primaryApi, mirrorApi, maxRetries = 1, initialDelayMs = 0L)

        val result = repo.getTouristPlaces(10.1, 20.1, 5000)

        assertTrue(result.isSuccess)
        assertEquals("Mirror Castle", result.getOrThrow().single().name)
        assertEquals(2, primaryApi.callCount)
        assertEquals(1, mirrorApi.callCount)
    }

    @Test
    fun bothEndpointsFailingReportsAccurateError() = runBlocking {
        val primaryApi = MockApi(responses = listOf(Result.failure(createHttpException(502)), Result.failure(createHttpException(502))))
        val mirrorApi = MockApi(responses = listOf(Result.failure(createHttpException(503)), Result.failure(createHttpException(503))))
        val repo = TouristPlaceRepository(primaryApi, mirrorApi, maxRetries = 1, initialDelayMs = 0L)

        val result = repo.getTouristPlaces(10.2, 20.2, 5000)

        assertTrue(result.isFailure)
        val message = result.exceptionOrNull()?.message.orEmpty()
        assertTrue(message.contains("503"))
        assertEquals(2, primaryApi.callCount)
        assertEquals(2, mirrorApi.callCount)
    }

    @Test
    fun httpStatusCodes429_502_503_504Handling() = runBlocking {
        val statusCodes = listOf(429, 502, 503, 504)
        for (code in statusCodes) {
            val primaryApi = MockApi(responses = listOf(Result.failure(createHttpException(code))))
            val mirrorApi = MockApi(responses = listOf(Result.failure(createHttpException(code))))
            val repo = TouristPlaceRepository(primaryApi, mirrorApi, maxRetries = 0, initialDelayMs = 0L)

            val result = repo.getTouristPlaces(11.0 + code, 21.0, 5000)

            assertTrue(result.isFailure)
            val msg = result.exceptionOrNull()?.message.orEmpty()
            assertTrue("Expected status $code in error message: $msg", msg.contains(code.toString()))
        }
    }

    @Test
    fun permanentClientErrorDoesNotRetryOnSameEndpoint() = runBlocking {
        val primaryApi = MockApi(responses = listOf(Result.failure(createHttpException(400))))
        val mirrorApi = MockApi(responses = listOf(Result.failure(createHttpException(400))))
        val repo = TouristPlaceRepository(primaryApi, mirrorApi, maxRetries = 2, initialDelayMs = 0L)

        val result = repo.getTouristPlaces(10.3, 20.3, 5000)

        assertTrue(result.isFailure)
        assertEquals(1, primaryApi.callCount)
        assertEquals(1, mirrorApi.callCount)
    }

    @Test
    fun respectsCoroutineCancellation() = runBlocking {
        val cancellingApi = object : OverpassApi {
            override suspend fun getTouristPlaces(query: RequestBody): OverpassResponse {
                throw CancellationException("Cancelled")
            }
        }
        val repo = TouristPlaceRepository(cancellingApi, cancellingApi, maxRetries = 2, initialDelayMs = 0L)

        val job = async {
            repo.getTouristPlaces(10.4, 20.4, 5000)
        }

        try {
            job.await()
            fail("Expected CancellationException")
        } catch (e: CancellationException) {
            assertEquals("Cancelled", e.message)
        }
    }

    private fun repositoryFor(response: OverpassResponse): TouristPlaceRepository {
        val api = RecordingApi(response)
        return TouristPlaceRepository(api, api, maxRetries = 0, initialDelayMs = 0L)
    }

    private fun element(
        type: String,
        id: Long,
        latitude: Double?,
        longitude: Double?,
        tags: Map<String, String>,
        center: OverpassCenter? = null
    ) = OverpassElement(type, id, latitude, longitude, center, tags)

    private fun createHttpException(code: Int): HttpException {
        val response = Response.error<OverpassResponse>(
            code,
            ResponseBody.create("application/json".toMediaTypeOrNull(), "{\"error\": \"$code\"}")
        )
        return HttpException(response)
    }

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

    private class MockApi(private val responses: List<Result<OverpassResponse>>) : OverpassApi {
        var callCount = 0

        override suspend fun getTouristPlaces(query: RequestBody): OverpassResponse {
            val idx = (callCount++).coerceAtMost(responses.lastIndex)
            val res = responses[idx]
            return res.getOrThrow()
        }
    }
}
