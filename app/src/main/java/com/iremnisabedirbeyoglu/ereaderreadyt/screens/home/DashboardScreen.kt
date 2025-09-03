package com.iremnisabedirbeyoglu.ereaderreadyt.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.graphics.vector.ImageVector
import com.iremnisabedirbeyoglu.ereaderreadyt.data.PdfStorageManager
import com.iremnisabedirbeyoglu.ereaderreadyt.screens.home.MusicToggleButton
import getDisplayName

@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    val recentList = remember { mutableStateListOf<android.net.Uri>() }

    // Son eklenen 1 kitabı al
    LaunchedEffect(Unit) {
        PdfStorageManager.getPdfUriList(context).collectLatest { list ->
            recentList.clear()
            recentList.addAll(list.takeLast(1))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Üst kısım (başlık + müzik)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Merhaba! ", style = MaterialTheme.typography.titleLarge)
            // Eski: MusicToggle()
            MusicToggleButton()  // << yeni buton + altta “Müzik”
        }

        Text(text = "Bugün ne yapmak istersin?", style = MaterialTheme.typography.bodyLarge)

        // Hızlı eylemler
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            QuickCard(
                title = "Kütüphane",
                subtitle = "Kitaplarını gör",
                icon = Icons.Filled.List,
                modifier = Modifier.weight(1f)
            ) { navController.navigate("library") }

            QuickCard(
                title = "Ekle",
                subtitle = "PDF seç",
                icon = Icons.Filled.Add,
                modifier = Modifier.weight(1f)
            ) { navController.navigate("add") }

            QuickCard(
                title = "Ayarlar",
                subtitle = "Tema & font",
                icon = Icons.Filled.Settings,
                modifier = Modifier.weight(1f)
            ) { navController.navigate("settings") }
        }

        // Son eklenen kitap
        if (recentList.isNotEmpty()) {
            val uri = recentList.first()
            val displayName by produceState<String?>(initialValue = null, key1 = uri) {
                value = getDisplayName(context.contentResolver, uri)
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Son eklenen", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = displayName ?: (uri.lastPathSegment ?: "PDF"),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    OutlinedButton(
                        onClick = {
                            navController.navigate("reader?uri=${Uri.encode(uri.toString())}")
                        }
                    ) { Text("Okumaya devam et") }
                }
            }
        }
    }
}

@Composable
private fun QuickCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier.height(120.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = title)
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
