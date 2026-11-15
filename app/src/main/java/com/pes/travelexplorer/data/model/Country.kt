
package com.pes.travelexplorer.data.model

import com.google.gson.annotations.SerializedName

data class CountryResponse(
    @SerializedName("data")
    val data: CountryData?
)

data class CountryData(
    @SerializedName("objects")
    val objects: List<Country>?,
    @SerializedName("meta") val meta: CountryPageMeta? = null
)
data class CountryPageMeta(
    val total: Int = 0,
    val count: Int = 0,
    val limit: Int = 25,
    val offset: Int = 0,
    val more: Boolean = false
)

data class Country(
    @SerializedName("names")
    val names: CountryNames?,

    @SerializedName("codes")
    val codes: CountryCodes?,

    @SerializedName("capitals")
    val capitals: List<Capital>?,

    @SerializedName("flag")
    val flag: CountryFlag?,

    @SerializedName("region")
    val region: String?,

    @SerializedName("subregion")
    val subregion: String?,

    @SerializedName("population")
    val population: Long?,

    @SerializedName("currencies")
    val currencies: List<Currency>?,

    @SerializedName("languages")
    val languages: List<Language>?
)

data class CountryNames(
    @SerializedName("common")
    val common: String?,

    @SerializedName("official")
    val official: String?
)

data class CountryCodes(
    @SerializedName("alpha_2")
    val alpha2: String?,

    @SerializedName("alpha_3")
    val alpha3: String?
)

data class Capital(
    @SerializedName("name")
    val name: String?
)

data class CountryFlag(
    @SerializedName("url_png")
    val urlPng: String?,

    @SerializedName("url_svg")
    val urlSvg: String?,

    @SerializedName("emoji")
    val emoji: String?,

    @SerializedName("description")
    val description: String?
)

data class Currency(
    @SerializedName("code")
    val code: String?,

    @SerializedName("name")
    val name: String?,

    @SerializedName("symbol")
    val symbol: String?
)

data class Language(
    @SerializedName("name")
    val name: String?,

    @SerializedName("native_name")
    val nativeName: String?
)
