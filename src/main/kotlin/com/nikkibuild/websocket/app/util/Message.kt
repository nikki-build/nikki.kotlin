package com.nikkibuild.websocket.app.util

import com.google.gson.annotations.SerializedName

data class Message(
    val name: String,
    val data: Data,
    val desc: String,
    val msg: String,
    val status: String = "ok",
    val action :String = "srvMsg",
    @SerializedName("sessionID") val session: String,
    @SerializedName("srvID") val service: String,
    @SerializedName("instID") val instance: String,
)

data class Data(val name: String, val data: Any, @SerializedName("desc") val description: String)
