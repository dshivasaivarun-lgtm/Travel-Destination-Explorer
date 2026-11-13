
package com.pes.travelexplorer.data.repository

import com.pes.travelexplorer.data.local.TripDao
import com.pes.travelexplorer.data.local.TripEntity
import kotlinx.coroutines.flow.Flow

class TripRepository(
    private val tripDao: TripDao
) {

    // Create a new trip
    suspend fun insertTrip(trip: TripEntity): Long {
        return tripDao.insertTrip(trip)
    }

    // Observe all trips
    fun getAllTrips(): Flow<List<TripEntity>> {
        return tripDao.getAllTrips()
    }

    // Get a particular trip
    suspend fun getTripById(tripId: Int): TripEntity? {
        return tripDao.getTripById(tripId)
    }

    // Update an existing trip
    suspend fun updateTrip(trip: TripEntity) {
        tripDao.updateTrip(trip)
    }

    // Delete a trip
    suspend fun deleteTrip(trip: TripEntity) {
        tripDao.deleteTrip(trip)
    }

    // Delete all trips
    suspend fun deleteAllTrips() {
        tripDao.deleteAllTrips()
    }
}
