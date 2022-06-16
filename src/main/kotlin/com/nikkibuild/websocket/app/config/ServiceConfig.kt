package com.nikkibuild.websocket.app.config

import com.nikkibuild.websocket.app.util.ServiceDefinition
import com.nikkibuild.websocket.app.util.ServiceToken
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class ServiceConfig {

    @Provides
    @Singleton
    fun okHttp(): OkHttpClient {
        return OkHttpClient.Builder()
            .pingInterval(2, TimeUnit.SECONDS)
            .build()
    }


    @Provides
    @Singleton
    fun serviceDef(provider: PropertiesProvider): ServiceDefinition {
        return provider.properties
    }

    @Provides
    @Singleton
    fun serviceToken(provider: PropertiesProvider): ServiceToken {
        return provider.serviceToken
    }

}