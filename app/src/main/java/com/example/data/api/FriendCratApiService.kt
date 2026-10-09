package com.example.data.api

import com.example.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface FriendCratApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<UserResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<UserResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("api/me")
    suspend fun getMe(): Response<UserResponse>

    @DELETE("api/account")
    suspend fun deleteAccount(): Response<Unit>

    @GET("api/friends")
    suspend fun getFriends(): Response<List<User>>

    @GET("api/friends/requests")
    suspend fun getFriendRequests(): Response<FriendRequestsResponse>

    @GET("api/users/search")
    suspend fun searchUsers(@Query("q") query: String): Response<List<User>>

    @POST("api/friends/request")
    suspend fun sendFriendRequest(@Body body: SendFriendRequestBody): Response<Unit>

    @POST("api/friends/respond")
    suspend fun respondFriendRequest(@Body body: RespondFriendRequestBody): Response<Unit>

    @GET("api/messages/{friendId}")
    suspend fun getMessages(@Path("friendId") friendId: String): Response<List<ChatMessage>>

    @POST("api/messages/{friendId}")
    suspend fun sendMessage(
        @Path("friendId") friendId: String,
        @Body body: SendMessageRequest
    ): Response<MessageResponse>

    @Multipart
    @POST("api/messages/{friendId}/media")
    suspend fun sendMediaMessage(
        @Path("friendId") friendId: String,
        @Part image: MultipartBody.Part
    ): Response<MessageResponse>

    @GET("api/notifications")
    suspend fun getNotifications(): Response<List<NotificationItem>>

    @POST("api/notifications/read")
    suspend fun markNotificationsRead(@Body empty: Map<String, String> = emptyMap()): Response<Unit>

    @PATCH("api/profile")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): Response<UserResponse>

    @Multipart
    @POST("api/avatar")
    suspend fun uploadAvatar(@Part image: MultipartBody.Part): Response<UserResponse>
}
