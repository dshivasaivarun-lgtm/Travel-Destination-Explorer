package com.pes.travelexplorer.data.local

import com.pes.travelexplorer.data.repository.TripRepository
import com.pes.travelexplorer.ui.trips.TripsViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.Assert.*
import org.junit.Test

class TripSaveTest {
    private open class Dao : TripDao {
        override suspend fun insertTrip(trip: TripEntity): Long = 1
        override fun getAllTrips(): Flow<List<TripEntity>> = flowOf(emptyList())
        override suspend fun getTripById(tripId: Int): TripEntity? = null
        override suspend fun updateTrip(trip: TripEntity) = Unit
        override suspend fun deleteTrip(trip: TripEntity) = Unit
        override suspend fun deleteAllTrips() = Unit
    }
    private val trip = TripEntity(title="Test",destinationCountryCode="IND",destinationCountryName="India",startDate="2026-11-14",endDate="2026-11-14")
    @Test fun saveAwaitsWriteAndIgnoresRepeatedTap() = runBlocking {
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val finished = CompletableDeferred<Unit>()
        var writes = 0
        val dao = object : Dao() {
            override suspend fun insertTrip(trip: TripEntity): Long {
                writes++; entered.complete(Unit); release.await(); return 1
            }
        }
        lateinit var vm: TripsViewModel
        withContext(Dispatchers.Main) {
            vm = TripsViewModel(TripRepository(dao))
            vm.saveTrip(trip) { finished.complete(Unit) }
            vm.saveTrip(trip) { fail("Duplicate save callback") }
        }
        withTimeout(5000) { entered.await() }
        assertEquals(1, writes)
        assertTrue(vm.saving.value)
        assertFalse(finished.isCompleted)
        release.complete(Unit)
        withTimeout(5000) { finished.await() }
        withContext(Dispatchers.Main) { assertFalse(vm.saving.value) }
    }
    @Test fun failedWriteKeepsFormOpenAndAllowsRetry() = runBlocking {
        var failWrite = true
        val dao = object : Dao() {
            override suspend fun insertTrip(trip: TripEntity): Long {
                if (failWrite) error("disk unavailable")
                return 1
            }
        }
        val done = CompletableDeferred<Unit>()
        lateinit var vm: TripsViewModel
        withContext(Dispatchers.Main) {
            vm = TripsViewModel(TripRepository(dao))
            vm.saveTrip(trip) { done.complete(Unit) }
        }
        withTimeout(5000) { vm.error.first { it != null } }
        withContext(Dispatchers.Main) {
            assertFalse(done.isCompleted)
            assertFalse(vm.saving.value)
            failWrite = false
            vm.saveTrip(trip) { done.complete(Unit) }
        }
        withTimeout(5000) { done.await() }
        assertNull(vm.error.value)
    }
}
