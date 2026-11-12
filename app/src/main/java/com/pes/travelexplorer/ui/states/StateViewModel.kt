package com.pes.travelexplorer.ui.states

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pes.travelexplorer.data.model.AdministrativeDivision
import com.pes.travelexplorer.data.repository.StateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface StateUiState {
    object Loading : StateUiState
    data class Success(val states: List<AdministrativeDivision>) : StateUiState
    object Empty : StateUiState
    data class Error(val message: String) : StateUiState
}

class StateViewModel(
    private val repository: StateRepository = StateRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<StateUiState>(StateUiState.Loading)
    val uiState: StateFlow<StateUiState> = _uiState.asStateFlow()

    fun loadStates(countryCode: String) {
        viewModelScope.launch {
            _uiState.value = StateUiState.Loading
            val result = repository.getStatesForCountry(countryCode)
            result.fold(
                onSuccess = { states ->
                    if (states.isEmpty()) {
                        _uiState.value = StateUiState.Empty
                    } else {
                        _uiState.value = StateUiState.Success(states)
                    }
                },
                onFailure = { error ->
                    _uiState.value = StateUiState.Error(error.localizedMessage ?: "Failed to load states")
                }
            )
        }
    }
}
