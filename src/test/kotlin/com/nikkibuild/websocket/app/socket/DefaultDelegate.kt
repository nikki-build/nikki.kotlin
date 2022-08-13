package com.nikkibuild.websocket.app.socket

import okhttp3.WebSocket

class DefaultDelegate : SocketDelegate {
    override fun onConnect(socket: WebSocket) {

    }

    override fun onDisconnect(socket: WebSocket, reason: String) {
    }

    override fun onData(data: Any) {
    }
}