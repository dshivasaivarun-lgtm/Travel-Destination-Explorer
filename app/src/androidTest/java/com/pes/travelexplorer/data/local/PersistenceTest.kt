package com.pes.travelexplorer.data.local
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test

class PersistenceTest {
    @Test fun wishlistAndTripCrudSurviveDatabaseReopen() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "persistence-regression-${System.nanoTime()}.db"
        fun open() = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
        var db = open()
        try {
            val saved = WishlistEntity("IND", "India", "Republic of India", null, "Asia")
            db.wishlistDao().addToWishlist(saved)
            db.wishlistDao().addToWishlist(saved)
            assertEquals(1, db.wishlistDao().getAllWishlist().first().size)
            val trip = TripEntity(title="India holiday",destinationCountryCode="IND",destinationCountryName="India",startDate="2026-12-01",endDate="2026-12-03")
            val id = db.tripDao().insertTrip(trip).toInt()
            db.close(); db = open()
            assertTrue(db.wishlistDao().isInWishlist("IND"))
            assertEquals("India holiday", db.tripDao().getTripById(id)?.title)
            db.tripDao().updateTrip(trip.copy(id=id,title="Edited",notes="Visit museum"))
            db.wishlistDao().removeFromWishlist("IND")
            db.close(); db = open()
            assertFalse(db.wishlistDao().isInWishlist("IND"))
            val updated = db.tripDao().getTripById(id)!!
            assertEquals("Edited", updated.title)
            assertEquals("Visit museum", updated.notes)
            db.tripDao().deleteTrip(updated)
            db.close(); db = open()
            assertTrue(db.tripDao().getAllTrips().first().isEmpty())
        } finally { db.close(); context.deleteDatabase(name) }
    }
    @Test fun regionPreferenceSurvivesNewManagerAndCanBeReset() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val original = PreferencesManager(context).getRegion()
        try {
            PreferencesManager(context).saveRegion("Oceania")
            assertEquals("Oceania", PreferencesManager(context).getRegion())
            PreferencesManager(context).saveRegion("All")
            assertEquals("All", PreferencesManager(context).getRegion())
        } finally { PreferencesManager(context).saveRegion(original) }
    }
}
