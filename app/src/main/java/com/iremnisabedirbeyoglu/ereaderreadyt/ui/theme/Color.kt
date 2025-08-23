package com.iremnisabedirbeyoglu.ereaderreadyt.ui.theme

import androidx.compose.ui.graphics.Color

// Cozy Tema Renkleri
val MilkWhite = Color(0xFFFAF8F6) // Süt beyazı (arka plan)
val MilkyCoffee = Color(0xFFA1887F) // Sütlü kahve (buton, vurgu)
val DarkCoffee = Color(0xFF4E342E) // Koyu kahve (başlıklar, ikonlar)

// Tema için kullanılan diğer renkler (Material Design 3'e uygun)
val Primary = MilkyCoffee
val OnPrimary = Color.White
val PrimaryContainer = MilkyCoffee.copy(alpha = 0.2f)
val OnPrimaryContainer = DarkCoffee
val Secondary = MilkyCoffee
val OnSecondary = Color.White
val SecondaryContainer = MilkyCoffee.copy(alpha = 0.2f)
val OnSecondaryContainer = DarkCoffee
val Tertiary = MilkyCoffee
val OnTertiary = Color.White
val TertiaryContainer = MilkyCoffee.copy(alpha = 0.2f)
val OnTertiaryContainer = DarkCoffee
val Error = Color(0xFFB00020)
val ErrorContainer = Color(0xFFFDD8DF)
val OnError = Color.White
val OnErrorContainer = Color(0xFF410002)
val Background = MilkWhite
val OnBackground = DarkCoffee
val Surface = MilkWhite
val OnSurface = DarkCoffee
val SurfaceVariant = MilkWhite.copy(alpha = 0.8f)
val OnSurfaceVariant = DarkCoffee.copy(alpha = 0.8f)
val Outline = DarkCoffee.copy(alpha = 0.5f)
val InverseOnSurface = MilkWhite
val InverseSurface = DarkCoffee
val InversePrimary = Color.White
val SurfaceTint = MilkyCoffee
val Scrim = Color(0xFF000000).copy(alpha = 0.4f)
val AppBarColor = MilkyCoffee // Bu rengi de ekledik
