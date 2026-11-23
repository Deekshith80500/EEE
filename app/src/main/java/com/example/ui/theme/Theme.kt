package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
  primary = AppleBlue,
  onPrimary = Color.White,
  primaryContainer = AppleBlueLight,
  onPrimaryContainer = Color(0xFF0369A1),
  secondary = AppleTeal,
  onSecondary = Color.White,
  secondaryContainer = AppleTealLight,
  onSecondaryContainer = Color(0xFF0F766E),
  background = AppleBgLight,
  onBackground = TextPrimary,
  surface = AppleSurfaceLight,
  onSurface = TextPrimary,
  surfaceVariant = AppleSurfaceSubtle,
  onSurfaceVariant = TextSecondary,
  outline = AppleBorder,
  outlineVariant = AppleBorderSubtle
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = LightColorScheme,
    typography = Typography,
    content = content
  )
}
