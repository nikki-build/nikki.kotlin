package com.nikkibuild.websocket.app.config

import com.google.gson.Gson
import com.nikkibuild.websocket.app.util.ServiceDefinition
import com.nikkibuild.websocket.app.util.ServiceToken
import java.io.FileInputStream
import java.io.InputStreamReader

internal class PropertiesProvider(tokenPath: String, defPath: String) {

    val properties: ServiceDefinition
    val serviceToken: ServiceToken

    init {
        val stream = FileInputStream(defPath)
        val reader = InputStreamReader(stream)
        properties = Gson().fromJson(reader, ServiceDefinition::class.java)
        val s = FileInputStream(tokenPath)
        val r = InputStreamReader(s)
        serviceToken = Gson().fromJson(r, ServiceToken::class.java)
    }
}