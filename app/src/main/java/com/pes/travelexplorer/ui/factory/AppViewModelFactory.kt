
package com.pes.travelexplorer.ui.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pes.travelexplorer.data.local.AppDatabase
import com.pes.travelexplorer.data.repository.TripRepository
import com.pes.travelexplorer.data.repository.WishlistRepository
import com.pes.travelexplorer.ui.trips.TripsViewModel
import com.pes.travelexplorer.ui.wishlist.WishlistViewModel

class AppViewModelFactory(
    private val database: AppDatabase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        return when {
            modelClass.isAssignableFrom(WishlistViewModel::class.java) -> {
                WishlistViewModel(
                    WishlistRepository(database.wishlistDao())
                ) as T
            }

            modelClass.isAssignableFrom(TripsViewModel::class.java) -> {
                TripsViewModel(
                    TripRepository(database.tripDao())
                ) as T
            }

            else -> throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}"
            )
        }
    }
}
