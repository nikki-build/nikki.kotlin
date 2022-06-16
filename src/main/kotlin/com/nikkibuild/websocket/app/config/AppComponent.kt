package com.nikkibuild.websocket.app.config

import com.nikkibuild.websocket.app.App
import dagger.Component
import javax.inject.Singleton

@Component(modules = [ServiceConfig::class])
@Singleton
interface AppComponent {
    fun app(): App
}