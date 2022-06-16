package com.nikkibuild.websocket.app.socket

import com.nikkibuild.websocket.app.util.Message
import okhttp3.WebSocket

interface SocketDelegate {
    fun onConnect(socket: WebSocket)
    fun onDisconnect(socket: WebSocket, reason: String)
    fun onMessage(message: Message)
}