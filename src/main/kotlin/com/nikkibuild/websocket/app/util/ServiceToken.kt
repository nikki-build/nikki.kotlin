package com.nikkibuild.websocket.app.util

import com.google.gson.annotations.SerializedName

data class ServiceToken(
    @SerializedName("sessionID")
    val sessionId: String,
    @SerializedName("wsAddr")
    val wsAddress: String,
    @SerializedName("userID")
    val userId: String,
    @SerializedName("restAddr")
    val restAddress: String
)
