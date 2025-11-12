package com.goquestly.data.remote.websocket

import android.util.Log
import com.goquestly.data.local.TokenManager
import com.goquestly.data.remote.dto.SocketErrorDto
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

abstract class BaseSocketService(
    private val tokenManager: TokenManager,
    protected val json: Json,
    private val endpoint: String,
    private val logTag: String
) {
    protected var socket: Socket? = null
    private val errorCallbacks = mutableListOf<(SocketErrorDto) -> Unit>()
    private val customErrorCallbacks = mutableMapOf<String, MutableList<(String) -> Unit>>()

    companion object {
        private const val RECONNECTION_DELAY_MS = 1000L
        private const val RECONNECTION_DELAY_MAX_MS = 5000L
        private const val TIMEOUT_MS = 10_000L
    }

    private fun getWebSocketUrl(baseUrl: String): String {
        return "${baseUrl.removeSuffix("/")}/$endpoint"
    }

    suspend fun connect(baseUrl: String) {
        if (socket?.connected() == true) {
            Log.d(logTag, "Already connected")
            return
        }

        val token = tokenManager.getToken()
            ?: throw IllegalStateException("No token available")

        val url = getWebSocketUrl(baseUrl)
        val options = IO.Options().apply {
            auth = mapOf("token" to token)
            reconnection = true
            reconnectionAttempts = Int.MAX_VALUE
            reconnectionDelay = RECONNECTION_DELAY_MS
            reconnectionDelayMax = RECONNECTION_DELAY_MAX_MS
            transports = arrayOf("websocket", "polling")
            timeout = TIMEOUT_MS
            forceNew = true
        }

        suspendCancellableCoroutine { continuation ->
            var isResumed = false

            socket = IO.socket(url, options).apply {
                once(Socket.EVENT_CONNECT) {
                    if (!isResumed) {
                        isResumed = true
                        Log.i(logTag, "Connected")
                        setupCommonEventListeners()
                        setupCustomEventListeners()
                        setupErrorEventListeners()
                        continuation.resume(Unit)
                    }
                }

                once(Socket.EVENT_CONNECT_ERROR) { args ->
                    if (!isResumed) {
                        isResumed = true
                        val error = args.firstOrNull()
                        Log.e(logTag, "Connection failed: $error")
                        off()
                        socket = null
                        continuation.resumeWithException(Exception("Connection failed: $error"))
                    }
                }

                connect()
            }

            continuation.invokeOnCancellation {
                socket?.disconnect()
                socket = null
            }
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        clearCallbacks()
        errorCallbacks.clear()
        customErrorCallbacks.clear()
        Log.i(logTag, "Disconnected")
    }

    protected fun emit(event: String, payload: JSONObject) {
        if (socket?.connected() != true) {
            Log.w(logTag, "Cannot emit '$event': not connected")
            return
        }
        socket?.emit(event, payload)
    }

    fun isConnected(): Boolean = socket?.connected() == true

    fun observeErrors(): Flow<SocketErrorDto> = callbackFlow {
        val callback: (SocketErrorDto) -> Unit = { error ->
            trySend(error)
        }
        errorCallbacks.add(callback)
        awaitClose { errorCallbacks.remove(callback) }
    }

    protected fun observeCustomError(eventName: String): Flow<String> = callbackFlow {
        val callback: (String) -> Unit = { errorMessage ->
            trySend(errorMessage)
        }
        customErrorCallbacks.getOrPut(eventName) { mutableListOf() }.add(callback)
        awaitClose {
            customErrorCallbacks[eventName]?.remove(callback)
        }
    }

    private fun setupCommonEventListeners() {
        socket?.apply {
            on(Socket.EVENT_CONNECT) {
                Log.i(logTag, "Socket connected")
            }

            on(Socket.EVENT_DISCONNECT) { args ->
                val reason = args.firstOrNull()?.toString() ?: "unknown"
                Log.w(logTag, "Disconnected: $reason")
            }

            on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e(logTag, "Connection error: ${args.firstOrNull()}")
            }

            on("error") { args ->
                args.firstOrNull()?.let { data ->
                    try {
                        val error = json.decodeFromString<SocketErrorDto>(data.toString())
                        errorCallbacks.forEach { it(error) }
                    } catch (e: Exception) {
                        Log.e(logTag, "Failed to parse error", e)
                    }
                }
            }
        }
    }

    private fun setupErrorEventListeners() {
        socket?.apply {
            getErrorEventNames().forEach { eventName ->
                on(eventName) { args ->
                    args.firstOrNull()?.let { data ->
                        val errorMessage = try {
                            val errorDto = json.decodeFromString<SocketErrorDto>(data.toString())
                            errorDto.error
                        } catch (e: Exception) {
                            data.toString()
                        }

                        Log.e(logTag, "Error event '$eventName': $errorMessage")
                        customErrorCallbacks[eventName]?.forEach { it(errorMessage) }
                    }
                }
            }
        }
    }

    protected abstract fun getErrorEventNames(): List<String>

    protected abstract fun setupCustomEventListeners()

    protected abstract fun clearCallbacks()
}
