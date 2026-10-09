package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.User
import com.example.ui.theme.*

@Composable
fun CuteAvatar(
    user: User?,
    size: Dp = 48.dp,
    showOnlineBadge: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val fullUrl = user?.fullAvatarUrl()
    val isOnline = user?.online == true

    Box(
        modifier = Modifier
            .size(size)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (!fullUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(fullUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = user?.displayName ?: "User Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(2.dp, CuteLavenderContainer, CircleShape)
            )
        } else {
            val initial = (user?.displayName?.firstOrNull() ?: user?.username?.firstOrNull() ?: 'F').uppercaseChar()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(CuteLavenderContainer)
                    .border(2.dp, CuteBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial.toString(),
                    color = CuteLavenderDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.42).sp
                )
            }
        }

        if (showOnlineBadge) {
            val badgeSize = (size * 0.28f).coerceAtLeast(10.dp)
            Box(
                modifier = Modifier
                    .size(badgeSize)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(if (isOnline) CuteOnlineGreen else CuteOfflineGrey)
                    .border(2.dp, Color.White, CircleShape)
            )
        }
    }
}

@Composable
fun CuteCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = CuteBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(16.dp),
        content = content
    )
}

@Composable
fun CuteEmptyState(
    emoji: String = "💬💜",
    title: String,
    subtitle: String,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Chat bubble and purple heart illustration badge
        Box(
            modifier = Modifier
                .padding(bottom = 18.dp)
                .size(92.dp)
                .clip(CircleShape)
                .background(CuteLavenderContainer.copy(alpha = 0.65f))
                .border(2.dp, CuteBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "💬",
                    fontSize = 36.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "💜",
                    fontSize = 34.sp
                )
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = CuteTextPrimary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 14.sp,
            color = CuteTextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(bottom = 22.dp)
        )

        if (actionButtonText != null && onActionClick != null) {
            Button(
                onClick = onActionClick,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .height(48.dp)
                    .widthIn(min = 160.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CuteLavenderPrimary,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = actionButtonText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }
    }
}
