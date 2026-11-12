package com.pes.travelexplorer.data.model

import com.google.gson.annotations.SerializedName

data class RegionResponse(
    @SerializedName("data")
    val data: List<AdministrativeDivision>?,

    @SerializedName("regions")
    val regions: List<AdministrativeDivision>?,

    @SerializedName("items")
    val items: List<AdministrativeDivision>?
)

data class AdministrativeDivision(
    @SerializedName("id")
    val id: String?,

    @SerializedName("name")
    val name: String?,

    @SerializedName("code")
    val code: String?,

    @SerializedName("type")
    val type: String?
)
