package com.nikkibuild.websocket.app.config

import com.google.gson.Gson
import com.nikkibuild.websocket.app.util.ServiceDefinition
import com.nikkibuild.websocket.app.util.ServiceToken
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

internal class PropertiesProvider(tokenPath: String, defPath: String) {

    val properties: ServiceDefinition
    val serviceToken: ServiceToken

    init {
        val deFile = File(defPath)
        if (deFile.exists().not()) {
            throw IllegalArgumentException("[{$defPath}] File not found.")
        }
        val tFile = File(tokenPath)
        if (tFile.exists().not()) {
            throw IllegalArgumentException("[{$tokenPath}] File not found.")
        }
        val stream = FileInputStream(deFile)
        val reader = InputStreamReader(stream).readText()
        properties = Gson().fromJson(reader, ServiceDefinition::class.java)
        if (properties?.instanceId == null) {
            throw IllegalArgumentException("Invalid service definition")
        }
        val s = FileInputStream(tFile)
        val r = InputStreamReader(s).readText()
        serviceToken = Gson().fromJson(r, ServiceToken::class.java)
        if (serviceToken?.sessionId == null) {
            throw IllegalArgumentException("Invalid service token")
        }
    }
}