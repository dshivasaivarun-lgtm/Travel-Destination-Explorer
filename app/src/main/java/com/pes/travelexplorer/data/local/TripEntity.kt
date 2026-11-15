
package com.pes.travelexplorer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    val destinationCountryCode: String,

    val destinationCountryName: String,

    val startDate: String,

    val endDate: String,

    val notes: String = ""
)
