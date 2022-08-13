package com.nikkibuild.websocket.app.socket

import io.reactivex.rxjava3.kotlin.subscribeBy
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.internal.concurrent.TaskRunner
import okhttp3.internal.ws.RealWebSocket
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.*


internal class SocketManagerTest {
    private val clientListener = WebSocketRecorder("client")
    private val serverListener = WebSocketRecorder("server")
    private val client: OkHttpClient = OkHttpClient.Builder()
        .writeTimeout(Duration.ofMillis(500))
        .readTimeout(Duration.ofMillis(500))
        .addInterceptor { chain ->
            val response: Response = chain.proceed(chain.request())
            // Ensure application interceptors never see a null body.
            assertThat(response.body).isNotNull
            response
        }
        .build()


    @Test
    fun `when bandwidth limit reached`() {
        val socketManager = SocketManager("./serviceToken.json", "./serviceDef.json", 1, 1, DefaultDelegate())
        socketManager.sendData(Any()).subscribeBy({}, {})
        socketManager.sendData(Any())
            .test()
            .assertError(IllegalArgumentException::class.java)
    }

    @Test
    fun `send when everything is ok`() {
        val socketManager = SocketManager("./serviceToken.json", "./serviceDef.json", 1, 1, DefaultDelegate())
        socketManager.sendData(Any())
            .test()
            .assertComplete()
    }

    @Test
    fun `connection open success`() {
        val mock = MockWebServer()
        mock.enqueue(MockResponse().withWebSocketUpgrade(clientListener))
        val socket = newWebSocket(mock)
        clientListener.assertOpen()
        socket.close(1001, "")
    }

    @Test
    fun `message send success`() {
        val mock = MockWebServer()
        mock.enqueue(MockResponse().withWebSocketUpgrade(serverListener))
        val webSocket = newWebSocket(mock)
        clientListener.assertOpen()
        val server = serverListener.assertOpen()
        webSocket.send("Hello, WebSockets!")
        serverListener.assertTextMessage("Hello, WebSockets!")
        closeWebSockets(webSocket, server)
    }

    @Test
    fun `message receive success`() {
        val mock = MockWebServer()
        mock.enqueue(MockResponse().withWebSocketUpgrade(serverListener))
        val webSocket = newWebSocket(mock)

        clientListener.assertOpen()
        val server = serverListener.assertOpen()

        server.send("Hello, WebSockets!")
        clientListener.assertTextMessage("Hello, WebSockets!")
        closeWebSockets(webSocket, server)

    }

    @Test
    fun `when connection is closing`() {
        val mock = MockWebServer()
        mock.enqueue(MockResponse().withWebSocketUpgrade(serverListener))
        val webSocket = newWebSocket(mock)

        clientListener.assertOpen()
        val server = serverListener.assertOpen()
        server.close(1001, "abc")
        clientListener.assertClosing(1001, "abc")
    }

    private fun closeWebSockets(webSocket: WebSocket, server: WebSocket) {
        server.close(1001, "")
        clientListener.assertClosing(1001, "")
        webSocket.close(1000, "")
        serverListener.assertClosing(1000, "")
        clientListener.assertClosed(1001, "")
        serverListener.assertClosed(1000, "")
        clientListener.assertExhausted()
        serverListener.assertExhausted()
    }

    private fun newWebSocket(webServer: MockWebServer): RealWebSocket {
        return newWebSocket(Request.Builder().get().url(webServer.url("/")).build())
    }

    private fun newWebSocket(request: Request): RealWebSocket {
        val webSocket = RealWebSocket(
            TaskRunner.INSTANCE, request, clientListener,
            Random(), client.pingIntervalMillis.toLong(), null, 0L
        )
        webSocket.connect(client)
        return webSocket
    }
}