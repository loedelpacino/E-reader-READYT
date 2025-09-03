package com.iremnisabedirbeyoglu.ereaderreadyt.screens.home

import android.media.MediaPlayer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.iremnisabedirbeyoglu.ereaderreadyt.R

@Composable
fun MusicToggle() {
    val context = LocalContext.current
    var isMusicOn by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledTonalIconButton(
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
                .width(44.dp)
                .height(44.dp)
        ) {
            Icon(
                imageVector = if (isMusicOn) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                contentDescription = if (isMusicOn) "Müziği Kapat" else "Müziği Aç"
            )
        }
        Text(
            text = "Müzik",
            style = MaterialTheme.typography.labelSmall
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
}

