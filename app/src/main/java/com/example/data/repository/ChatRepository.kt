package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.api.ApiClient
import com.example.data.model.*
import com.example.data.session.SessionManager
import com.example.data.socket.SocketManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream

class ChatRepository(
    private val context: Context,
    private val apiClient: ApiClient,
    val sessionManager: SessionManager,
    val socketManager: SocketManager
) {
    private val api = apiClient.apiService

    suspend fun register(username: String, displayName: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = api.register(RegisterRequest(username.trim(), displayName.trim(), password))
            if (response.isSuccessful && response.body()?.user != null) {
                val user = response.body()!!.user!!
                sessionManager.currentUser = user
                socketManager.connect()
                Result.success(user)
            } else {
                val err = response.body()?.error ?: response.errorBody()?.string() ?: "Registration failed"
                Result.failure(Exception(cleanErrorMessage(err)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(username: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = api.login(LoginRequest(username.trim(), password))
            if (response.isSuccessful && response.body()?.user != null) {
                val user = response.body()!!.user!!
                sessionManager.currentUser = user
                socketManager.connect()
                Result.success(user)
            } else {
                val err = response.body()?.error ?: response.errorBody()?.string() ?: "Login failed"
                Result.failure(Exception(cleanErrorMessage(err)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchCurrentUser(): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMe()
            if (response.isSuccessful && response.body()?.user != null) {
                val user = response.body()!!.user!!
                sessionManager.currentUser = user
                socketManager.connect()
                Result.success(user)
            } else {
                val err = response.errorBody()?.string() ?: "Failed to fetch user"
                Result.failure(Exception(cleanErrorMessage(err)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            socketManager.disconnect()
            try {
                api.logout()
            } catch (_: Exception) {}
            sessionManager.clearSession()
            Result.success(Unit)
        } catch (e: Exception) {
            sessionManager.clearSession()
            Result.success(Unit)
        }
    }

    suspend fun deleteAccount(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteAccount()
            socketManager.disconnect()
            sessionManager.clearSession()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete account"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFriends(): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getFriends()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load friends"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFriendRequests(): Result<FriendRequestsResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getFriendRequests()
            if (response.isSuccessful) {
                Result.success(response.body() ?: FriendRequestsResponse())
            } else {
                Result.failure(Exception("Failed to load requests"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchUsers(query: String): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            val response = api.searchUsers(query)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Search failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendFriendRequest(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.sendFriendRequest(SendFriendRequestBody(userId))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to send friend request"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun respondFriendRequest(requestId: String, accept: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.respondFriendRequest(RespondFriendRequestBody(requestId, accept))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to respond to request"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMessages(friendId: String): Result<List<ChatMessage>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMessages(friendId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load messages"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(friendId: String, text: String): Result<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            val response = api.sendMessage(friendId, SendMessageRequest(text))
            if (response.isSuccessful && response.body()?.message != null) {
                Result.success(response.body()!!.message!!)
            } else {
                Result.failure(Exception("Failed to send message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendImageMessage(friendId: String, imageUri: Uri): Result<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                return@withContext Result.failure(Exception("Cannot read image"))
            }
            val bytes = inputStream.use { it.readBytes() }
            if (bytes.size > 8 * 1024 * 1024) {
                return@withContext Result.failure(Exception("Image must be smaller than 8MB"))
            }

            val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
            val requestFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("image", "photo.jpg", requestFile)

            val response = api.sendMediaMessage(friendId, part)
            if (response.isSuccessful && response.body()?.message != null) {
                Result.success(response.body()!!.message!!)
            } else {
                Result.failure(Exception("Image upload failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNotifications(): Result<List<NotificationItem>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getNotifications()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load notifications"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markNotificationsRead(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.markNotificationsRead()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to mark read"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(displayName: String, bio: String, status: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateProfile(UpdateProfileRequest(displayName, bio, status))
            if (response.isSuccessful && response.body()?.user != null) {
                val updated = response.body()!!.user!!
                sessionManager.currentUser = updated
                Result.success(updated)
            } else {
                Result.failure(Exception("Failed to update profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadAvatar(imageUri: Uri): Result<User> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: return@withContext Result.failure(Exception("Cannot read image"))
            val bytes = inputStream.use { it.readBytes() }
            val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
            val requestFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("image", "avatar.jpg", requestFile)

            val response = api.uploadAvatar(part)
            if (response.isSuccessful && response.body()?.user != null) {
                val updated = response.body()!!.user!!
                sessionManager.currentUser = updated
                Result.success(updated)
            } else {
                Result.failure(Exception("Avatar upload failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun cleanErrorMessage(raw: String): String {
        return if (raw.contains("\"error\":")) {
            val regex = "\"error\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            regex.find(raw)?.groups?.get(1)?.value ?: raw
        } else {
            raw
        }
    }
}
