package com.nikkibuild.websocket.app.util

import com.google.gson.annotations.SerializedName

data class ServiceDefinition(
    @SerializedName("srvID")
    val serviceId: String,
    @SerializedName("instID")
    val instanceId: String,
    @SerializedName("proglang")
    val lang: String,
    val iDf: Df,
    val oDf: Df,
    val name: String,
    @SerializedName("dispName")
    val displayName: String,
    @SerializedName("desc")
    val description: String,
    val tags: List<String>
)

data class Df(
    val name: String,
    @SerializedName("desc")
    val description: String,
    val data: Any
)