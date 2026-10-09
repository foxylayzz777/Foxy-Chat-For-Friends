package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.components.CuteAvatar
import com.example.ui.components.CuteEmptyState
import com.example.ui.theme.*
import com.example.ui.viewmodel.ChatViewModel

@Composable
fun SearchTab(
    viewModel: ChatViewModel,
    onOpenChat: (User) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CuteBackground)
            .padding(16.dp)
    ) {
        // Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                if (it.length >= 2) {
                    viewModel.searchUsers(it)
                }
            },
            placeholder = { Text("Search by username or name…", fontSize = 14.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = CuteLavenderPrimary)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        searchQuery = ""
                        viewModel.searchUsers("")
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = CuteTextSecondary)
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_users_input"),
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                focusManager.clearFocus()
                viewModel.searchUsers(searchQuery)
            }),
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

        Spacer(modifier = Modifier.height(16.dp))

        if (isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CuteLavenderPrimary)
            }
        } else if (searchQuery.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CuteEmptyState(
                    emoji = "🐾💜",
                    title = "Find Your People",
                    subtitle = "Type a username or display name to connect with friends."
                )
            }
        } else if (searchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CuteEmptyState(
                    emoji = "😿",
                    title = "No people found",
                    subtitle = "We couldn't find anyone matching \"$searchQuery\""
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(searchResults, key = { it.id }) { user ->
                    SearchResultRow(
                        user = user,
                        onAddFriend = { viewModel.sendFriendRequest(user.id) },
                        onOpenChat = { onOpenChat(user) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    user: User,
    onAddFriend: () -> Unit,
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
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CuteAvatar(user = user, size = 48.dp, showOnlineBadge = true)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.displayName.ifBlank { user.username },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = CuteTextPrimary
                )
                Text(
                    text = "@${user.username} · ${if (user.online == true) "🟢 Online" else "Offline"}",
                    fontSize = 12.sp,
                    color = CuteTextSecondary
                )
            }

            when {
                user.friend == true -> {
                    Button(
                        onClick = onOpenChat,
                        colors = ButtonDefaults.buttonColors(containerColor = CuteLavenderLight),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Friends", color = CuteLavenderPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                user.incoming == true -> {
                    Text(
                        text = "Sent request",
                        fontSize = 12.sp,
                        color = CutePinkHeart,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                user.pending == true -> {
                    Text(
                        text = "Requested 💌",
                        fontSize = 12.sp,
                        color = CuteLavenderPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                else -> {
                    Button(
                        onClick = onAddFriend,
                        colors = ButtonDefaults.buttonColors(containerColor = CuteLavenderPrimary),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_friend_button_${user.id}")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add friend", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
