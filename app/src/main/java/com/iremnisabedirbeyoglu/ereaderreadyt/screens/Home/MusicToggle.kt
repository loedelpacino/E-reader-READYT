package com.iremnisabedirbeyoglu.ereaderreadyt.screens.Home

import android.media.MediaPlayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import com.iremnisabedirbeyoglu.ereaderreadyt.R

@Composable
fun MusicToggle() {
    val context = LocalContext.current
    var isMusicOn by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    IconButton(
        onClick = {
            isMusicOn = !isMusicOn
            if (isMusicOn) {
                mediaPlayer = MediaPlayer.create(context, R.raw.cozy_music).apply {
                    isLooping = true
                    start()
                }
            } else {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            }
        },
        modifier = Modifier
    ) {
        Icon(
            imageVector = if (isMusicOn) Icons.Filled.Done else Icons.Filled.Close,
            contentDescription = if (isMusicOn) "Müziği Kapat" else "Müziği Aç",
            tint = if (isMusicOn) Color(0xFF4E342E) else Color.Gray
        )
    }

    // MediaPlayer leak'ini önlemek için
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
    }
}
