package com.nikkibuild.websocket.app.socket

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThrottleManager @Inject constructor() {
    private val limit = Bandwidth.simple(5, Duration.ofMinutes(1))
    private val bucket = Bucket.builder().addLimit(limit).build()

    fun canSend(): Boolean {
        return bucket.tryConsume(1)
    }
}