package com.pes.travelexplorer.ui.trips
import org.junit.Assert.*
import org.junit.Test
import com.pes.travelexplorer.utils.validCoordinates
class TripValidationTest {
    @Test fun impossibleDatesAreRejected() {
        listOf("2026-02-30", "2026-13-01", "2026-00-10", "2026-04-31", "2026-2-01", "0000-01-01").forEach {
            assertFalse(it, validTripDates(it, "2027-01-01"))
        }
    }
    @Test fun leapYearsSameDayAndDateOrder() {
        assertTrue(validTripDates("2028-02-29", "2028-02-29"))
        assertFalse(validTripDates("2027-02-29", "2027-03-01"))
        assertFalse(validTripDates("2026-12-01", "2026-11-01"))
        assertTrue(validTripDates("2026-12-31", "2027-01-01"))
    }
    @Test fun geographicBoundsIncludeZeroButRejectNonFiniteValues() {
        assertTrue(validCoordinates(0.0, 0.0))
        assertTrue(validCoordinates(-90.0, 180.0))
        assertFalse(validCoordinates(91.0, 0.0))
        assertFalse(validCoordinates(0.0, -181.0))
        assertFalse(validCoordinates(Double.NaN, 1.0))
        assertFalse(validCoordinates(1.0, Double.POSITIVE_INFINITY))
    }
}
