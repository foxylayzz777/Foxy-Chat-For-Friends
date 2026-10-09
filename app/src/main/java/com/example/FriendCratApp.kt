package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.data.api.ApiClient
import com.example.data.repository.ChatRepository
import com.example.data.session.SessionManager
import com.example.data.socket.SocketManager

class FriendCratApp : Application(), ImageLoaderFactory {

    lateinit var sessionManager: SessionManager
        private set
    lateinit var apiClient: ApiClient
        private set
    lateinit var socketManager: SocketManager
        private set
    lateinit var repository: ChatRepository
        private set

    companion object {
        lateinit var instance: FriendCratApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        sessionManager = SessionManager(this)
        apiClient = ApiClient(sessionManager)
        socketManager = SocketManager(sessionManager)
        repository = ChatRepository(this, apiClient, sessionManager, socketManager)

        // Connect socket if already logged in
        if (sessionManager.isLoggedIn()) {
            socketManager.connect()
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient(apiClient.okHttpClient)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024)
                    .build()
            }
            .crossfade(true)
            .build()
    }
}
