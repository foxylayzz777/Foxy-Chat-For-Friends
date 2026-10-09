package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class AppTheme(
    val id: String,
    val displayName: String,
    val emoji: String,
    val previewColor: Color,
    val previewBg: Color
) {
    LILAC("lilac", "Foxy Lilac", "💜", Color(0xFF7C3AED), Color(0xFFF8F5FD)),
    SAKURA("sakura", "Sakura Rose", "🌸", Color(0xFFE11D48), Color(0xFFFFF5F6)),
    MATCHA("matcha", "Matcha Mint", "🍵", Color(0xFF059669), Color(0xFFF2FAF5)),
    OCEAN("ocean", "Sky Azure", "🌊", Color(0xFF2563EB), Color(0xFFF0F7FF));

    companion object {
        fun fromId(id: String): AppTheme = values().firstOrNull { it.id == id } ?: LILAC
    }
}

data class AppPalette(
    val primary: Color,
    val primaryDark: Color,
    val primaryLight: Color,
    val primaryContainer: Color,
    val secondary: Color,
    val secondaryLight: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val bubbleMine: Color,
    val bubbleThem: Color,
    val textMine: Color,
    val textThem: Color,
    val timeMine: Color,
    val timeThem: Color,
    val onlineGreen: Color = Color(0xFF059669),
    val offlineGrey: Color = Color(0xFF6B7280)
)

// 1. Foxy Lilac (Vivid violet, soft lavender background, deep charcoal text)
val LilacPalette = AppPalette(
    primary = Color(0xFF7C3AED),
    primaryDark = Color(0xFF5B21B6),
    primaryLight = Color(0xFFEDE9FE),
    primaryContainer = Color(0xFFDDD6FE),
    secondary = Color(0xFFDB2777),
    secondaryLight = Color(0xFFFCE7F3),
    background = Color(0xFFF8F5FD),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF3E8FF),
    border = Color(0xFFC4B5FD),
    textPrimary = Color(0xFF111827),
    textSecondary = Color(0xFF374151),
    textMuted = Color(0xFF4B5563),
    bubbleMine = Color(0xFF7C3AED),
    bubbleThem = Color(0xFFF3E8FF),
    textMine = Color(0xFFFFFFFF),
    textThem = Color(0xFF111827),
    timeMine = Color(0xFFE9D5FF),
    timeThem = Color(0xFF4B5563)
)

// 2. Sakura Rose (Rose petal pink, soft blush mist background, crisp dark charcoal text)
val SakuraPalette = AppPalette(
    primary = Color(0xFFE11D48),
    primaryDark = Color(0xFF9F1239),
    primaryLight = Color(0xFFFFE4E6),
    primaryContainer = Color(0xFFFECDD3),
    secondary = Color(0xFFF43F5E),
    secondaryLight = Color(0xFFFFF1F2),
    background = Color(0xFFFFF5F6),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFFFE4E6),
    border = Color(0xFFFDA4AF),
    textPrimary = Color(0xFF18181B),
    textSecondary = Color(0xFF3F3F46),
    textMuted = Color(0xFF52525B),
    bubbleMine = Color(0xFFE11D48),
    bubbleThem = Color(0xFFFFE4E6),
    textMine = Color(0xFFFFFFFF),
    textThem = Color(0xFF18181B),
    timeMine = Color(0xFFFFE4E6),
    timeThem = Color(0xFF52525B)
)

// 3. Matcha Mint (Fresh emerald sage, soothing tea background, ultra-clear dark charcoal text)
val MatchaPalette = AppPalette(
    primary = Color(0xFF059669),
    primaryDark = Color(0xFF065F46),
    primaryLight = Color(0xFFECFDF5),
    primaryContainer = Color(0xFFA7F3D0),
    secondary = Color(0xFF10B981),
    secondaryLight = Color(0xFFD1FAE5),
    background = Color(0xFFF2FAF5),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFD1FAE5),
    border = Color(0xFF6EE7B7),
    textPrimary = Color(0xFF111827),
    textSecondary = Color(0xFF374151),
    textMuted = Color(0xFF4B5563),
    bubbleMine = Color(0xFF059669),
    bubbleThem = Color(0xFFECFDF5),
    textMine = Color(0xFFFFFFFF),
    textThem = Color(0xFF111827),
    timeMine = Color(0xFFD1FAE5),
    timeThem = Color(0xFF4B5563)
)

// 4. Sky Azure (Bright ocean blue, airy sky background, deep navy-charcoal text)
val OceanPalette = AppPalette(
    primary = Color(0xFF2563EB),
    primaryDark = Color(0xFF1E40AF),
    primaryLight = Color(0xFFEFF6FF),
    primaryContainer = Color(0xFFBFDBFE),
    secondary = Color(0xFF3B82F6),
    secondaryLight = Color(0xFFDBEAFE),
    background = Color(0xFFF0F7FF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFDBEAFE),
    border = Color(0xFF93C5FD),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF334155),
    textMuted = Color(0xFF475569),
    bubbleMine = Color(0xFF2563EB),
    bubbleThem = Color(0xFFEFF6FF),
    textMine = Color(0xFFFFFFFF),
    textThem = Color(0xFF0F172A),
    timeMine = Color(0xFFDBEAFE),
    timeThem = Color(0xFF475569)
)

val LocalAppPalette = staticCompositionLocalOf { LilacPalette }

object CuteTheme {
    val colors: AppPalette
        @Composable
        get() = LocalAppPalette.current
}

// Fallback constant bindings for backward compatibility
val CuteLavenderPrimary = LilacPalette.primary
val CuteLavenderDark = LilacPalette.primaryDark
val CuteLavenderLight = LilacPalette.primaryLight
val CuteLavenderContainer = LilacPalette.primaryContainer
val CutePinkHeart = LilacPalette.secondary
val CutePinkHeartLight = LilacPalette.secondaryLight

val CuteOnlineGreen = Color(0xFF059669)
val CuteOfflineGrey = Color(0xFF6B7280)

val CuteBackground = LilacPalette.background
val CuteSurface = LilacPalette.surface
val CuteSurfaceVariant = LilacPalette.surfaceVariant
val CuteBorder = LilacPalette.border

val CuteTextPrimary = LilacPalette.textPrimary
val CuteTextSecondary = LilacPalette.textSecondary
val CuteTextMuted = LilacPalette.textMuted

val CuteBubbleMine = LilacPalette.bubbleMine
val CuteBubbleThem = LilacPalette.bubbleThem
val CuteTextMine = LilacPalette.textMine
val CuteTextThem = LilacPalette.textThem
val CuteTimeMine = LilacPalette.timeMine
val CuteTimeThem = LilacPalette.timeThem
