package com.pes.travelexplorer.ui.components

import com.pes.travelexplorer.data.photos.DestinationPhotos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DestinationPhotoStateTest {
    @Test
    fun licensedCatalogueHasTwelveUniqueCountryCodesAndNoFallbackForUnknownCountries() {
        assertEquals(12, DestinationPhotos.all.size)
        assertEquals(12, DestinationPhotos.all.map { it.code }.toSet().size)
        assertNull(DestinationPhotos.find("XXX"))
    }
}
