package com.nikkibuild.websocket.app.socket

import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.internal.platform.Platform
import okhttp3.internal.platform.Platform.Companion.INFO
import okhttp3.internal.ws.WebSocketReader
import okio.ByteString
import org.assertj.core.api.Assertions
import java.io.IOException
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class WebSocketRecorder(private val name: String) : WebSocketListener() {
    private val events: BlockingQueue<Any> = LinkedBlockingQueue()
    private var delegate: WebSocketListener? = null

    /** Sets a delegate for handling the next callback to this listener. Cleared after invoked.  */
    fun setNextEventDelegate(delegate: WebSocketListener?) {
        this.delegate = delegate
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Platform.get().log("[WS $name] onOpen", Platform.INFO, null)
        val delegate = delegate
        if (delegate != null) {
            this.delegate = null
            delegate.onOpen(webSocket, response)
        } else {
            events.add(Open(webSocket, response))
        }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        Platform.get().log("[WS $name] onMessage", Platform.INFO, null)
        val delegate = delegate
        if (delegate != null) {
            this.delegate = null
            delegate.onMessage(webSocket, bytes)
        } else {
            val event = Message(bytes)
            events.add(event)
        }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Platform.get().log("[WS $name] onMessage", INFO, null)
        val delegate = delegate
        if (delegate != null) {
            this.delegate = null
            delegate.onMessage(webSocket, text)
        } else {
            val event = Message(text)
            events.add(event)
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Platform.get().log("[WS $name] onClosing $code", INFO, null)
        val delegate = delegate
        if (delegate != null) {
            this.delegate = null
            delegate.onClosing(webSocket, code, reason)
        } else {
            events.add(Closing(code, reason))
        }
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Platform.get().log("[WS $name] onClosed $code", INFO, null)
        val delegate = delegate
        if (delegate != null) {
            this.delegate = null
            delegate.onClosed(webSocket, code, reason)
        } else {
            events.add(Closed(code, reason))
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Platform.get().log("[WS $name] onFailure", INFO, t)
        val delegate = delegate
        if (delegate != null) {
            this.delegate = null
            delegate.onFailure(webSocket, t, response)
        } else {
            events.add(Failure(t, response))
        }
    }

    private fun nextEvent(): Any {
        return try {
            val event = events.poll(10, TimeUnit.SECONDS)
                ?: throw AssertionError("Timed out waiting for event.")
            event
        } catch (e: InterruptedException) {
            throw AssertionError(e)
        }
    }

    fun assertTextMessage(payload: String?) {
        val actual = nextEvent()
        Assertions.assertThat(actual).isEqualTo(Message(payload))
    }

    fun assertBinaryMessage(payload: ByteString?) {
        val actual = nextEvent()
        Assertions.assertThat(actual).isEqualTo(Message(payload))
    }

    fun assertPing(payload: ByteString) {
        val actual = nextEvent()
        Assertions.assertThat(actual).isEqualTo(Ping(payload))
    }

    fun assertPong(payload: ByteString) {
        val actual = nextEvent()
        Assertions.assertThat(actual).isEqualTo(Pong(payload))
    }

    fun assertClosing(code: Int, reason: String) {
        val actual = nextEvent()
        Assertions.assertThat(actual).isEqualTo(Closing(code, reason))
    }

    fun assertClosed(code: Int, reason: String) {
        val actual = nextEvent()
        Assertions.assertThat(actual).isEqualTo(Closed(code, reason))
    }

    fun assertExhausted() {
        Assertions.assertThat(events).isEmpty()
    }

    fun assertOpen(): WebSocket {
        val event = nextEvent()
        if (event !is Open) {
            throw AssertionError("Expected Open but was $event")
        }
        return event.webSocket
    }

    fun assertFailure(t: Throwable?) {
        val event = nextEvent()
        if (event !is Failure) {
            throw AssertionError("Expected Failure but was $event")
        }
        val failure = event
        Assertions.assertThat(failure.response).isNull()
        Assertions.assertThat(failure.t).isSameAs(t)
    }

    fun assertFailure(cls: Class<out IOException?>?, vararg messages: String) {
        val event = nextEvent()
        if (event !is Failure) {
            throw AssertionError("Expected Failure but was $event")
        }
        val failure = event
        Assertions.assertThat(failure.response).isNull()
        Assertions.assertThat(failure.t.javaClass).isEqualTo(cls)
        if (messages.size > 0) {
            Assertions.assertThat(messages).contains(failure.t.message)
        }
    }

    fun assertFailure() {
        val event = nextEvent()
        if (event !is Failure) {
            throw AssertionError("Expected Failure but was $event")
        }
    }

    @Throws(IOException::class)
    fun assertFailure(code: Int, body: String?, cls: Class<out IOException?>?, message: String?) {
        val event = nextEvent()
        if (event !is Failure) {
            throw AssertionError("Expected Failure but was $event")
        }
        val failure = event
        Assertions.assertThat(failure.response!!.code).isEqualTo(code)
        if (body != null) {
            Assertions.assertThat(failure.responseBody).isEqualTo(body)
        }
        Assertions.assertThat(failure.t.javaClass).isEqualTo(cls)
        Assertions.assertThat(failure.t.message).isEqualTo(message)
    }

    /** Expose this recorder as a frame callback and shim in "ping" events.  */
    fun asFrameCallback(): WebSocketReader.FrameCallback {
        return object : WebSocketReader.FrameCallback {
            @Throws(IOException::class)
            override fun onReadMessage(text: String) {
                //onMessage(null, text)
            }

            @Throws(IOException::class)
            override fun onReadMessage(bytes: ByteString) {
                //onMessage(null, bytes)
            }

            override fun onReadPing(payload: ByteString) {
                events.add(Ping(payload))
            }

            override fun onReadPong(payload: ByteString) {
                events.add(Pong(payload))
            }

            override fun onReadClose(code: Int, reason: String) {
                //onClosing(null, code, reason)
            }
        }
    }

    internal class Open(val webSocket: WebSocket, val response: Response) {
        override fun toString(): String {
            return "Open[$response]"
        }
    }

    internal class Failure(val t: Throwable, val response: Response?) {
        val responseBody: String?

        init {
            var responseBody: String? = null
            if (response != null) {
                try {
                    responseBody = response.body!!.string()
                } catch (ignored: IOException) {
                }
            }
            this.responseBody = responseBody
        }

        override fun toString(): String {
            return if (response == null) {
                "Failure[$t]"
            } else "Failure[$response]"
        }
    }

    internal class Message {
        val bytes: ByteString?
        val string: String?

        constructor(bytes: ByteString?) {
            this.bytes = bytes
            string = null
        }

        constructor(string: String?) {
            bytes = null
            this.string = string
        }

        override fun toString(): String {
            return "Message[" + (bytes ?: string) + "]"
        }

        override fun hashCode(): Int {
            return (bytes ?: string).hashCode()
        }

        override fun equals(other: Any?): Boolean {
            return other is Message && other.bytes == bytes && other.string == string
        }
    }

    internal class Ping(val payload: ByteString) {
        override fun toString(): String {
            return "Ping[$payload]"
        }

        override fun hashCode(): Int {
            return payload.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            return (other is Ping
                    && other.payload.equals(payload))
        }
    }

    internal class Pong(val payload: ByteString) {
        override fun toString(): String {
            return "Pong[$payload]"
        }

        override fun hashCode(): Int {
            return payload.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            return (other is Pong
                    && other.payload.equals(payload))
        }
    }

    internal class Closing(val code: Int, val reason: String) {
        override fun toString(): String {
            return "Closing[$code $reason]"
        }

        override fun hashCode(): Int {
            return code * 37 + reason.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            return other is Closing && other.code == code && (other as Closing).reason == reason
        }
    }

    internal class Closed(val code: Int, val reason: String) {
        override fun toString(): String {
            return "Closed[$code $reason]"
        }

        override fun hashCode(): Int {
            return code * 37 + reason.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            return other is Closed && other.code == code && (other as Closed).reason == reason
        }
    }
}