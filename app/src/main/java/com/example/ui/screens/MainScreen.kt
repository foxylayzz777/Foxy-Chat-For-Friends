package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.NotificationItem
import com.example.ui.components.CuteAvatar
import com.example.ui.theme.*
import com.example.ui.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class BottomTab {
    CHATS, FRIENDS, SEARCH, PROFILE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ChatViewModel
) {
    var selectedTab by remember { mutableStateOf(BottomTab.CHATS) }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    val currentUser by viewModel.currentUser.collectAsState()
    val activeFriend by viewModel.activeFriend.collectAsState()
    val unreadNotifications by viewModel.unreadNotificationsCount.collectAsState()
    val totalUnreadMessages by viewModel.totalUnreadMessagesCount.collectAsState()
    val friendRequests by viewModel.friendRequests.collectAsState()
    val currentTheme by viewModel.appTheme.collectAsState()
    val themeColors = CuteTheme.colors

    // If an active friend chat is open, show the full chat screen
    if (activeFriend != null) {
        ChatDetailScreen(
            friend = activeFriend!!,
            viewModel = viewModel,
            onBack = { viewModel.closeChat() }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = themeColors.background,
        topBar = {
            TopAppBar(
                modifier = Modifier.drawBehind {
                    drawLine(
                        color = themeColors.border.copy(alpha = 0.5f),
                        start = androidx.compose.ui.geometry.Offset(0f, size.height),
                        end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Profile Icon / App Logo
                        val avatarUrl = currentUser?.fullAvatarUrl()
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(themeColors.primaryContainer)
                                .border(1.5.dp, themeColors.border, CircleShape)
                                .clickable { selectedTab = BottomTab.PROFILE },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!avatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(avatarUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Profile",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_app_icon),
                                    contentDescription = "Logo",
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Foxy Chat",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = themeColors.textPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = currentTheme.emoji, fontSize = 16.sp)
                    }
                },
                actions = {
                    // Notification Icon with Badge
                    IconButton(
                        onClick = {
                            viewModel.loadNotifications()
                            showNotificationsDialog = true
                        },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .testTag("notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifications > 0) {
                                    Badge(containerColor = CutePinkHeart) {
                                        Text(
                                            text = if (unreadNotifications > 99) "99+" else unreadNotifications.toString(),
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (unreadNotifications > 0) Icons.Default.Notifications else Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = themeColors.textPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = themeColors.textPrimary,
                    actionIconContentColor = themeColors.textPrimary
                ),
                windowInsets = TopAppBarDefaults.windowInsets
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp,
                windowInsets = NavigationBarDefaults.windowInsets,
                modifier = Modifier
                    .testTag("bottom_nav_bar")
                    .drawBehind {
                        drawLine(
                            color = themeColors.border.copy(alpha = 0.6f),
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
            ) {
                val navItemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = themeColors.primary,
                    selectedTextColor = themeColors.primary,
                    indicatorColor = themeColors.primaryContainer,
                    unselectedIconColor = Color(0xFF6B7280),
                    unselectedTextColor = Color(0xFF4B5563)
                )

                // Chats Tab
                NavigationBarItem(
                    selected = selectedTab == BottomTab.CHATS,
                    onClick = { selectedTab = BottomTab.CHATS },
                    alwaysShowLabel = true,
                    icon = {
                        BadgedBox(
                            badge = {
                                if (totalUnreadMessages > 0) {
                                    Badge(containerColor = CutePinkHeart) {
                                        Text(
                                            text = if (totalUnreadMessages > 99) "99+" else totalUnreadMessages.toString(),
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (selectedTab == BottomTab.CHATS) Icons.Default.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                                contentDescription = "Chats"
                            )
                        }
                    },
                    label = {
                        Text(
                            text = "Chats",
                            fontWeight = if (selectedTab == BottomTab.CHATS) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = navItemColors,
                    modifier = Modifier.testTag("nav_tab_chats")
                )

                // Friends Tab
                NavigationBarItem(
                    selected = selectedTab == BottomTab.FRIENDS,
                    onClick = { selectedTab = BottomTab.FRIENDS },
                    alwaysShowLabel = true,
                    icon = {
                        BadgedBox(
                            badge = {
                                val incomingCount = friendRequests.incoming.size
                                if (incomingCount > 0) {
                                    Badge(containerColor = themeColors.primary) {
                                        Text(
                                            text = if (incomingCount > 99) "99+" else incomingCount.toString(),
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (selectedTab == BottomTab.FRIENDS) Icons.Default.People else Icons.Outlined.PeopleOutline,
                                contentDescription = "Friends"
                            )
                        }
                    },
                    label = {
                        Text(
                            text = "Friends",
                            fontWeight = if (selectedTab == BottomTab.FRIENDS) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = navItemColors,
                    modifier = Modifier.testTag("nav_tab_friends")
                )

                // Search Tab
                NavigationBarItem(
                    selected = selectedTab == BottomTab.SEARCH,
                    onClick = { selectedTab = BottomTab.SEARCH },
                    alwaysShowLabel = true,
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == BottomTab.SEARCH) Icons.Default.Search else Icons.Outlined.Search,
                            contentDescription = "Search"
                        )
                    },
                    label = {
                        Text(
                            text = "Search",
                            fontWeight = if (selectedTab == BottomTab.SEARCH) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = navItemColors,
                    modifier = Modifier.testTag("nav_tab_search")
                )

                // Profile Tab
                NavigationBarItem(
                    selected = selectedTab == BottomTab.PROFILE,
                    onClick = { selectedTab = BottomTab.PROFILE },
                    alwaysShowLabel = true,
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == BottomTab.PROFILE) Icons.Default.Person else Icons.Outlined.PersonOutline,
                            contentDescription = "Profile"
                        )
                    },
                    label = {
                        Text(
                            text = "Profile",
                            fontWeight = if (selectedTab == BottomTab.PROFILE) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = navItemColors,
                    modifier = Modifier.testTag("nav_tab_profile")
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(themeColors.background)
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                BottomTab.CHATS -> ChatsTab(
                    viewModel = viewModel,
                    onOpenChat = { friend -> viewModel.openChat(friend) },
                    onGoToFriends = { selectedTab = BottomTab.FRIENDS }
                )
                BottomTab.FRIENDS -> FriendsTab(
                    viewModel = viewModel,
                    onOpenChat = { friend -> viewModel.openChat(friend) },
                    onGoToSearch = { selectedTab = BottomTab.SEARCH }
                )
                BottomTab.SEARCH -> SearchTab(
                    viewModel = viewModel,
                    onOpenChat = { friend -> viewModel.openChat(friend) }
                )
                BottomTab.PROFILE -> ProfileTab(viewModel = viewModel)
            }
        }
    }

    // Notifications Dialog
    if (showNotificationsDialog) {
        val notifications by viewModel.notifications.collectAsState()

        AlertDialog(
            onDismissRequest = {
                viewModel.markNotificationsRead()
                showNotificationsDialog = false
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Notifications ✨", fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
                    TextButton(onClick = { viewModel.markNotificationsRead() }) {
                        Text("Mark read", color = themeColors.primary, fontSize = 13.sp)
                    }
                }
            },
            text = {
                if (notifications.isEmpty()) {
                    Text("You're all caught up! 🦋", color = themeColors.textSecondary)
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 350.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications, key = { it.id }) { item ->
                            NotificationRow(item = item)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.markNotificationsRead()
                        showNotificationsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary)
                ) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun NotificationRow(item: NotificationItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!item.read) CuteLavenderLight else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CuteAvatar(user = item.fromUser, size = 38.dp, showOnlineBadge = false)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.fromUser?.displayName ?: "Foxy Chat",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = CuteTextPrimary
                )
                val description = when (item.type) {
                    "request" -> "sent you a friend request 💌"
                    "friend" -> "is now your friend! 💜"
                    else -> "sent you a notification ✨"
                }
                Text(text = description, fontSize = 12.sp, color = CuteTextSecondary)
            }
            if (!item.read) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(CutePinkHeart)
                )
            }
        }
    }
}
