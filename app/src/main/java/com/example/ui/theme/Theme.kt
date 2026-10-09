package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun FriendCratTheme(
    appTheme: AppTheme = AppTheme.LILAC,
    content: @Composable () -> Unit
) {
    val palette = when (appTheme) {
        AppTheme.LILAC -> LilacPalette
        AppTheme.SAKURA -> SakuraPalette
        AppTheme.MATCHA -> MatchaPalette
        AppTheme.OCEAN -> OceanPalette
    }

    val dynamicColorScheme = lightColorScheme(
        primary = palette.primary,
        onPrimary = Color.White,
        primaryContainer = palette.primaryContainer,
        onPrimaryContainer = palette.primaryDark,
        secondary = palette.secondary,
        onSecondary = Color.White,
        secondaryContainer = palette.secondaryLight,
        onSecondaryContainer = palette.secondary,
        background = palette.background,
        onBackground = palette.textPrimary,
        surface = palette.surface,
        onSurface = palette.textPrimary,
        surfaceVariant = palette.surfaceVariant,
        onSurfaceVariant = palette.textSecondary,
        outline = palette.border,
        outlineVariant = palette.primaryLight
    )

    CompositionLocalProvider(
        LocalAppPalette provides palette
    ) {
        MaterialTheme(
            colorScheme = dynamicColorScheme,
            typography = Typography,
            content = content
        )
    }
}
