package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    val id: String,
    val username: String,
    val displayName: String = "",
    val bio: String? = "",
    val avatar: String? = "",
    val status: String? = "",
    val online: Boolean? = false,
    val createdAt: Long? = 0L,
    val friend: Boolean? = false,
    val incoming: Boolean? = false,
    val pending: Boolean? = false
) {
    fun fullAvatarUrl(baseUrl: String = "http://nile.hidencloud.com:24697"): String? {
        if (avatar.isNullOrBlank()) return null
        return if (avatar.startsWith("http://") || avatar.startsWith("https://")) {
            avatar
        } else {
            val path = if (avatar.startsWith("/")) avatar else "/$avatar"
            "$baseUrl$path"
        }
    }
}

@JsonClass(generateAdapter = true)
data class UserResponse(
    val user: User? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val username: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val username: String,
    val displayName: String = "",
    val password: String
)

@JsonClass(generateAdapter = true)
data class FriendRequest(
    val id: String,
    val user: User
)

@JsonClass(generateAdapter = true)
data class FriendRequestsResponse(
    val incoming: List<FriendRequest> = emptyList(),
    val outgoing: List<FriendRequest> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SendFriendRequestBody(
    val userId: String
)

@JsonClass(generateAdapter = true)
data class RespondFriendRequestBody(
    val requestId: String,
    val accept: Boolean
)

@JsonClass(generateAdapter = true)
data class ChatMessage(
    val id: String,
    val from: String,
    val to: String,
    val text: String? = "",
    val image: String? = null,
    val createdAt: Long = 0L
) {
    fun fullImageUrl(baseUrl: String = "http://nile.hidencloud.com:24697"): String? {
        if (image.isNullOrBlank()) return null
        return if (image.startsWith("http://") || image.startsWith("https://")) {
            image
        } else {
            val path = if (image.startsWith("/")) image else "/$image"
            "$baseUrl$path"
        }
    }
}

@JsonClass(generateAdapter = true)
data class MessageResponse(
    val message: ChatMessage? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class SendMessageRequest(
    val text: String
)

@JsonClass(generateAdapter = true)
data class UpdateProfileRequest(
    val displayName: String? = null,
    val bio: String? = null,
    val status: String? = null
)

@JsonClass(generateAdapter = true)
data class NotificationItem(
    val id: String,
    val type: String = "notification", // request, friend, etc.
    val fromUser: User? = null,
    val read: Boolean = false,
    val createdAt: Long = 0L
)

@JsonClass(generateAdapter = true)
data class SimpleSuccessResponse(
    val success: Boolean? = true,
    val message: String? = null,
    val error: String? = null
)
