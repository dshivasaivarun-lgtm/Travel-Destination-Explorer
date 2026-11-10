package com.pes.travelexplorer.data.model

import com.google.gson.annotations.SerializedName

data class CityResponse(
    @SerializedName("data")
    val data: List<City>?,

    @SerializedName("cities")
    val cities: List<City>?,

    @SerializedName("items")
    val items: List<City>?,

    @SerializedName("objects")
    val objects: List<City>?,

    @SerializedName("settlements")
    val settlements: List<City>?
)

data class City(
    @SerializedName("id")
    val id: String?,

    @SerializedName("name")
    val name: String?,

    @SerializedName("latitude")
    val latitude: Double?,

    @SerializedName("longitude")
    val longitude: Double?,

    @SerializedName("population")
    val population: Long?,

    @SerializedName("type")
    val type: String?
)
