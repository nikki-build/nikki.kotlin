package com.nikkibuild.websocket.app.socket

import com.google.gson.Gson
import com.nikkibuild.websocket.app.config.ServiceConfig
import com.nikkibuild.websocket.app.util.Message
import com.nikkibuild.websocket.app.util.ServiceJoinInfo
import io.reactivex.rxjava3.core.Completable
import okhttp3.Request
import okhttp3.WebSocket
import org.apache.commons.codec.binary.Base64
import java.nio.charset.StandardCharsets

class SocketManager constructor(
    tokenPath: String,
    defPath: String,
    throttleCapacity: Long,
    throttleDurationMinutes: Long,
    delegate: SocketDelegate
) {
    private val serviceConfig = ServiceConfig(configPath = defPath, tokenPath = tokenPath)
    private val eventListener = SocketEventListener(delegate)
    private val okHttp = serviceConfig.okHttp()
    val definition = serviceConfig.serviceDef()
    val token = serviceConfig.serviceToken()
    private val throttleManager = ThrottleManager(throttleCapacity, throttleDurationMinutes)
    private var socket: WebSocket? = null
    private val transformer = Gson()

    fun start(): Completable {
        return Completable.create {
            try {
                doClose()
                val request = Request.Builder()
                    .url(token.wsAddress.plus("?wsKey=${makeInfoParam()}"))
                    .build()
                socket = okHttp.newWebSocket(request, eventListener)
                if (it.isDisposed.not()) {
                    it.onComplete()
                }
            } catch (e: Exception) {
                it.tryOnError(e)
            }
        }
    }

    private fun makeInfoParam(): String {
        val info = ServiceJoinInfo(token.sessionId, token.userId, definition, token.wsAddress)
        val json = transformer.toJson(info)
        return Base64.encodeBase64URLSafeString(json.toByteArray(StandardCharsets.UTF_8))
    }

    fun stop(): Completable {
        return Completable.create {
            try {
                doClose()
                if (it.isDisposed.not()) {
                    it.onComplete()
                }
            } catch (e: Exception) {
                it.tryOnError(e)
            }
        }
    }

    private fun doClose() {
        socket?.close(1000, "Closing on user request.")
        socket = null
    }

    fun send(message: Message): Completable {
        return Completable.create {
            try {
                if (!throttleManager.canSend()) {
                    throw IllegalArgumentException("Bandwidth limit reached. Limit [5] messages in a minute")
                }
                val json = transformer.toJson(message)
                socket?.send(json)
                if (it.isDisposed.not()) {
                    it.onComplete()
                }
            } catch (e: Exception) {
                it.tryOnError(e)
            }
        }
    }
}