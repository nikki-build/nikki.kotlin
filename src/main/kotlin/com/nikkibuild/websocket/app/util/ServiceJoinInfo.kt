package com.nikkibuild.websocket.app.util

import com.google.gson.annotations.SerializedName

data class ServiceJoinInfo(
    @SerializedName("sessionID")
    val session: String,
    @SerializedName("userID")
    val user: String,
    @SerializedName("srv")
    val source: ServiceDefinition,
    @SerializedName("wsAddr")
    val wsAddress: String,
    val type: String = "service"
)
