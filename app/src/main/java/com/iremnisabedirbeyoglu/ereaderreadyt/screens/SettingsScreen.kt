package com.iremnisabedirbeyoglu.ereaderreadyt.screens

import android.graphics.Color.parseColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iremnisabedirbeyoglu.ereaderreadyt.data.UserPreferencesManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scroll = rememberScrollState()

    // DataStore state
    var selectedFontSize by remember { mutableStateOf("medium") }
    var selectedBgColor by remember { mutableStateOf("#FAF8F6") }
    var selectedFont by remember { mutableStateOf("Lora") }
    var selectedWeight by remember { mutableStateOf("normal") } // normal | bold
    var selectedStyle by remember { mutableStateOf("normal") }  // normal | italic

    // Load prefs
    LaunchedEffect(Unit) { UserPreferencesManager.getFontSize(context).collect { selectedFontSize = it } }
    LaunchedEffect(Unit) { UserPreferencesManager.getBackgroundColor(context).collect { selectedBgColor = it } }
    LaunchedEffect(Unit) { UserPreferencesManager.getFontStyle(context).collect { selectedFont = it } }
    LaunchedEffect(Unit) { UserPreferencesManager.getFontWeight(context).collect { selectedWeight = it } }
    LaunchedEffect(Unit) { UserPreferencesManager.getFontItalic(context).collect { selectedStyle = it } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            "Ayarlar",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF4E342E)
        )

        // Preview
        PreviewCard(
            fontSizeKey = selectedFontSize,
            fontKey = selectedFont,
            bgHex = selectedBgColor,
            weightKey = selectedWeight,
            styleKey = selectedStyle
        )

        Section(
            title = "Yazı Boyutu",
            helper = "Okuma metninin genel boyutu."
        ) {
            SingleChoiceSegmentedButtonRow {
                listOf("small", "medium", "large").forEachIndexed { index, opt ->
                    SegmentedButton(
                        selected = selectedFontSize == opt,
                        onClick = {
                            selectedFontSize = opt
                            scope.launch { UserPreferencesManager.setFontSize(context, opt) }
                        },
                        shape = SegmentedButtonDefaults.itemShape(index, 3)
                    ) { Text(when (opt) { "small" -> "Küçük"; "large" -> "Büyük"; else -> "Orta" }) }
                }
            }
        }

        Divider()

        Section(
            title = "Font",
            helper = "Metnin karakterini seç."
        ) {
            SingleChoiceSegmentedButtonRow {
                listOf("Lora", "Sans-serif", "Serif").forEachIndexed { index, opt ->
                    SegmentedButton(
                        selected = selectedFont == opt,
                        onClick = {
                            selectedFont = opt
                            scope.launch { UserPreferencesManager.setFontStyle(context, opt) }
                        },
                        shape = SegmentedButtonDefaults.itemShape(index, 3)
                    ) { Text(if (opt == "Sans-serif") "Sans" else opt) }
                }
            }
        }

        Section(
            title = "Kalınlık",
            helper = "Normal veya kalın (bold)."
        ) {
            SingleChoiceSegmentedButtonRow {
                listOf("normal", "bold").forEachIndexed { index, opt ->
                    SegmentedButton(
                        selected = selectedWeight == opt,
                        onClick = {
                            selectedWeight = opt
                            scope.launch { UserPreferencesManager.setFontWeight(context, opt) }
                        },
                        shape = SegmentedButtonDefaults.itemShape(index, 2)
                    ) { Text(if (opt == "bold") "Bold" else "Normal") }
                }
            }
        }

        Section(
            title = "Stil",
            helper = "Normal veya italik."
        ) {
            SingleChoiceSegmentedButtonRow {
                listOf("normal", "italic").forEachIndexed { index, opt ->
                    SegmentedButton(
                        selected = selectedStyle == opt,
                        onClick = {
                            selectedStyle = opt
                            scope.launch { UserPreferencesManager.setFontItalic(context, opt) }
                        },
                        shape = SegmentedButtonDefaults.itemShape(index, 2)
                    ) { Text(if (opt == "italic") "İtalik" else "Normal") }
                }
            }
        }

        Divider()

        Section(
            title = "Arka Plan Rengi",
            helper = "Okuma ekranının arka plan tonu."
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("#FAF8F6", "#EFE6E1", "#FBEAD1").forEach { hex ->
                    ColorSwatch(
                        hex = hex,
                        selected = selectedBgColor == hex
                    ) {
                        selectedBgColor = hex
                        scope.launch { UserPreferencesManager.setBackgroundColor(context, hex) }
                    }
                }
            }
        }

        // Bottom padding so it doesn't collide with nav bar
        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                selectedFontSize = "medium"
                selectedBgColor = "#FAF8F6"
                selectedFont = "Lora"
                selectedWeight = "normal"
                selectedStyle = "normal"
                scope.launch {
                    UserPreferencesManager.setFontSize(context, "medium")
                    UserPreferencesManager.setBackgroundColor(context, "#FAF8F6")
                    UserPreferencesManager.setFontStyle(context, "Lora")
                    UserPreferencesManager.setFontWeight(context, "normal")
                    UserPreferencesManager.setFontItalic(context, "normal")
                }
            },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp) // nav bar için alt boşluk
        ) {
            Icon(Icons.Default.Restore, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Varsayılanlara Dön")
        }
    }
}

/* ----------------- UI parçaları ----------------- */

@Composable
private fun Section(
    title: String,
    helper: String? = null,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (helper != null) {
            Text(
                helper,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        content()
    }
}

@Composable
private fun ColorSwatch(hex: String, selected: Boolean, onClick: () -> Unit) {
    val color = Color(parseColor(hex))
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {}
}

@Composable
private fun PreviewCard(
    fontSizeKey: String,
    fontKey: String,
    bgHex: String,
    weightKey: String,
    styleKey: String
) {
    val fontSize = when (fontSizeKey) {
        "small" -> 14.sp
        "large" -> 18.sp
        else -> 16.sp
    }
    val family = when (fontKey) {
        "Sans-serif" -> FontFamily.SansSerif
        "Serif" -> FontFamily.Serif
        else -> FontFamily.Serif
    }
    val weight = if (weightKey == "bold") FontWeight.Bold else FontWeight.Normal
    val style  = if (styleKey  == "italic") FontStyle.Italic  else FontStyle.Normal
    val bg = Color(parseColor(bgHex))

    ElevatedCard(shape = RoundedCornerShape(18.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(bg.copy(alpha = 0.6f))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Önizleme",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                "Okuma ekranınız bu ayarlarla görünecek. Yazı boyutu, font, kalınlık ve stil burada deneyebilirsiniz.",
                fontSize = fontSize,
                fontFamily = family,
                fontWeight = weight,
                fontStyle = style
            )
        }
    }
}

