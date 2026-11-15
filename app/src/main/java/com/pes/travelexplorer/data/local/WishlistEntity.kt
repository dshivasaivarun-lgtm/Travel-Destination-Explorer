package com.pes.travelexplorer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wishlist")
data class WishlistEntity(

    @PrimaryKey
    val cca3: String,

    val countryName: String,

    val officialName: String,

    val flagUrl: String?,

    val region: String?
)