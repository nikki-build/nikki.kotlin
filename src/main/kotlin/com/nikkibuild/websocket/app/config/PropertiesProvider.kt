package com.nikkibuild.websocket.app.config

import com.google.gson.Gson
import com.nikkibuild.websocket.app.util.ServiceDefinition
import com.nikkibuild.websocket.app.util.ServiceToken
import java.io.FileInputStream
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PropertiesProvider @Inject constructor() {

    val properties: ServiceDefinition
    val serviceToken: ServiceToken

    init {
        val stream = FileInputStream("./serviceDef.json")
        val reader = InputStreamReader(stream)
        properties = Gson().fromJson(reader, ServiceDefinition::class.java)
        val s = FileInputStream("./serviceToken.json")
        val r = InputStreamReader(s)
        serviceToken = Gson().fromJson(r, ServiceToken::class.java)
    }
}