
package com.pes.travelexplorer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {

    // Add a country. Ignore it if it already exists.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addToWishlist(country: WishlistEntity): Long

    // Get all saved countries.
    @Query("SELECT * FROM wishlist ORDER BY countryName ASC")
    fun getAllWishlist(): Flow<List<WishlistEntity>>

    // Remove a country using its alpha-3 code.
    @Query("DELETE FROM wishlist WHERE cca3 = :countryCode")
    suspend fun removeFromWishlist(countryCode: String)

    // Check whether a country is already saved.
    @Query("SELECT EXISTS(SELECT 1 FROM wishlist WHERE cca3 = :countryCode)")
    suspend fun isInWishlist(countryCode: String): Boolean

    // Remove all saved countries.
    @Query("DELETE FROM wishlist")
    suspend fun clearWishlist()
}
