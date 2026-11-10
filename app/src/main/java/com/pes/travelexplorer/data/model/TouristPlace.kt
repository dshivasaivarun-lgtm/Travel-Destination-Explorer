package com.pes.travelexplorer.data.model

import com.google.gson.annotations.SerializedName

data class OverpassResponse(
    @SerializedName("elements")
    val elements: List<OverpassElement>?
)

data class OverpassElement(
    @SerializedName("id")
    val id: Long?,

    @SerializedName("lat")
    val lat: Double?,

    @SerializedName("lon")
    val lon: Double?,

    @SerializedName("center")
    val center: OverpassCenter?,

    @SerializedName("tags")
    val tags: Map<String, String>?
)

data class OverpassCenter(
    @SerializedName("lat")
    val lat: Double?,

    @SerializedName("lon")
    val lon: Double?
)

data class TouristPlace(
    val id: Long,
    val name: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val website: String?,
    val openingHours: String?
)
