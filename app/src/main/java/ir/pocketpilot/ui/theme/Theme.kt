package ir.pocketpilot.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ir.pocketpilot.domain.model.ThemeMode

val Emerald = Color(0xFF087F70)
val Coral = Color(0xFFB84448)
val LightColors = lightColorScheme(primary = Emerald, onPrimary = Color.White, primaryContainer = Color(0xFFD6F3E9), onPrimaryContainer = Color(0xFF004E42), secondary = Color(0xFF46675F), secondaryContainer = Color(0xFFE2EEE9), background = Color(0xFFF5F7F6), surface = Color(0xFFFFFFFF), surfaceVariant = Color(0xFFEBF0ED), onSurface = Color(0xFF182C29), onSurfaceVariant = Color(0xFF51645E), outline = Color(0xFF748780), error = Coral)
val DarkColors = darkColorScheme(primary = Color(0xFF7ADABD), onPrimary = Color(0xFF00382E), primaryContainer = Color(0xFF125345), onPrimaryContainer = Color(0xFFB9F0DC), secondary = Color(0xFFB0CEC0), background = Color(0xFF101C1A), surface = Color(0xFF172623), surfaceVariant = Color(0xFF283C36), onSurface = Color(0xFFE4EEE8), onSurfaceVariant = Color(0xFFB2C6BD), outline = Color(0xFF82998E), error = Color(0xFFFFA6A6))
private val PersianTypography = Typography(
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 36.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 32.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 28.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp, lineHeight = 28.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 24.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 24.sp)
)
@Composable fun PocketTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (mode) { ThemeMode.SYSTEM -> isSystemInDarkTheme(); ThemeMode.DARK -> true; ThemeMode.LIGHT -> false }
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, typography = PersianTypography, content = content)
}
