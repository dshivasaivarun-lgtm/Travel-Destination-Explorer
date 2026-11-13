
package com.pes.travelexplorer.ui.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pes.travelexplorer.data.local.WishlistEntity
import com.pes.travelexplorer.data.repository.WishlistRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class WishlistViewModel(
    private val repository: WishlistRepository
) : ViewModel() {
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    fun clearError() { _error.value = null }
    private suspend fun safely(block: suspend () -> Unit) {
        try { block() }
        catch (e: CancellationException) { throw e }
        catch (_: Exception) { _error.value = "storage" }
    }


    // Observe saved countries
    val wishlist: StateFlow<List<WishlistEntity>> =
        repository.getAllWishlist().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Add country
    fun addCountry(country: WishlistEntity) {
        viewModelScope.launch {
            safely { repository.addToWishlist(country) }
        }
    }

    // Remove country
    fun removeCountry(countryCode: String) {
        viewModelScope.launch {
            safely { repository.removeFromWishlist(countryCode) }
        }
    }

    // Check whether country is already saved
    fun isCountrySaved(
        countryCode: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            onResult(repository.isInWishlist(countryCode))
        }
    }

    // Clear wishlist
    fun clearWishlist() {
        viewModelScope.launch {
            safely { repository.clearWishlist() }
        }
    }
}
