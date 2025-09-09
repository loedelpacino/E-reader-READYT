// path: app/src/main/java/com/iremnisabedirbeyoglu/ereaderreadyt/screens/home/DashboardScreen.kt
package com.iremnisabedirbeyoglu.ereaderreadyt.screens.home

import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import com.iremnisabedirbeyoglu.ereaderreadyt.data.PdfStorageManager
import com.iremnisabedirbeyoglu.ereaderreadyt.screens.home.MusicToggleButton
import com.iremnisabedirbeyoglu.ereaderreadyt.util.getDisplayName

@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current

    // Son eklenen 1 kitap
    val recentList = remember { mutableStateListOf<Uri>() }

    // Son okunan kitap
    var lastRead by remember { mutableStateOf<Uri?>(null) }

    // Son eklenen 1 kitabı al
    LaunchedEffect(Unit) {
        PdfStorageManager.getPdfUriList(context).collectLatest { list ->
            recentList.clear()
            recentList.addAll(list.takeLast(1))
        }
    }

    // Son okunan kitabı al
    LaunchedEffect(Unit) {
        PdfStorageManager.getLastReadPdf(context).collectLatest { uri ->
            lastRead = uri
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Üst kısım (başlık + müzik butonu – yazısız)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Okumaya başla!",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            // yalnızca ikon: yazı yok
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
            ) {
                // İkonlu toggle
                MusicToggleButton()
            }
        }

        Text(
            text = "Bugün ne okumak istersin?",
            style = MaterialTheme.typography.bodyLarge
        )

        // Hızlı eylemler
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            QuickCard(
                title = "Kütüphane",
                subtitle = "Kitaplarını gör",
                icon = Icons.Filled.Book,
                modifier = Modifier.weight(1f)
            ) { navController.navigate("bookList") }

            QuickCard(
                title = "Ekle",
                subtitle = "PDF seç",
                icon = Icons.Filled.Add,
                modifier = Modifier.weight(1f)
            ) { navController.navigate("addBook") } // AppNavHost'ta alias "add" da var

            QuickCard(
                title = "Ayarlar",
                subtitle = "Tema & font",
                icon = Icons.Filled.Settings,
                modifier = Modifier.weight(1f)
            ) { navController.navigate("settings") }
        }

        //  Son Okunan kitap kartı
        lastRead?.let { lastUri ->
            val lastDisplayName by produceState<String?>(initialValue = null, key1 = lastUri) {
                value = getDisplayName(context.contentResolver, lastUri)
            }

            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Son okunan",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = lastDisplayName ?: (lastUri.lastPathSegment ?: "PDF"),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        FilledTonalButton(
                            onClick = {
                                navController.navigate("reader?uri=${Uri.encode(lastUri.toString())}")
                            },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Kaldığın yerden devam et")
                        }
                    }
                }
            }
        }

        // Son Eklenen kitap kartı
        if (recentList.isNotEmpty()) {
            val uri = recentList.first()
            val displayName by produceState<String?>(initialValue = null, key1 = uri) {
                value = getDisplayName(context.contentResolver, uri)
            }

            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Son eklenen",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = displayName ?: (uri.lastPathSegment ?: "PDF"),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        FilledTonalButton(
                            onClick = {
                                navController.navigate("reader?uri=${Uri.encode(uri.toString())}")
                            },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Aç")
                        }
                    }
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
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.height(118.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ikon için yumuşak bir baloncuk
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    shape = CircleShape
                ) {
                    Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            contentDescription = title,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
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
