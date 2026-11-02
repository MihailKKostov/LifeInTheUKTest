package uk.lifeintheuk.study.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
data class StudyColors(
    val highlight: Color,
    val onHighlight: Color,
    val success: Color,
    val successContainer: Color,
    val danger: Color,
    val dangerContainer: Color,
    val tip: Color,
    val onTip: Color,
    val accent: Color,
)

private val LightStudy = StudyColors(
    highlight = Color(0xFFFFE89A),
    onHighlight = Color(0xFF2B2300),
    success = Color(0xFF2E7D4F),
    successContainer = Color(0xFFDDF1E4),
    danger = Color(0xFFB3261E),
    dangerContainer = Color(0xFFF9DEDC),
    tip = Color(0xFFE8EEF8),
    onTip = Color(0xFF1D3557),
    accent = Color(0xFFC8102E),
)

private val DarkStudy = StudyColors(
    highlight = Color(0xFF5A4A12),
    onHighlight = Color(0xFFFFF4CC),
    success = Color(0xFF7BD19A),
    successContainer = Color(0xFF1E3A29),
    danger = Color(0xFFF2B8B5),
    dangerContainer = Color(0xFF4A1F1C),
    tip = Color(0xFF1F2A3A),
    onTip = Color(0xFFC9D8F0),
    accent = Color(0xFFFF8A80),
)

val LocalStudyColors = staticCompositionLocalOf { LightStudy }

private val LightColors = lightColorScheme(
    primary = Color(0xFF1D3557),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE5F3),
    onPrimaryContainer = Color(0xFF0F1F36),
    secondary = Color(0xFF55657E),
    secondaryContainer = Color(0xFFE6EAF0),
    onSecondaryContainer = Color(0xFF1D2738),
    background = Color(0xFFF8F7F3),
    onBackground = Color(0xFF1B1C1E),
    surface = Color(0xFFF8F7F3),
    onSurface = Color(0xFF1B1C1E),
    surfaceVariant = Color(0xFFECEAE4),
    onSurfaceVariant = Color(0xFF5D5E61),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFF1EFEA),
    surfaceContainerHigh = Color(0xFFEBE9E3),
    outline = Color(0xFFC9C7C0),
    outlineVariant = Color(0xFFE2E0DA),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA9C2E8),
    onPrimary = Color(0xFF0F2440),
    primaryContainer = Color(0xFF263B5A),
    onPrimaryContainer = Color(0xFFDCE5F3),
    secondary = Color(0xFFB9C4D6),
    secondaryContainer = Color(0xFF2C3442),
    onSecondaryContainer = Color(0xFFDCE3EE),
    background = Color(0xFF121316),
    onBackground = Color(0xFFE4E2DE),
    surface = Color(0xFF121316),
    onSurface = Color(0xFFE4E2DE),
    surfaceVariant = Color(0xFF2A2C30),
    onSurfaceVariant = Color(0xFFB3B2AE),
    surfaceContainerLowest = Color(0xFF0D0E10),
    surfaceContainerLow = Color(0xFF1B1C20),
    surfaceContainer = Color(0xFF1F2024),
    surfaceContainerHigh = Color(0xFF26272B),
    outline = Color(0xFF4A4B50),
    outlineVariant = Color(0xFF34353A),
)

private val AppTypography = Typography().run {
    copy(
        headlineLarge = headlineLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = (-0.25).sp),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
    )
}

/** Serif body style used for the narrative reading text. */
val ReadingStyle = TextStyle(
    fontFamily = FontFamily.Serif,
    fontSize = 18.sp,
    lineHeight = 29.sp,
)

@Composable
fun StudyTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    CompositionLocalProvider(LocalStudyColors provides if (dark) DarkStudy else LightStudy) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = AppTypography,
            content = content,
        )
    }
}
