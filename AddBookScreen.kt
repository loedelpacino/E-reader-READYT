package com.iremnisabedirbeyoglu.ereaderreadyt.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.iremnisabedirbeyoglu.ereaderreadyt.data.PdfStorageManager
import kotlinx.coroutines.launch

@Composable
fun AddBookScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var lastAddedUri by remember { mutableStateOf<Uri?>(null) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            lastAddedUri = it
            scope.launch {
                PdfStorageManager.addPdfUri(context, it)
                // Başarılı ekleme -> kütüphaneye
                navController?.navigate("library") {
                    launchSingleTop = true
                    popUpTo("library") { inclusive = false }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("PDF Kitap Ekle", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF4E342E))
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA1887F))
        ) { Text("Cihazdan PDF Seç", color = Color.White) }
        Spacer(Modifier.height(24.dp))
        lastAddedUri?.let { uri ->
            Text("Eklenen dosya: ${uri.lastPathSegment}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF4E342E))
        }
    }
}
