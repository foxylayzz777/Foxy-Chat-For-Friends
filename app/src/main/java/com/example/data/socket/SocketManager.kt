package com.example.data.socket

import android.util.Log
import com.example.data.model.ChatMessage
import com.example.data.session.SessionManager
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URI

data class TypingEvent(val from: String, val active: Boolean)

class SocketManager(private val sessionManager: SessionManager) {

    private val tag = "SocketManager"
    private var socket: Socket? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _newMessages = MutableSharedFlow<ChatMessage>(extraBufferCapacity = 64)
    val newMessages: SharedFlow<ChatMessage> = _newMessages.asSharedFlow()

    private val _typingEvents = MutableSharedFlow<TypingEvent>(extraBufferCapacity = 64)
    val typingEvents: SharedFlow<TypingEvent> = _typingEvents.asSharedFlow()

    private val _notifications = MutableSharedFlow<Unit>(extraBufferCapacity = 16)
    val notifications: SharedFlow<Unit> = _notifications.asSharedFlow()

    @Synchronized
    fun connect() {
        val token = sessionManager.sessionToken
        if (token.isNullOrBlank()) {
            Log.d(tag, "Cannot connect to socket without session token")
            return
        }

        try {
            disconnect()

            val opts = IO.Options().apply {
                forceNew = true
                reconnection = true
                extraHeaders = mapOf("Cookie" to listOf("foxy_session=$token"))
            }

            val newSocket = IO.socket(URI.create(SessionManager.SERVER_URL), opts)

            newSocket.on(Socket.EVENT_CONNECT) {
                Log.d(tag, "Socket connected successfully")
                _isConnected.value = true
            }

            newSocket.on(Socket.EVENT_DISCONNECT) {
                Log.d(tag, "Socket disconnected")
                _isConnected.value = false
            }

            newSocket.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.w(tag, "Socket connect error: ${args.firstOrNull()}")
                _isConnected.value = false
            }

            newSocket.on("dm:new") { args ->
                try {
                    val obj = args.firstOrNull() as? JSONObject ?: return@on
                    val message = ChatMessage(
                        id = obj.optString("id"),
                        from = obj.optString("from"),
                        to = obj.optString("to"),
                        text = obj.optString("text"),
                        image = if (obj.has("image") && !obj.isNull("image")) obj.optString("image") else null,
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                    scope.launch {
                        _newMessages.emit(message)
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Failed to parse dm:new payload", e)
                }
            }

            newSocket.on("dm:typing") { args ->
                try {
                    val obj = args.firstOrNull() as? JSONObject ?: return@on
                    val from = obj.optString("from")
                    val active = obj.optBoolean("active", false)
                    scope.launch {
                        _typingEvents.emit(TypingEvent(from = from, active = active))
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Failed to parse dm:typing payload", e)
                }
            }

            newSocket.on("notification") {
                scope.launch {
                    _notifications.emit(Unit)
                }
            }

            newSocket.connect()
            this.socket = newSocket

        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize Socket.IO client", e)
        }
    }

    fun emitTyping(toUserId: String, active: Boolean) {
        try {
            socket?.let { s ->
                if (s.connected()) {
                    val payload = JSONObject().apply {
                        put("to", toUserId)
                        put("active", active)
                    }
                    s.emit("dm:typing", payload)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to emit typing event", e)
        }
    }

    @Synchronized
    fun disconnect() {
        try {
            socket?.disconnect()
            socket?.off()
            socket = null
            _isConnected.value = false
        } catch (e: Exception) {
            Log.e(tag, "Error during socket disconnect", e)
        }
    }
}
