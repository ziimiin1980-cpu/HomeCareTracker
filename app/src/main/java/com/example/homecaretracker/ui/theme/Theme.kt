package com.example.homecaretracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class AppThemeMode(val label: String) {
    SYSTEM("Как в системе"),
    LIGHT("Светлая"),
    DARK("Тёмная")
}

private val DarkColorScheme = darkColorScheme(
    primary = LightSageGreen,
    onPrimary = Color(0xFF1F2B23),
    primaryContainer = Color(0xFF35463A),
    onPrimaryContainer = Color(0xFFE1E9DF),
    secondary = Color(0xFFADBAB5),
    onSecondary = Color(0xFF25302B),
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = Color(0xFFDCE3DE),
    tertiary = LightTerracotta,
    onTertiary = Color(0xFF3A2119),
    error = Color(0xFFE4A293),
    onError = Color(0xFF3D140B),
    errorContainer = Color(0xFF512C25),
    onErrorContainer = Color(0xFFFFDBD2),
    background = DarkBackground,
    onBackground = Color(0xFFE2E5E1),
    surface = DarkSurface,
    onSurface = Color(0xFFE2E5E1),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFBBC3BC),
    outline = DarkOutline,
    outlineVariant = Color(0xFF3B443F)
)

private val LightColorScheme = lightColorScheme(
    primary = SageGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2EAE0),
    onPrimaryContainer = Color(0xFF26362B),
    secondary = MutedGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE7ECEA),
    onSecondaryContainer = Color(0xFF303A36),
    tertiary = WarmTerracotta,
    onTertiary = Color.White,
    error = Color(0xFF9D5545),
    onError = Color.White,
    errorContainer = Color(0xFFF5E3DE),
    onErrorContainer = Color(0xFF49251D),
    background = WarmBackground,
    onBackground = PrimaryText,
    surface = Color(0xFFFFFEFC),
    onSurface = PrimaryText,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = SecondaryText,
    outline = LightOutline,
    outlineVariant = Color(0xFFE5E6E0)
)

private val HomeCareShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun HomeCareTrackerTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = HomeCareShapes,
        content = content
    )
}
