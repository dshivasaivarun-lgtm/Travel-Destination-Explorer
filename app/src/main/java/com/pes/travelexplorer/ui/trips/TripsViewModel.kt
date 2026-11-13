
package com.pes.travelexplorer.ui.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pes.travelexplorer.data.local.TripEntity
import com.pes.travelexplorer.data.repository.TripRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TripsViewModel(
    private val repository: TripRepository
) : ViewModel() {

    // Observe all trips
    val trips: StateFlow<List<TripEntity>> =
        repository.getAllTrips().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Create a trip
    fun addTrip(trip: TripEntity) {
        viewModelScope.launch {
            repository.insertTrip(trip)
        }
    }

    // Update a trip
    fun updateTrip(trip: TripEntity) {
        viewModelScope.launch {
            repository.updateTrip(trip)
        }
    }

    // Delete a trip
    fun deleteTrip(trip: TripEntity) {
        viewModelScope.launch {
            repository.deleteTrip(trip)
        }
    }

    // Retrieve one trip
    fun getTripById(
        tripId: Int,
        onResult: (TripEntity?) -> Unit
    ) {
        viewModelScope.launch {
            onResult(repository.getTripById(tripId))
        }
    }

    // Delete all trips
    fun deleteAllTrips() {
        viewModelScope.launch {
            repository.deleteAllTrips()
        }
    }
}
