package com.nikkibuild.websocket.app.config

import com.nikkibuild.websocket.app.util.ServiceDefinition
import com.nikkibuild.websocket.app.util.ServiceToken

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

internal class ServiceConfig(configPath: String, tokenPath: String) {
    private val properties = PropertiesProvider(tokenPath, configPath)
    fun okHttp(): OkHttpClient {
        return OkHttpClient.Builder()
            .pingInterval(2, TimeUnit.SECONDS)
            .build()
    }


    fun serviceDef(): ServiceDefinition {
        return properties.properties
    }

    fun serviceToken(): ServiceToken {
        return properties.serviceToken
    }

}