
package com.pes.travelexplorer.ui.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pes.travelexplorer.data.local.WishlistEntity
import com.pes.travelexplorer.data.repository.WishlistRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WishlistViewModel(
    private val repository: WishlistRepository
) : ViewModel() {

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
            repository.addToWishlist(country)
        }
    }

    // Remove country
    fun removeCountry(countryCode: String) {
        viewModelScope.launch {
            repository.removeFromWishlist(countryCode)
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
            repository.clearWishlist()
        }
    }
}
