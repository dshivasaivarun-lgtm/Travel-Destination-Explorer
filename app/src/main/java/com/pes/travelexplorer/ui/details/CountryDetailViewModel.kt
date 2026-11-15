package com.pes.travelexplorer.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pes.travelexplorer.data.model.Country
import com.pes.travelexplorer.data.repository.CountryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException

sealed interface CountryDetailUiState {
    object Loading : CountryDetailUiState
    data class Success(val country: Country) : CountryDetailUiState
    data class Error(val message: String) : CountryDetailUiState
}

class CountryDetailViewModel(
    private val repository: CountryRepository = CountryRepository(),
    private val loadCountry: suspend (String) -> Result<Country?> = repository::getCountryByCode
) : ViewModel() {

    private val _uiState = MutableStateFlow<CountryDetailUiState>(CountryDetailUiState.Loading)
    val uiState: StateFlow<CountryDetailUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    fun loadCountryDetails(alpha3: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = CountryDetailUiState.Loading
            val result = loadCountry(alpha3)
            result.exceptionOrNull()?.let { error ->
                if (error is CancellationException) throw error
            }
            result.fold(
                onSuccess = { country ->
                    if (country != null) {
                        _uiState.value = CountryDetailUiState.Success(country)
                    } else {
                        _uiState.value = CountryDetailUiState.Error("Country not found for code: $alpha3")
                    }
                },
                onFailure = { error ->
                    _uiState.value = CountryDetailUiState.Error(error.localizedMessage ?: "Unknown error occurred")
                }
            )
        }
    }
}
