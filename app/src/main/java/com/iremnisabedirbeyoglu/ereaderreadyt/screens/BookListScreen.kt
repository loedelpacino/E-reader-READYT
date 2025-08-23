package com.iremnisabedirbeyoglu.ereaderreadyt.screens

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.iremnisabedirbeyoglu.ereaderreadyt.data.PdfStorageManager
import kotlinx.coroutines.flow.collectLatest

@Composable
fun BookListScreen(
    navController: NavController? = null // İsteğe bağlı yönlendirme için
) {
    val context = LocalContext.current
    val uriListState = remember { mutableStateListOf<Uri>() }

    // URI listesini DataStore'dan al
    LaunchedEffect(Unit) {
        PdfStorageManager.getPdfUriList(context).collectLatest { uris ->
            uriListState.clear()
            uriListState.addAll(uris)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Kütüphanem",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF4E342E)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uriListState.isEmpty()) {
            Text("Henüz kitap eklenmedi.", color = Color.Gray)
        } else {
            LazyColumn {
                items(uriListState) { uri ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                navController?.navigate("reader?uri=${Uri.encode(uri.toString())}")
                            }
                        ,
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF9F4)
                        )
                    ) {
                        Text(
                            text = uri.lastPathSegment ?: "PDF Dosyası",
                            modifier = Modifier.padding(16.dp),
                            color = Color(0xFF4E342E)
                        )
                    }
                }
            }
        }
    }
}
