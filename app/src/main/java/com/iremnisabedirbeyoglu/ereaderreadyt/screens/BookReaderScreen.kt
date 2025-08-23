package com.iremnisabedirbeyoglu.ereaderreadyt.screens

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import loadPdfBitmaps

@Composable
fun BookReaderScreen(uriString: String?) {
    val context = LocalContext.current

    if (uriString == null) {
        Text("PDF bulunamadı.")
        return
    }

    val uri = Uri.parse(uriString)
    var bitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }

    // PDF'den bitmap'leri üret
    LaunchedEffect(uri) {
        bitmaps = loadPdfBitmaps(context.contentResolver, uri)
    }

    // Görüntüle
    if (bitmaps.isEmpty()) {
        CircularProgressIndicator(modifier = Modifier.padding(24.dp))
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(bitmaps) { index, bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Sayfa ${index + 1}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                )
            }
        }
    }
}
