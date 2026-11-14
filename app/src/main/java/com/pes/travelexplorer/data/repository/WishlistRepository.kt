
package com.pes.travelexplorer.data.repository

import com.pes.travelexplorer.data.local.WishlistDao
import com.pes.travelexplorer.data.local.WishlistEntity
import kotlinx.coroutines.flow.Flow

class WishlistRepository(
    private val wishlistDao: WishlistDao
) {

    // Observe all saved countries
    fun getAllWishlist(): Flow<List<WishlistEntity>> {
        return wishlistDao.getAllWishlist()
    }

    // Add a country to wishlist
    suspend fun addToWishlist(country: WishlistEntity): Long {
        return wishlistDao.addToWishlist(country)
    }

    // Remove a country from wishlist
    suspend fun removeFromWishlist(countryCode: String) {
        wishlistDao.removeFromWishlist(countryCode)
    }

    // Check whether a country is already saved
    suspend fun isInWishlist(countryCode: String): Boolean {
        return wishlistDao.isInWishlist(countryCode)
    }

    // Clear wishlist
    suspend fun clearWishlist() {
        wishlistDao.clearWishlist()
    }
}
