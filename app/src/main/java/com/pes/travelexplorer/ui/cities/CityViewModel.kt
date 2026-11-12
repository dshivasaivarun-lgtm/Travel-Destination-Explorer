package com.pes.travelexplorer.ui.cities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pes.travelexplorer.data.model.City
import com.pes.travelexplorer.data.repository.CityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CityUiState {
    object Loading : CityUiState
    data class Success(val cities: List<City>) : CityUiState
    object Empty : CityUiState
    data class Error(val message: String) : CityUiState
}

class CityViewModel(
    private val repository: CityRepository = CityRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<CityUiState>(CityUiState.Loading)
    val uiState: StateFlow<CityUiState> = _uiState.asStateFlow()

    fun loadCities(countryCode: String, admin1Id: String) {
        viewModelScope.launch {
            _uiState.value = CityUiState.Loading
            val result = repository.getCitiesForDivision(countryCode, admin1Id)
            result.fold(
                onSuccess = { cities ->
                    if (cities.isEmpty()) {
                        _uiState.value = CityUiState.Empty
                    } else {
                        _uiState.value = CityUiState.Success(cities)
                    }
                },
                onFailure = { error ->
                    _uiState.value = CityUiState.Error(error.localizedMessage ?: "Failed to load cities")
                }
            )
        }
    }
}
