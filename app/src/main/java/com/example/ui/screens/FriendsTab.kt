package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FriendRequest
import com.example.data.model.User
import com.example.ui.components.CuteAvatar
import com.example.ui.components.CuteEmptyState
import com.example.ui.theme.*
import com.example.ui.viewmodel.ChatViewModel

@Composable
fun FriendsTab(
    viewModel: ChatViewModel,
    onOpenChat: (User) -> Unit,
    onGoToSearch: () -> Unit
) {
    val friends by viewModel.friends.collectAsState()
    val friendRequests by viewModel.friendRequests.collectAsState()

    val onlineFriends = remember(friends) { friends.filter { it.online == true } }
    val offlineFriends = remember(friends) { friends.filter { it.online != true } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CuteBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Incoming Friend Requests Section
        if (friendRequests.incoming.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = CuteLavenderContainer),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CuteBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Friend Requests 💌",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = CuteTextPrimary
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Badge(containerColor = CuteLavenderPrimary) {
                                Text(
                                    text = friendRequests.incoming.size.toString(),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        friendRequests.incoming.forEach { req ->
                            FriendRequestRow(
                                request = req,
                                onAccept = { viewModel.respondFriendRequest(req.id, true) },
                                onDecline = { viewModel.respondFriendRequest(req.id, false) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // Friends Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Your Friends (${friends.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = CuteTextPrimary
                )

                IconButton(
                    onClick = { viewModel.loadFriends() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh friends",
                        tint = CuteLavenderPrimary
                    )
                }
            }
        }

        if (friends.isEmpty()) {
            item {
                CuteEmptyState(
                    emoji = "🌷✨",
                    title = "Your friend list starts here",
                    subtitle = "Search for friends by username and send a request!",
                    actionButtonText = "Find People",
                    onActionClick = onGoToSearch
                )
            }
        } else {
            // Online Friends
            if (onlineFriends.isNotEmpty()) {
                item {
                    Text(
                        text = "🟢 Online (${onlineFriends.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CuteOnlineGreen,
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                    )
                }
                items(onlineFriends, key = { it.id }) { friend ->
                    FriendRow(friend = friend, onOpenChat = { onOpenChat(friend) })
                }
            }

            // Offline Friends
            if (offlineFriends.isNotEmpty()) {
                item {
                    Text(
                        text = "Offline (${offlineFriends.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CuteTextSecondary,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 2.dp)
                    )
                }
                items(offlineFriends, key = { it.id }) { friend ->
                    FriendRow(friend = friend, onOpenChat = { onOpenChat(friend) })
                }
            }
        }
    }
}

@Composable
private fun FriendRequestRow(
    request: FriendRequest,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CuteAvatar(user = request.user, size = 44.dp, showOnlineBadge = false)
            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.user.displayName.ifBlank { request.user.username },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = CuteTextPrimary
                )
                Text(
                    text = "@${request.user.username}",
                    fontSize = 12.sp,
                    color = CuteTextSecondary
                )
            }

            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = CuteLavenderPrimary),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("accept_friend_request_${request.id}")
            ) {
                Text("Accept", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(6.dp))

            OutlinedButton(
                onClick = onDecline,
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CuteTextSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, CuteBorder),
                modifier = Modifier.testTag("decline_friend_request_${request.id}")
            ) {
                Text("Decline", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun FriendRow(
    friend: User,
    onOpenChat: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CuteBorder.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CuteAvatar(user = friend, size = 48.dp, showOnlineBadge = true)
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.displayName.ifBlank { friend.username },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = CuteTextPrimary
                )
                Text(
                    text = if (friend.online == true) "🟢 Online" else "@${friend.username}",
                    fontSize = 12.sp,
                    color = if (friend.online == true) CuteOnlineGreen else CuteTextSecondary
                )
            }

            Button(
                onClick = onOpenChat,
                colors = ButtonDefaults.buttonColors(containerColor = CuteLavenderLight),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("message_friend_button_${friend.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = null,
                    tint = CuteLavenderPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Message",
                    color = CuteLavenderPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
