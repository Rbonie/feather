package org.monero.feather.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MoneroOrange,
    onPrimary = Color.White,
    primaryContainer = MoneroOrangeDark,
    onPrimaryContainer = Color.White,
    
    secondary = MoneroOrangeLight,
    onSecondary = Color.White,
    secondaryContainer = SurfaceSecondary,
    onSecondaryContainer = TextPrimary,
    
    tertiary = InfoBlue,
    onTertiary = Color.White,
    
    background = BackgroundPrimary,
    onBackground = TextPrimary,
    
    surface = SurfacePrimary,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundSecondary,
    onSurfaceVariant = TextSecondary,
    
    error = ErrorRed,
    onError = Color.White,
    
    outline = BorderColor
)

@Composable
fun FeatherTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
