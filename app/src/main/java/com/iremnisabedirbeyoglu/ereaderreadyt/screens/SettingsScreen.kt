package com.iremnisabedirbeyoglu.ereaderreadyt.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.iremnisabedirbeyoglu.ereaderreadyt.data.UserPreferencesManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedFontSize by remember { mutableStateOf("medium") }
    var selectedBgColor by remember { mutableStateOf("#FAF8F6") }
    var selectedFont by remember { mutableStateOf("Lora") }

    // Mevcut değerleri yükle
    LaunchedEffect(Unit) {
        UserPreferencesManager.getFontSize(context).collect {
            selectedFontSize = it
        }
        UserPreferencesManager.getBackgroundColor(context).collect {
            selectedBgColor = it
        }
        UserPreferencesManager.getFontStyle(context).collect {
            selectedFont = it
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Ayarlar", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF4E342E))

        // Yazı Boyutu
        Text("Yazı Boyutu")
        DropdownMenuSetting(
            options = listOf("small", "medium", "large"),
            selected = selectedFontSize,
            onSelect = {
                selectedFontSize = it
                scope.launch { UserPreferencesManager.setFontSize(context, it) }
            }
        )

        // Arka Plan Rengi
        Text("Arka Plan Rengi")
        DropdownMenuSetting(
            options = listOf("#FAF8F6", "#EFE6E1", "#FBEAD1"),
            selected = selectedBgColor,
            onSelect = {
                selectedBgColor = it
                scope.launch { UserPreferencesManager.setBackgroundColor(context, it) }
            }
        )

        // Font Seçimi
        Text("Font")
        DropdownMenuSetting(
            options = listOf("Lora", "Sans-serif", "Serif"),
            selected = selectedFont,
            onSelect = {
                selectedFont = it
                scope.launch { UserPreferencesManager.setFontStyle(context, it) }
            }
        )
    }
}

@Composable
fun DropdownMenuSetting(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selected)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
