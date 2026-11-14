package com.pes.travelexplorer.ui.tourist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pes.travelexplorer.data.model.TouristPlace
import com.pes.travelexplorer.data.repository.TouristPlaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TouristPlacesUiState {
    object Loading : TouristPlacesUiState
    data class Success(val places: List<TouristPlace>) : TouristPlacesUiState
    object Empty : TouristPlacesUiState
    data class Error(val message: String) : TouristPlacesUiState
}

class TouristPlacesViewModel(
    private val repository: TouristPlaceRepository = TouristPlaceRepository()
) : ViewModel() {
    private var loadJob: kotlinx.coroutines.Job? = null

    private val _uiState = MutableStateFlow<TouristPlacesUiState>(TouristPlacesUiState.Loading)
    val uiState: StateFlow<TouristPlacesUiState> = _uiState.asStateFlow()

    fun loadTouristPlaces(lat: Double, lon: Double, radius: Int = 5000) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = TouristPlacesUiState.Loading
            val result = repository.getTouristPlaces(lat, lon, radius)
            result.fold(
                onSuccess = { places ->
                    if (places.isEmpty()) {
                        _uiState.value = TouristPlacesUiState.Empty
                    } else {
                        _uiState.value = TouristPlacesUiState.Success(places)
                    }
                },
                onFailure = { error ->
                    _uiState.value = TouristPlacesUiState.Error(error.localizedMessage ?: "Failed to load tourist places")
                }
            )
        }
    }
}
