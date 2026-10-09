package com.example.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.FriendCratApp
import com.example.data.model.*
import com.example.data.repository.ChatRepository
import com.example.ui.theme.AppTheme
import com.example.util.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository = FriendCratApp.instance.repository
) : ViewModel() {

    private val tag = "ChatViewModel"

    private val _currentUser = MutableStateFlow<User?>(repository.sessionManager.currentUser)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(repository.sessionManager.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    private val _friends = MutableStateFlow<List<User>>(emptyList())
    val friends: StateFlow<List<User>> = _friends.asStateFlow()

    private val _friendRequests = MutableStateFlow(FriendRequestsResponse())
    val friendRequests: StateFlow<FriendRequestsResponse> = _friendRequests.asStateFlow()

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    val unreadNotificationsCount: StateFlow<Int> = _notifications.map { list ->
        list.count { !it.read }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Active 1-to-1 Chat State
    private val _activeFriend = MutableStateFlow<User?>(null)
    val activeFriend: StateFlow<User?> = _activeFriend.asStateFlow()

    private val _activeMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val activeMessages: StateFlow<List<ChatMessage>> = _activeMessages.asStateFlow()

    private val _isFriendTyping = MutableStateFlow(false)
    val isFriendTyping: StateFlow<Boolean> = _isFriendTyping.asStateFlow()

    private val _isSendingMessage = MutableStateFlow(false)
    val isSendingMessage: StateFlow<Boolean> = _isSendingMessage.asStateFlow()

    // Map of friendId -> last message
    private val _lastMessages = MutableStateFlow<Map<String, ChatMessage>>(emptyMap())
    val lastMessages: StateFlow<Map<String, ChatMessage>> = _lastMessages.asStateFlow()

    // Map of friendId -> unread count
    private val _unreadCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val unreadCounts: StateFlow<Map<String, Int>> = _unreadCounts.asStateFlow()

    val totalUnreadMessagesCount: StateFlow<Int> = _unreadCounts.map { map ->
        map.values.sum()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _appTheme = MutableStateFlow(AppTheme.fromId(repository.sessionManager.appTheme))
    val appTheme: StateFlow<AppTheme> = _appTheme.asStateFlow()

    fun setAppTheme(theme: AppTheme) {
        repository.sessionManager.appTheme = theme.id
        _appTheme.value = theme
    }

    private var typingTimeoutJob: Job? = null

    init {
        observeSocket()
        if (_isLoggedIn.value) {
            refreshInitialData()
        }
    }

    private fun observeSocket() {
        viewModelScope.launch {
            repository.socketManager.newMessages.collect { message ->
                val currentMeId = _currentUser.value?.id ?: ""
                val currentFriend = _activeFriend.value

                // If message belongs to active chat, add it
                if (currentFriend != null &&
                    (message.from == currentFriend.id || message.to == currentFriend.id)
                ) {
                    val currentList = _activeMessages.value
                    if (currentList.none { it.id == message.id }) {
                        _activeMessages.value = currentList + message
                    }
                } else if (message.from != currentMeId) {
                    // Increment unread count for message sender
                    val senderId = message.from
                    val curr = _unreadCounts.value[senderId] ?: 0
                    _unreadCounts.value = _unreadCounts.value + (senderId to (curr + 1))
                }

                // Show notification if message is incoming from another user and not currently active
                if (message.from != currentMeId && (currentFriend == null || currentFriend.id != message.from)) {
                    val senderFriend = _friends.value.firstOrNull { it.id == message.from }
                    val senderName = senderFriend?.displayName?.ifBlank { senderFriend.username } ?: "Foxy Chat Friend"
                    NotificationHelper.showIncomingMessageNotification(
                        context = FriendCratApp.instance,
                        senderId = message.from,
                        senderName = senderName,
                        messageText = message.text,
                        isPhoto = !message.image.isNullOrBlank()
                    )
                }

                // Update last message preview
                val otherId = if (message.from == currentMeId) message.to else message.from
                _lastMessages.value = _lastMessages.value + (otherId to message)
            }
        }

        viewModelScope.launch {
            repository.socketManager.typingEvents.collect { event ->
                val currentFriend = _activeFriend.value
                if (currentFriend != null && event.from == currentFriend.id) {
                    _isFriendTyping.value = event.active
                    typingTimeoutJob?.cancel()
                    if (event.active) {
                        typingTimeoutJob = viewModelScope.launch {
                            delay(4000)
                            _isFriendTyping.value = false
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            repository.socketManager.notifications.collect {
                loadNotifications()
                loadFriends()
                _toastMessage.emit("New update received ✨")
            }
        }
    }

    fun refreshInitialData() {
        viewModelScope.launch {
            repository.fetchCurrentUser().onSuccess { user ->
                _currentUser.value = user
            }
            loadFriends()
            loadNotifications()
        }
    }

    fun login(username: String, password: String, onResult: (Boolean, String?) -> Unit) {
        if (username.isBlank() || password.isBlank()) {
            onResult(false, "Please enter both username and password")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.login(username, password)
            _isLoading.value = false
            result.fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    _isLoggedIn.value = true
                    refreshInitialData()
                    onResult(true, null)
                },
                onFailure = { err ->
                    onResult(false, err.message ?: "Login failed")
                }
            )
        }
    }

    fun register(username: String, displayName: String, password: String, onResult: (Boolean, String?) -> Unit) {
        if (username.length < 3) {
            onResult(false, "Username must be at least 3 characters")
            return
        }
        if (password.length < 6) {
            onResult(false, "Password must be at least 6 characters")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.register(username, displayName.ifBlank { username }, password)
            _isLoading.value = false
            result.fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    _isLoggedIn.value = true
                    refreshInitialData()
                    onResult(true, null)
                },
                onFailure = { err ->
                    onResult(false, err.message ?: "Registration failed")
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.logout()
            _isLoading.value = false
            _currentUser.value = null
            _isLoggedIn.value = false
            _friends.value = emptyList()
            _activeFriend.value = null
            _activeMessages.value = emptyList()
        }
    }

    fun deleteAccount(onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.deleteAccount()
            _isLoading.value = false
            res.fold(
                onSuccess = {
                    _currentUser.value = null
                    _isLoggedIn.value = false
                    _friends.value = emptyList()
                    _activeFriend.value = null
                    onComplete(true, null)
                },
                onFailure = { err ->
                    onComplete(false, err.message ?: "Failed to delete account")
                }
            )
        }
    }

    fun loadFriends() {
        viewModelScope.launch {
            repository.getFriends().onSuccess { list ->
                _friends.value = list
                // Fetch recent messages for friends if not already loaded
                list.forEach { friend ->
                    if (!_lastMessages.value.containsKey(friend.id)) {
                        launch {
                            repository.getMessages(friend.id).onSuccess { msgs ->
                                val last = msgs.lastOrNull()
                                if (last != null) {
                                    _lastMessages.value = _lastMessages.value + (friend.id to last)
                                }
                            }
                        }
                    }
                }
            }
            repository.getFriendRequests().onSuccess { reqs ->
                _friendRequests.value = reqs
            }
        }
    }

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            repository.searchUsers(query).fold(
                onSuccess = { results ->
                    _searchResults.value = results
                    _isSearching.value = false
                },
                onFailure = {
                    _isSearching.value = false
                }
            )
        }
    }

    fun sendFriendRequest(userId: String) {
        viewModelScope.launch {
            repository.sendFriendRequest(userId).fold(
                onSuccess = {
                    _toastMessage.emit("Friend request sent 💌")
                    // Update search item status locally
                    _searchResults.value = _searchResults.value.map {
                        if (it.id == userId) it.copy(pending = true) else it
                    }
                },
                onFailure = { err ->
                    _toastMessage.emit(err.message ?: "Failed to send request")
                }
            )
        }
    }

    fun respondFriendRequest(requestId: String, accept: Boolean) {
        viewModelScope.launch {
            repository.respondFriendRequest(requestId, accept).fold(
                onSuccess = {
                    _toastMessage.emit(if (accept) "You're friends now 💜" else "Request declined")
                    loadFriends()
                },
                onFailure = { err ->
                    _toastMessage.emit(err.message ?: "Failed to respond")
                }
            )
        }
    }

    fun openChat(friend: User) {
        _activeFriend.value = friend
        _isFriendTyping.value = false
        // Clear unread count for this friend
        _unreadCounts.value = _unreadCounts.value - friend.id

        viewModelScope.launch {
            _isLoading.value = true
            repository.getMessages(friend.id).fold(
                onSuccess = { msgs ->
                    _activeMessages.value = msgs
                    _isLoading.value = false
                    msgs.lastOrNull()?.let { last ->
                        _lastMessages.value = _lastMessages.value + (friend.id to last)
                    }
                },
                onFailure = { err ->
                    _isLoading.value = false
                    _toastMessage.emit(err.message ?: "Failed to load chat")
                }
            )
        }
    }

    fun closeChat() {
        val current = _activeFriend.value
        if (current != null) {
            repository.socketManager.emitTyping(current.id, false)
        }
        _activeFriend.value = null
        _activeMessages.value = emptyList()
        _isFriendTyping.value = false
    }

    fun sendMessage(text: String) {
        val friend = _activeFriend.value ?: return
        val trimmed = text.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            repository.socketManager.emitTyping(friend.id, false)
            _isSendingMessage.value = true
            repository.sendMessage(friend.id, trimmed).fold(
                onSuccess = { msg ->
                    _isSendingMessage.value = false
                    if (_activeMessages.value.none { it.id == msg.id }) {
                        _activeMessages.value = _activeMessages.value + msg
                    }
                    _lastMessages.value = _lastMessages.value + (friend.id to msg)
                },
                onFailure = { err ->
                    _isSendingMessage.value = false
                    _toastMessage.emit(err.message ?: "Message could not be sent")
                }
            )
        }
    }

    fun sendImageMessage(uri: Uri) {
        val friend = _activeFriend.value ?: return
        viewModelScope.launch {
            _isSendingMessage.value = true
            _toastMessage.emit("Sending photo… 📷")
            repository.sendImageMessage(friend.id, uri).fold(
                onSuccess = { msg ->
                    _isSendingMessage.value = false
                    if (_activeMessages.value.none { it.id == msg.id }) {
                        _activeMessages.value = _activeMessages.value + msg
                    }
                    _lastMessages.value = _lastMessages.value + (friend.id to msg)
                    _toastMessage.emit("Photo sent 💜")
                },
                onFailure = { err ->
                    _isSendingMessage.value = false
                    _toastMessage.emit(err.message ?: "Photo upload failed")
                }
            )
        }
    }

    fun onTypingChanged(text: String) {
        val friend = _activeFriend.value ?: return
        repository.socketManager.emitTyping(friend.id, text.isNotBlank())
    }

    fun loadNotifications() {
        viewModelScope.launch {
            repository.getNotifications().onSuccess { list ->
                _notifications.value = list
            }
        }
    }

    fun markNotificationsRead() {
        viewModelScope.launch {
            repository.markNotificationsRead().onSuccess {
                _notifications.value = _notifications.value.map { it.copy(read = true) }
            }
        }
    }

    fun updateProfile(displayName: String, bio: String, status: String, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateProfile(displayName, bio, status).fold(
                onSuccess = { updated ->
                    _isLoading.value = false
                    _currentUser.value = updated
                    _toastMessage.emit("Profile saved 💜")
                    onComplete(true, null)
                },
                onFailure = { err ->
                    _isLoading.value = false
                    onComplete(false, err.message ?: "Failed to update profile")
                }
            )
        }
    }

    fun uploadAvatar(uri: Uri, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _toastMessage.emit("Uploading profile picture…")
            repository.uploadAvatar(uri).fold(
                onSuccess = { updated ->
                    _isLoading.value = false
                    _currentUser.value = updated
                    _toastMessage.emit("Profile picture updated 💜")
                    onComplete(true, null)
                },
                onFailure = { err ->
                    _isLoading.value = false
                    onComplete(false, err.message ?: "Avatar upload failed")
                }
            )
        }
    }

    fun openChatByUserId(userId: String) {
        val friend = _friends.value.firstOrNull { it.id == userId }
        if (friend != null) {
            openChat(friend)
        } else {
            viewModelScope.launch {
                loadFriends()
                _friends.value.firstOrNull { it.id == userId }?.let { openChat(it) }
            }
        }
    }
}
