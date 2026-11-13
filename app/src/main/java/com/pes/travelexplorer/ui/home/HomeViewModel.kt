package com.pes.travelexplorer.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pes.travelexplorer.data.model.Country
import com.pes.travelexplorer.data.repository.CountryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Content(val countries: List<Country>) : HomeUiState
    data object Error : HomeUiState
}

class HomeViewModel(
    private val loadCountries: suspend () -> Result<List<Country>> = {
        CountryRepository().getAllCountries()
    }
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        retry()
    }

    fun retry() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val result = loadCountries()
                // Some repositories wrap cancellation in Result.failure.
                val failure = result.exceptionOrNull()
                if (failure is CancellationException) throw failure
                _uiState.value = result.fold(
                    onSuccess = { countries ->
                        HomeUiState.Content(countries.sortedWith(
                            compareBy(String.CASE_INSENSITIVE_ORDER) { it.names?.common.orEmpty() }
                        ))
                    },
                    onFailure = { HomeUiState.Error }
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // Keep raw server/credential information out of the UI.
                _uiState.value = HomeUiState.Error
            }
        }
    }
}
