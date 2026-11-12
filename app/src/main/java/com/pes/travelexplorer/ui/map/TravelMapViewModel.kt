package com.pes.travelexplorer.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pes.travelexplorer.data.model.TouristPlace
import com.pes.travelexplorer.data.repository.TouristPlaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TravelMapUiState {
    object Loading : TravelMapUiState
    data class Success(val places: List<TouristPlace>) : TravelMapUiState
    data class Error(val message: String) : TravelMapUiState
}

class TravelMapViewModel(
    private val repository: TouristPlaceRepository = TouristPlaceRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<TravelMapUiState>(TravelMapUiState.Loading)
    val uiState: StateFlow<TravelMapUiState> = _uiState.asStateFlow()

    fun loadMapData(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = TravelMapUiState.Loading
            val result = repository.getTouristPlaces(lat, lon)
            result.fold(
                onSuccess = { places ->
                    _uiState.value = TravelMapUiState.Success(places)
                },
                onFailure = { error ->
                    _uiState.value = TravelMapUiState.Error(error.localizedMessage ?: "Failed to load map data")
                }
            )
        }
    }
}
