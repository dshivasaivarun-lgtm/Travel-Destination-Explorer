package com.pes.travelexplorer.data.model

import com.google.gson.annotations.SerializedName

data class WikimediaQueryResponse(
    @SerializedName("query")
    val query: WikimediaQueryData?
)

data class WikimediaQueryData(
    @SerializedName("pages")
    val pages: Map<String, WikimediaPage>?
)

data class WikimediaPage(
    @SerializedName("pageid")
    val pageId: Long?,

    @SerializedName("title")
    val title: String?,

    @SerializedName("imageinfo")
    val imageInfo: List<WikimediaImageInfo>?
)

data class WikimediaImageInfo(
    @SerializedName("url")
    val url: String?,

    @SerializedName("descriptionurl")
    val descriptionUrl: String?,

    @SerializedName("user")
    val user: String?,

    @SerializedName("extmetadata")
    val extMetadata: Map<String, WikimediaExtMetaValue>?
)

data class WikimediaExtMetaValue(
    @SerializedName("value")
    val value: String?
)
