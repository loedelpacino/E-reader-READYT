package com.iremnisabedirbeyoglu.ereaderreadyt.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController // Accompanist import



private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    errorContainer = ErrorContainer,
    onError = OnError,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    inverseOnSurface = InverseOnSurface,
    inverseSurface = InverseSurface,
    inversePrimary = InversePrimary,
    surfaceTint = SurfaceTint,
    // Diğer renkleri de Color.kt'den alabilirsiniz
)

@Composable
fun ReadyttTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Şimdilik sistem temasına göre ayarlı
    // Dinamik renkler Android 12+ üzerinde mevcut
    dynamicColor: Boolean = false, // Dokümantasyonda belirtilmediği için false bırakıyoruz
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> LightColorScheme // Şimdilik sadece LightColorScheme'imiz var
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb() // Durum çubuğu rengi
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    // Accompanist System UI Controller ile durum çubuğu ve navigasyon çubuğu renklerini ayarla
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !darkTheme // Koyu temada açık ikonlar, açık temada koyu ikonlar

    SideEffect {
        systemUiController.setStatusBarColor(
            color = colorScheme.background, // Arka plan rengiyle aynı yapıyoruz
            darkIcons = useDarkIcons
        )
        systemUiController.setNavigationBarColor(
            color = colorScheme.background, // Arka plan rengiyle aynı yapıyoruz
            darkIcons = useDarkIcons
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
