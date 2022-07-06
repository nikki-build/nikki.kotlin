package com.nikkibuild.websocket.app.socket

import com.google.gson.Gson
import com.nikkibuild.websocket.app.util.Message
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

internal class SocketEventListener constructor(private val delegate: SocketDelegate) : WebSocketListener() {
    private val mapper = Gson()
    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        delegate.onDisconnect(webSocket, reason)
    }


    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        delegate.onConnect(webSocket)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        try {
            val message = mapper.fromJson(text, Message::class.java) ?: return
            delegate.onMessage(message)
        } catch (e: Exception) {
            println("Not data message ($text)")
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        delegate.onDisconnect(webSocket, t.localizedMessage)
    }

}

data class SocketEvent(val socket: WebSocket, val status: SocketStatus) {
    enum class SocketStatus {
        CLOSED, OPENED, ERROR
    }
}
