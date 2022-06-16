package com.nikkibuild.websocket.app.socket

import com.google.gson.Gson
import com.nikkibuild.websocket.app.util.Message
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketEventListener @Inject constructor() : WebSocketListener() {
    private val logger = LoggerFactory.getLogger(SocketEventListener::class.java)
    var delegate: SocketDelegate? = null
    private val statusSubject = PublishSubject.create<SocketEvent>()
    private val messageSubject = PublishSubject.create<String>()
    private val mapper = Gson()
    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        statusSubject.onNext(SocketEvent(webSocket, SocketEvent.SocketStatus.CLOSED))
        delegate?.onDisconnect(webSocket, reason)
    }


    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        statusSubject.onNext(SocketEvent(webSocket, SocketEvent.SocketStatus.OPENED))
        delegate?.onConnect(webSocket)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        messageSubject.onNext(text)
        try {
            val message = mapper.fromJson(text, Message::class.java) ?: return
            delegate?.onMessage(message)
        } catch (e: Exception) {
            println("Not data message ($text)")
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        statusSubject.onNext(SocketEvent(webSocket, SocketEvent.SocketStatus.ERROR))
        delegate?.onDisconnect(webSocket, t.localizedMessage)
    }

    fun status(): Observable<SocketEvent> {
        return statusSubject.hide()
    }

    fun messages(): Observable<String> {
        return messageSubject.hide()
    }
}

data class SocketEvent(val socket: WebSocket, val status: SocketStatus) {
    enum class SocketStatus {
        CLOSED, OPENED, ERROR
    }
}
