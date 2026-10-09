package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.compose.material.icons.filled.PhotoCamera
import java.io.File
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.FriendCratApp
import com.example.data.model.ChatMessage
import com.example.data.model.User
import com.example.ui.components.CuteAvatar
import com.example.ui.theme.*
import com.example.ui.viewmodel.ChatViewModel
import com.example.util.ImageDownloader
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    friend: User,
    viewModel: ChatViewModel,
    onBack: () -> Unit
) {
    BackHandler {
        viewModel.closeChat()
        onBack()
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val okHttpClient = remember { FriendCratApp.instance.apiClient.okHttpClient }

    val currentUser by viewModel.currentUser.collectAsState()
    val messages by viewModel.activeMessages.collectAsState()
    val isFriendTyping by viewModel.isFriendTyping.collectAsState()
    val isSending by viewModel.isSendingMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var selectedFullImage by remember { mutableStateOf<String?>(null) }
    var isDownloading by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val listState = rememberLazyListState()

    // Photo picker for sending image from Gallery
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.sendImageMessage(uri)
        }
    }

    // Camera launcher for taking photo directly with Camera
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraUri != null) {
            viewModel.sendImageMessage(tempCameraUri!!)
        }
    }

    // Function to launch Camera with temporary FileProvider URI
    val launchCamera: () -> Unit = {
        try {
            val photoFile = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            tempCameraUri = uri
            takePictureLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open camera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Camera runtime permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(context, "Camera permission needed to take photos 📷", Toast.LENGTH_SHORT).show()
        }
    }

    // Auto-scroll to latest message
    LaunchedEffect(messages.size, isFriendTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val triggerDownload: (String) -> Unit = { url ->
        scope.launch {
            isDownloading = true
            Toast.makeText(context, "Downloading photo… 📥", Toast.LENGTH_SHORT).show()
            val success = ImageDownloader.downloadImage(context, url, okHttpClient)
            isDownloading = false
            Toast.makeText(
                context,
                if (success) "Photo saved to Gallery / Downloads folder! 🖼️✨" else "Failed to download image",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val themeColors = CuteTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CuteAvatar(user = friend, size = 42.dp, showOnlineBadge = true)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = friend.displayName.ifBlank { friend.username },
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = themeColors.textPrimary
                            )
                            Text(
                                text = if (isFriendTyping) "Typing… 💜"
                                else if (friend.online == true) "🟢 Online"
                                else "Offline",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isFriendTyping) themeColors.primary
                                else if (friend.online == true) themeColors.onlineGreen
                                else themeColors.textSecondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.closeChat()
                            onBack()
                        },
                        modifier = Modifier.testTag("chat_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = themeColors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Direct Camera button
                    IconButton(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                launchCamera()
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("chat_camera_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Take Photo with Camera",
                            tint = themeColors.primary
                        )
                    }

                    // Gallery Photo button
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("chat_attach_photo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Choose Photo from Gallery",
                            tint = themeColors.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Text Input
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = {
                            textInput = it
                            viewModel.onTypingChanged(it)
                        },
                        placeholder = { Text("Write a message… 💬", fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_message_input"),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = themeColors.textPrimary,
                            unfocusedTextColor = themeColors.textPrimary,
                            focusedPlaceholderColor = themeColors.textMuted,
                            unfocusedPlaceholderColor = themeColors.textMuted,
                            focusedBorderColor = themeColors.primary,
                            unfocusedBorderColor = themeColors.border,
                            focusedContainerColor = themeColors.background,
                            unfocusedContainerColor = themeColors.background
                        )
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Send Button
                    FilledIconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                viewModel.sendMessage(textInput)
                                textInput = ""
                            }
                        },
                        enabled = textInput.isNotBlank() && !isSending,
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = themeColors.primary),
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("chat_send_button")
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(themeColors.background)
                .padding(innerPadding)
        ) {
            if (isLoading && messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CuteLavenderPrimary)
                }
            } else if (messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CuteAvatar(user = friend, size = 68.dp, showOnlineBadge = false)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Say hello to ${friend.displayName.ifBlank { friend.username }}! ✨",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = CuteTextPrimary
                    )
                    Text(
                        text = "Your private corner together 💜",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = CuteTextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        val isMine = msg.from == currentUser?.id
                        MessageBubble(
                            message = msg,
                            isMine = isMine,
                            onImageClick = { url -> selectedFullImage = url },
                            onDownloadClick = { url -> triggerDownload(url) }
                        )
                    }

                    if (isFriendTyping) {
                        item {
                            TypingIndicatorBubble(friendName = friend.displayName.ifBlank { friend.username })
                        }
                    }
                }
            }
        }
    }

    // Full Screen Image Dialog with Download Option
    if (selectedFullImage != null) {
        Dialog(
            onDismissRequest = { selectedFullImage = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
            ) {
                // Image
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(selectedFullImage)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Full photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { selectedFullImage = null }
                )

                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { selectedFullImage = null },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }

                    // Download Button
                    Button(
                        onClick = {
                            selectedFullImage?.let { triggerDownload(it) }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CuteLavenderPrimary),
                        shape = RoundedCornerShape(24.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        if (isDownloading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Download",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isMine: Boolean,
    onImageClick: (String) -> Unit,
    onDownloadClick: (String) -> Unit
) {
    val bubbleShape = if (isMine) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
    }

    val colors = CuteTheme.colors
    val backgroundColor = if (isMine) colors.bubbleMine else colors.bubbleThem
    val textColor = if (isMine) colors.textMine else colors.textThem

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(bubbleShape)
                .background(backgroundColor)
                .border(
                    width = 1.dp,
                    color = if (isMine) Color.Transparent else colors.border,
                    shape = bubbleShape
                )
                .padding(if (message.image != null) 5.dp else 12.dp)
        ) {
            Column {
                // If message has image
                val imgUrl = message.fullImageUrl()
                if (!imgUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(14.dp))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imgUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Shared photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { onImageClick(imgUrl) }
                        )

                        // Quick Download Button Overlay on Image
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f))
                                .clickable { onDownloadClick(imgUrl) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download Photo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // If message has text
                if (!message.text.isNullOrBlank()) {
                    Text(
                        text = message.text,
                        color = textColor,
                        fontSize = 15.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(
                            horizontal = if (message.image != null) 8.dp else 0.dp,
                            vertical = if (message.image != null) 6.dp else 0.dp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = formatBubbleTime(message.createdAt),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (isMine) colors.textMuted else colors.timeThem,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun TypingIndicatorBubble(friendName: String) {
    val colors = CuteTheme.colors
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(colors.primaryContainer)
                .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "$friendName is typing… 💭",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primaryDark
            )
        }
    }
}

private fun formatBubbleTime(timestamp: Long): String {
    if (timestamp <= 0) return ""
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
}
