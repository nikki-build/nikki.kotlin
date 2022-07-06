package com.nikkibuild.websocket.app.socket

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import java.time.Duration

internal class ThrottleManager constructor(capacity: Long, duration: Long) {
    private val limit = Bandwidth.simple(capacity, Duration.ofMinutes(duration))
    private val bucket = Bucket.builder().addLimit(limit).build()

    fun canSend(): Boolean {
        return bucket.tryConsume(1)
    }
}