package com.iremnisabedirbeyoglu.ereaderreadyt.screens

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.iremnisabedirbeyoglu.ereaderreadyt.data.PdfStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import getDisplayName

@Composable
fun BookListScreen(
    navController: NavController? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Veri durumu
    val allUris = remember { mutableStateListOf<Uri>() }
    var query by remember { mutableStateOf("") }
    var isGrid by remember { mutableStateOf(true) } // grid varsayılan cozy duruyor

    // DataStore’dan yükle
    LaunchedEffect(Unit) {
        PdfStorageManager.getPdfUriList(context).collectLatest { uris ->
            allUris.clear()
            allUris.addAll(uris)
        }
    }

    // Filtrelenmiş liste
    val filtered = remember(query, allUris) {
        if (query.isBlank()) allUris
        else allUris.filter { it.toString().contains(query, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Başlık + arama + görünüm toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kütüphanem",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            IconButton(
                onClick = { isGrid = !isGrid }
            ) {
                Icon(
                    imageVector = if (isGrid) Icons.Default.List else Icons.Default.GridView,
                    contentDescription = "Görünümü değiştir"
                )
            }
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            placeholder = { Text("Ara…") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(Modifier.height(4.dp))

        if (filtered.isEmpty()) {
            EmptyLibrary(
                onAddClick = { navController?.navigate("add") }
            )
        } else {
            if (isGrid) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered, key = { it.toString() }) { uri ->
                        BookCardGrid(
                            uri = uri,
                            onOpen = {
                                navController?.navigate("reader?uri=${Uri.encode(uri.toString())}")
                            },
                            onRemove = {
                                scope.launch {
                                    PdfStorageManager.removePdfUri(context, uri)
                                }
                            }
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered, key = { it.toString() }) { uri ->
                        BookRowItem(
                            uri = uri,
                            onOpen = {
                                navController?.navigate("reader?uri=${Uri.encode(uri.toString())}")
                            },
                            onRemove = {
                                scope.launch {
                                    PdfStorageManager.removePdfUri(context, uri)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/* --------------------------- UI Bileşenleri --------------------------- */

@Composable
private fun BookRowItem(
    uri: Uri,
    onOpen: () -> Unit,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    var displayName by remember(uri) { mutableStateOf<String?>(null) }
    val thumb by remember(uri) { mutableStateOf(uri) }.let {
        produceState<Bitmap?>(initialValue = null, key1 = uri) {
            value = renderFirstPageThumbnail(context.contentResolver, uri, 96)
        }
    }

    LaunchedEffect(uri) {
        displayName = getDisplayName(context.contentResolver, uri)
    }

    ElevatedCard(
        onClick = onOpen,
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Thumbnail(thumb = thumb, width = 56.dp, height = 74.dp)

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = displayName ?: (uri.lastPathSegment ?: "PDF"),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = uri.authority ?: "",
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = onOpen, shape = RoundedCornerShape(12.dp)) {
                    Text("Oku")
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Kaldır")
                }
            }
        }
    }
}

@Composable
private fun BookCardGrid(
    uri: Uri,
    onOpen: () -> Unit,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    var displayName by remember(uri) { mutableStateOf<String?>(null) }
    val thumb by remember(uri) { mutableStateOf(uri) }.let {
        produceState<Bitmap?>(initialValue = null, key1 = uri) {
            value = renderFirstPageThumbnail(context.contentResolver, uri, 140)
        }
    }

    LaunchedEffect(uri) {
        displayName = getDisplayName(context.contentResolver, uri)
    }

    ElevatedCard(
        onClick = onOpen,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Thumbnail(thumb = thumb, width = 110.dp, height = 145.dp)

            Text(
                text = displayName ?: (uri.lastPathSegment ?: "PDF"),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilledTonalButton(
                    onClick = onOpen,
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Oku") }

                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Kaldır")
                }
            }
        }
    }
}

@Composable
private fun Thumbnail(thumb: Bitmap?, width: Dp, height: Dp) {
    if (thumb != null) {
        Image(
            bitmap = thumb.asImageBitmap(),
            contentDescription = "Önizleme",
            modifier = Modifier
                .width(width)
                .height(height)
                .clip(RoundedCornerShape(10.dp))
        )
    } else {
        // Yüklenene kadar yumuşak placeholder
        Box(
            modifier = Modifier
                .width(width)
                .height(height)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = null)
        }
    }
}

@Composable
private fun EmptyLibrary(onAddClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.LibraryBooks, contentDescription = null)
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    "Henüz kitap yok",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            Text("“Ekle” ile cihazından bir PDF seçip kütüphanene ekleyebilirsin.")
            FilledTonalButton(onClick = onAddClick, shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("PDF Ekle")
            }
        }
    }
}

/* ---------------------- Yardımcı: hızlı thumbnail render ---------------------- */

private suspend fun renderFirstPageThumbnail(
    resolver: android.content.ContentResolver,
    uri: Uri,
    targetWidthPx: Int
): Bitmap? = withContext(Dispatchers.IO) {
    val pfd: ParcelFileDescriptor = try {
        resolver.openFileDescriptor(uri, "r") ?: return@withContext null
    } catch (_: Exception) { return@withContext null }

    pfd.use { fd ->
        try {
            PdfRenderer(fd).use { renderer ->
                if (renderer.pageCount == 0) return@withContext null
                renderer.openPage(0).use { page ->
                    val ratio = page.height.toFloat() / page.width.toFloat()
                    val w = targetWidthPx
                    val h = (w * ratio).toInt()
                    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    return@withContext bmp
                }
            }
        } catch (_: Exception) {
            return@withContext null
        }
    }
}

