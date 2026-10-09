package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.User
import com.example.ui.components.CuteAvatar
import com.example.ui.components.CuteEmptyState
import com.example.ui.theme.*
import com.example.ui.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatsTab(
    viewModel: ChatViewModel,
    onOpenChat: (User) -> Unit,
    onGoToFriends: () -> Unit
) {
    val friends by viewModel.friends.collectAsState()
    val lastMessages by viewModel.lastMessages.collectAsState()
    val unreadCounts by viewModel.unreadCounts.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredFriends = remember(friends, searchQuery) {
        if (searchQuery.isBlank()) friends
        else friends.filter {
            it.displayName.contains(searchQuery, ignoreCase = true) ||
                    it.username.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CuteBackground)
    ) {
        // Search Filter Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = "Search conversations…",
                    fontSize = 14.sp,
                    color = CuteTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = CuteLavenderPrimary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = CuteTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .testTag("conversations_search_input"),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = CuteTextPrimary,
                unfocusedTextColor = CuteTextPrimary,
                focusedPlaceholderColor = CuteTextMuted,
                unfocusedPlaceholderColor = CuteTextMuted,
                focusedBorderColor = CuteLavenderPrimary,
                unfocusedBorderColor = CuteBorder,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        if (friends.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                CuteEmptyState(
                    title = "No conversations yet",
                    subtitle = "Add friends or find people to start chatting freely!",
                    actionButtonText = "Find Friends",
                    onActionClick = onGoToFriends
                )
            }
        } else if (filteredFriends.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                CuteEmptyState(
                    title = "No matches found",
                    subtitle = "Try searching for a different friend name"
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredFriends, key = { it.id }) { friend ->
                    val lastMsg = lastMessages[friend.id]
                    val unreadCount = unreadCounts[friend.id] ?: 0

                    ConversationItem(
                        friend = friend,
                        lastMessage = lastMsg,
                        unreadCount = unreadCount,
                        onClick = { onOpenChat(friend) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationItem(
    friend: User,
    lastMessage: ChatMessage?,
    unreadCount: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("conversation_item_${friend.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CuteBorder.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CuteAvatar(user = friend, size = 52.dp, showOnlineBadge = true)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = friend.displayName.ifBlank { friend.username },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = CuteTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (lastMessage != null && lastMessage.createdAt > 0) {
                        Text(
                            text = formatTime(lastMessage.createdAt),
                            fontSize = 12.sp,
                            color = CuteTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val previewText = when {
                        lastMessage?.image != null -> "📷 Sent a photo"
                        lastMessage?.text?.isNotBlank() == true -> lastMessage.text
                        else -> "Say hello! 💜"
                    }

                    Text(
                        text = previewText,
                        fontSize = 13.sp,
                        color = if (unreadCount > 0) CuteLavenderPrimary else CuteTextSecondary,
                        fontWeight = if (unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(CutePinkHeart),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Calendar.getInstance()
    val msgCal = Calendar.getInstance().apply { time = date }

    return if (now.get(Calendar.DAY_OF_YEAR) == msgCal.get(Calendar.DAY_OF_YEAR) &&
        now.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR)
    ) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
    } else {
        SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
    }
}
