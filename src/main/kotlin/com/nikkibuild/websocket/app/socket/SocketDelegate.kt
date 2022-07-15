package com.nikkibuild.websocket.app.socket

import okhttp3.WebSocket

interface SocketDelegate {
    fun onConnect(socket: WebSocket)
    fun onDisconnect(socket: WebSocket, reason: String)
    fun onData(data: Any)
}