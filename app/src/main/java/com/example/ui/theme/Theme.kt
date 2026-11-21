package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun CasanareTheme(
    primaryHex: String = DefaultPrimaryHex,
    secondaryHex: String = DefaultSecondaryHex,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val primaryColor = parseHexColorOrDefault(primaryHex, EmeraldLlanero)
    val secondaryColor = parseHexColorOrDefault(secondaryHex, AmberSabana)

    val lightScheme = lightColorScheme(
        primary = primaryColor,
        onPrimary = Color.White,
        primaryContainer = primaryColor.copy(alpha = 0.14f),
        onPrimaryContainer = InkDark,
        secondary = secondaryColor,
        onSecondary = Color.White,
        secondaryContainer = secondaryColor.copy(alpha = 0.16f),
        onSecondaryContainer = InkDark,
        tertiary = EmeraldDeep,
        onTertiary = Color.White,
        background = SurfaceWarmPaper,
        onBackground = InkDark,
        surface = SurfaceCardLight,
        onSurface = InkDark,
        surfaceVariant = SurfaceMuted,
        onSurfaceVariant = InkMuted,
        outline = Color(0xFFC8D5CE),
        error = AlertCritical,
        onError = Color.White
    )

    val darkScheme = darkColorScheme(
        primary = primaryColor.copy(alpha = 0.9f),
        onPrimary = Color.White,
        primaryContainer = primaryColor.copy(alpha = 0.25f),
        onPrimaryContainer = Color(0xFFE6F4EE),
        secondary = secondaryColor,
        onSecondary = Color(0xFF1F1606),
        secondaryContainer = secondaryColor.copy(alpha = 0.24f),
        onSecondaryContainer = Color(0xFFFFEFC7),
        background = Color(0xFF0D1713),
        onBackground = Color(0xFFEBF2EE),
        surface = Color(0xFF14221C),
        onSurface = Color(0xFFEBF2EE),
        surfaceVariant = Color(0xFF1E322A),
        onSurfaceVariant = Color(0xFFACC2B7),
        outline = Color(0xFF334E42),
        error = Color(0xFFF87171),
        onError = Color(0xFF3B0909)
    )

    MaterialTheme(
        colorScheme = if (darkTheme) darkScheme else lightScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
