package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CuteAvatar
import com.example.ui.theme.*
import com.example.ui.viewmodel.ChatViewModel

@Composable
fun ProfileTab(
    viewModel: ChatViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val friends by viewModel.friends.collectAsState()
    val currentTheme by viewModel.appTheme.collectAsState()
    val themeColors = CuteTheme.colors
    val scrollState = rememberScrollState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAccountSettingsDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    // Photo picker for Avatar
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.uploadAvatar(uri) { _, _ -> }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, CuteBorder.copy(alpha = 0.6f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar with Camera Badge
                Box(contentAlignment = Alignment.BottomEnd) {
                    CuteAvatar(
                        user = currentUser,
                        size = 88.dp,
                        showOnlineBadge = true,
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(CuteLavenderPrimary)
                            .border(2.dp, Color.White, CircleShape)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = currentUser?.displayName?.ifBlank { currentUser?.username } ?: "Foxy Chat User",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = CuteTextPrimary
                )

                Text(
                    text = "@${currentUser?.username ?: ""}",
                    fontSize = 14.sp,
                    color = CuteTextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )

                if (!currentUser?.status.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "💭 ${currentUser?.status}",
                        fontSize = 13.sp,
                        color = CuteLavenderDark,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (!currentUser?.bio.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentUser?.bio ?: "",
                        fontSize = 13.sp,
                        color = CuteTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Stats Row (Friends, Rooms: 0, Posts: 0 - as in prompt mockup)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CuteLavenderLight)
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(count = friends.size.toString(), label = "Friends")
                    Divider(
                        modifier = Modifier
                            .height(28.dp)
                            .width(1.dp),
                        color = CuteBorder
                    )
                    StatItem(count = "0", label = "Rooms")
                    Divider(
                        modifier = Modifier
                            .height(28.dp)
                            .width(1.dp),
                        color = CuteBorder
                    )
                    StatItem(count = "0", label = "Posts")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Options List Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, CuteBorder.copy(alpha = 0.6f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                ProfileOptionRow(
                    icon = Icons.Default.Palette,
                    label = "App Theme (${currentTheme.emoji} ${currentTheme.displayName})",
                    iconTint = themeColors.primary,
                    onClick = { showThemeDialog = true }
                )
                Divider(color = themeColors.primaryLight, thickness = 1.dp)

                ProfileOptionRow(
                    icon = Icons.Default.Edit,
                    label = "Edit Profile",
                    iconTint = themeColors.primary,
                    onClick = { showEditDialog = true }
                )
                Divider(color = themeColors.primaryLight, thickness = 1.dp)

                ProfileOptionRow(
                    icon = Icons.Default.PhotoLibrary,
                    label = "Change Profile Picture",
                    iconTint = themeColors.primary,
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
                Divider(color = themeColors.primaryLight, thickness = 1.dp)

                ProfileOptionRow(
                    icon = Icons.Default.Settings,
                    label = "Account Settings",
                    iconTint = themeColors.primary,
                    onClick = { showAccountSettingsDialog = true }
                )
                Divider(color = themeColors.primaryLight, thickness = 1.dp)

                ProfileOptionRow(
                    icon = Icons.Default.DeleteOutline,
                    label = "Delete Account",
                    iconTint = CutePinkHeart,
                    textColor = CutePinkHeart,
                    onClick = { showDeleteConfirmDialog = true }
                )
                Divider(color = themeColors.primaryLight, thickness = 1.dp)

                ProfileOptionRow(
                    icon = Icons.Default.ExitToApp,
                    label = "Log Out",
                    iconTint = themeColors.primary,
                    textColor = themeColors.primary,
                    onClick = { showLogoutConfirmDialog = true }
                )
            }
        }
    }

    // App Theme Picker Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = {
                Text(
                    text = "Select App Theme ✨",
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Choose your aesthetic palette with crystal-clear readability:",
                        fontSize = 13.sp,
                        color = themeColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    AppTheme.values().forEach { theme ->
                        val isSelected = currentTheme == theme
                        Card(
                            onClick = {
                                viewModel.setAppTheme(theme)
                                showThemeDialog = false
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) theme.previewBg else Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) theme.previewColor else themeColors.border.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Color preview circle
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(theme.previewColor)
                                        .border(2.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = theme.emoji, fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${theme.displayName} ${theme.emoji}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = CuteTextPrimary
                                    )
                                    val desc = when (theme) {
                                        AppTheme.LILAC -> "Pastel violet & lavender aesthetic"
                                        AppTheme.SAKURA -> "Cute soft blush rose aesthetic"
                                        AppTheme.MATCHA -> "Fresh soothing mint sage aesthetic"
                                        AppTheme.OCEAN -> "Crisp sky azure & ocean aesthetic"
                                    }
                                    Text(
                                        text = desc,
                                        fontSize = 12.sp,
                                        color = CuteTextSecondary
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = theme.previewColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Close", color = themeColors.primary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Edit Profile Dialog
    if (showEditDialog) {
        var editName by remember { mutableStateOf(currentUser?.displayName ?: "") }
        var editBio by remember { mutableStateOf(currentUser?.bio ?: "") }
        var editStatus by remember { mutableStateOf(currentUser?.status ?: "") }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile 💜", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CuteTextPrimary,
                            unfocusedTextColor = CuteTextPrimary,
                            focusedBorderColor = CuteLavenderPrimary,
                            unfocusedBorderColor = CuteBorder
                        )
                    )
                    OutlinedTextField(
                        value = editStatus,
                        onValueChange = { editStatus = it },
                        label = { Text("Status / Mood") },
                        placeholder = { Text("e.g. Purring happily") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CuteTextPrimary,
                            unfocusedTextColor = CuteTextPrimary,
                            focusedBorderColor = CuteLavenderPrimary,
                            unfocusedBorderColor = CuteBorder
                        )
                    )
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio") },
                        maxLines = 3,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CuteTextPrimary,
                            unfocusedTextColor = CuteTextPrimary,
                            focusedBorderColor = CuteLavenderPrimary,
                            unfocusedBorderColor = CuteBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfile(editName, editBio, editStatus) { _, _ -> }
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CuteLavenderPrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Account Settings Dialog
    if (showAccountSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showAccountSettingsDialog = false },
            title = { Text("Account Settings ⚙️", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Username: @${currentUser?.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = CuteTextPrimary
                    )
                    Text(
                        text = "Display Name: ${currentUser?.displayName?.ifBlank { currentUser?.username }}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CuteTextSecondary
                    )
                    Text(
                        text = "Private 1-on-1 Chat Mode Active. Safe and private.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CuteLavenderDark
                    )
                    Text(
                        text = "Foxy Chat v1.0 • Cute & Private",
                        style = MaterialTheme.typography.bodySmall,
                        color = CuteTextMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAccountSettingsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CuteLavenderPrimary)
                ) {
                    Text("Done")
                }
            }
        )
    }

    // Delete Account Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Account?", fontWeight = FontWeight.Bold, color = CutePinkHeart) },
            text = {
                Text("Are you sure? This will permanently delete your account, friends, and conversations from the server.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteAccount { _, _ -> }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CutePinkHeart)
                ) {
                    Text("Permanently Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = { Text("Log out?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to log out of Foxy Chat?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CuteLavenderPrimary)
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = CuteTextPrimary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = CuteTextSecondary
        )
    }
}

@Composable
private fun ProfileOptionRow(
    icon: ImageVector,
    label: String,
    iconTint: Color = CuteLavenderPrimary,
    textColor: Color = CuteTextPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = CuteBorder,
            modifier = Modifier.size(20.dp)
        )
    }
}
