
package com.pes.travelexplorer.ui.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pes.travelexplorer.data.local.TripEntity
import com.pes.travelexplorer.data.repository.TripRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TripsViewModel(
    private val repository: TripRepository
) : ViewModel() {
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    fun clearError() { _error.value = null }
    private suspend fun safely(block: suspend () -> Unit) {
        try { block() }
        catch (e: CancellationException) { throw e }
        catch (_: Exception) { _error.value = "storage" }
    }


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
            safely { repository.insertTrip(trip) }
        }
    }

    // Update a trip
    fun updateTrip(trip: TripEntity) {
        viewModelScope.launch {
            safely { repository.updateTrip(trip) }
        }
    }

    // Delete a trip
    fun deleteTrip(trip: TripEntity) {
        viewModelScope.launch {
            safely { repository.deleteTrip(trip) }
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
            safely { repository.deleteAllTrips() }
        }
    }

    private val _saving = MutableStateFlow(false)
    val saving = _saving.asStateFlow()
    suspend fun readTrip(id: Int): TripEntity? = repository.getTripById(id)
    fun saveTrip(trip: TripEntity, onSaved: () -> Unit) {
        if (_saving.value) return
        _saving.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                if (trip.id == 0) repository.insertTrip(trip) else repository.updateTrip(trip)
                onSaved()
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { _error.value = "storage" }
            finally { _saving.value = false }
        }
    }
}
