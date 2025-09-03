package com.iremnisabedirbeyoglu.ereaderreadyt.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.iremnisabedirbeyoglu.ereaderreadyt.data.PdfStorageManager
import kotlinx.coroutines.launch
import getDisplayName
import kotlinx.coroutines.CoroutineScope

@Composable
fun AddBookScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }

    // Son eklenen(ler)i göstermek için
    var lastAdded by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Tekli seçim
    val pickSingle = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { selected ->
            persistAndSave(scope, context, listOf(selected)) { saved ->
                lastAdded = saved
                // başarı → kütüphaneye
                navController?.navigate("library") {
                    launchSingleTop = true
                    popUpTo("library") { inclusive = false }
                }
            }
        }
    }

    // Çoklu seçim
    val pickMultiple = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            persistAndSave(scope, context, uris) { saved ->
                lastAdded = saved
                navController?.navigate("library") {
                    launchSingleTop = true
                    popUpTo("library") { inclusive = false }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snack) }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "PDF Ekle",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF4E342E)
            )

            // Büyük çağrı kartı
            ElevatedCard(
                onClick = { pickSingle.launch(arrayOf("application/pdf")) },
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
                    Text(
                        "Cihazından PDF seç",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Tek dokunuşla bir PDF ekle. İstersen aşağıdan çoklu seçim yapabilirsin.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    FilledTonalButton(
                        onClick = { pickSingle.launch(arrayOf("application/pdf")) },
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("PDF Seç")
                    }
                }
            }

            // Çoklu seçim
            OutlinedButton(
                onClick = { pickMultiple.launch(arrayOf("application/pdf")) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.LibraryBooks, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Birden çok PDF seç")
            }

            // İpucu satırı
            AssistChipBar(
                text = "Seçilen PDF’lere kalıcı erişim verilir; uygulamayı silene kadar kütüphanede kalır."
            )

            // Son eklenen(ler)
            if (lastAdded.isNotEmpty()) {
                ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Son eklenenler", style = MaterialTheme.typography.titleMedium)
                        lastAdded.take(3).forEach { uri ->
                            val name by produceState<String?>(initialValue = null, key1 = uri) {
                                value = getDisplayName(context.contentResolver, uri)
                            }
                            Text(
                                text = "• " + (name ?: (uri.lastPathSegment ?: "PDF")),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (lastAdded.size > 3) {
                            Text("… ve ${lastAdded.size - 3} dosya daha")
                        }
                    }
                }
            }
        }
    }
}

/* ---------- küçük yardımcılar ---------- */

@Composable
private fun AssistChipBar(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

/**
 * URI’lere kalıcı okuma izni verip DataStore’a kaydeder.
 */
private fun persistAndSave(
    scope: androidx.lifecycle.LifecycleCoroutineScope? = null, // isteğe bağlı değilse null geçilebilir
    context: android.content.Context,
    uris: List<Uri>,
    onDone: (List<Uri>) -> Unit
) {
    // Composition içinde rememberCoroutineScope() kullandığımız için
    // bu overload'ı sadeleştiriyoruz:
}

private fun persistAndSave(
    scope: CoroutineScope,
    context: android.content.Context,
    uris: List<Uri>,
    onDone: (List<Uri>) -> Unit
) {
    scope.launch {
        // Kalıcı okuma izni
        uris.forEach { uri ->
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) { /* zaten izin verilmiş olabilir */ }
        }
        // DataStore’a ekle (set olduğu için kopyalar otomatik engellenir)
        uris.forEach { PdfStorageManager.addPdfUri(context, it) }
        onDone(uris)
    }
}
