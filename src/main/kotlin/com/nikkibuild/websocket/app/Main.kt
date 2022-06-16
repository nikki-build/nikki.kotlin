package com.nikkibuild.websocket.app

import com.google.gson.Gson
import com.nikkibuild.websocket.app.config.DaggerAppComponent
import com.nikkibuild.websocket.app.socket.SocketDelegate
import com.nikkibuild.websocket.app.socket.SocketEventListener
import com.nikkibuild.websocket.app.socket.SocketManager
import com.nikkibuild.websocket.app.util.*
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.WebSocket
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.exitProcess

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val app = DaggerAppComponent.create().app()
            app.startApp()
        }
    }

    fun readJson(filePath: String): Anything {
        val input = InputStreamReader(FileInputStream(filePath))
        return Gson().fromJson(input, Anything::class.java)
    }
}

@Singleton
class App @Inject constructor(
    private val socketManager: SocketManager,
    private val eventListener: SocketEventListener,
    private val serviceDefinition: ServiceDefinition,
    private val token: ServiceToken
) : SocketDelegate {
    private var connected = false
    fun startApp() {
        println("---------- W E B  S O C K E T ----------")
        eventListener.delegate = this
        showMenu()
    }

    private fun showMenu() {
        while (true) {
            if (connected) {
                println("[1] Send Message")
                println("[2] Disconnect")
            } else {
                println("[1] Start Socket")
            }
            println("[0] Exit")
            print("CMD ~> ")
            when (readlnOrNull()?.toIntOrNull()) {
                null -> println("Invalid command.")
                1 -> {
                    if (connected) {
                        send()
                    } else {
                        start()
                    }
                }
                2 -> stop()
                0 -> exit()
            }
        }
    }

    private fun send() {
        val text = UUID.randomUUID().toString()
        println("Sending=> $text")
        val message = temporaryMessage(text)
        socketManager.send(message)
            .subscribeOn(Schedulers.io())
            .subscribeBy(onError = {
                println()
                println(it.localizedMessage)
            }, onComplete = {
                println()
                println("Message sent!")
            })
    }

    private fun temporaryMessage(text: String): Message {
        val message = Anything(text)
        val d = Data("n", message, "d")
        return Message(
            "t", d, "d", "msg", "ok", token.sessionId,
            serviceDefinition.serviceId,
            serviceDefinition.instanceId
        )
    }


    private fun start() {
        println("Connecting...")
        connected = true
        socketManager.start()
            .subscribeOn(Schedulers.io())
            .subscribeBy(onError = {
                println("Error in connecting. Try connecting again.")
            }, onComplete = {

            })
    }

    private fun stop() {
        println("Disconnecting...")
        connected = false
        socketManager.stop()
            .subscribeOn(Schedulers.io())
            .subscribeBy(onError = {
                println("Oha! failed to disconnect. ${it.localizedMessage}")
            }, onComplete = {
            })
    }

    private fun exit() {
        println("Bye!")
        exitProcess(0)
    }

    override fun onConnect(socket: WebSocket) {
        connected = true
        println()
        println("Connected")
    }

    override fun onDisconnect(socket: WebSocket, reason: String) {
        doOnDisconnect(reason)
    }

    private fun doOnDisconnect(reason: String) {
        println()
        println("Disconnected ($reason)")
        connected = false
    }

    override fun onMessage(message: Message) {
        println()
        println("New message received! ${message.name}")
    }
}