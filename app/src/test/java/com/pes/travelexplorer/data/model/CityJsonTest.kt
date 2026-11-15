package com.pes.travelexplorer.data.model

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CityJsonTest {
    private val gson = Gson()

    @Test
    fun placeDbLatLonPopulateCityCoordinates() {
        val city = gson.fromJson(
            """{"id":"22076254","name":"Afra Bay","lat":11.675982,"lon":92.734689}""",
            City::class.java
        )

        assertEquals(11.675982, city.latitude!!, 0.0)
        assertEquals(92.734689, city.longitude!!, 0.0)
    }

    @Test
    fun legacyLatitudeLongitudeStillPopulateCityCoordinates() {
        val city = gson.fromJson(
            """{"id":"legacy","name":"Legacy City","latitude":12.0,"longitude":77.0}""",
            City::class.java
        )

        assertEquals(12.0, city.latitude!!, 0.0)
        assertEquals(77.0, city.longitude!!, 0.0)
    }

    @Test
    fun missingCoordinatesRemainNull() {
        val city = gson.fromJson("""{"id":"missing","name":"No Location"}""", City::class.java)

        assertNull(city.latitude)
        assertNull(city.longitude)
    }
}
