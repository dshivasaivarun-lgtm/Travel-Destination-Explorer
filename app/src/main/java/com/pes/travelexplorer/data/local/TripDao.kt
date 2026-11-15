
package com.pes.travelexplorer.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    // Create a new trip
    @Insert
    suspend fun insertTrip(trip: TripEntity): Long

    // Retrieve all trips
    @Query("SELECT * FROM trips ORDER BY startDate ASC")
    fun getAllTrips(): Flow<List<TripEntity>>

    // Retrieve one trip by ID
    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTripById(tripId: Int): TripEntity?

    // Update an existing trip
    @Update
    suspend fun updateTrip(trip: TripEntity)

    // Delete a trip
    @Delete
    suspend fun deleteTrip(trip: TripEntity)

    // Delete all trips
    @Query("DELETE FROM trips")
    suspend fun deleteAllTrips()
}
