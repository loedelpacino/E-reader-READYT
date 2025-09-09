// path: app/src/main/java/com/iremnisabedirbeyoglu/ereaderreadyt/screens/home/MusicToggle.kt
package com.iremnisabedirbeyoglu.ereaderreadyt.screens.home

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.iremnisabedirbeyoglu.ereaderreadyt.media.MusicPlayerManager

@Composable
fun MusicToggleButton(
    modifier: Modifier = Modifier,
    showLabel: Boolean = false,
    preferredRawNames: List<String> = listOf("cozy_music", "bg_music", "readyt_bg", "bgm", "music")
) {
    val context = LocalContext.current
    val isPlaying by MusicPlayerManager.isPlaying.collectAsState()

    // Dinamik raw arama (derleme hatası önler)
    val resIdState = remember { mutableIntStateOf(findFirstExistingRaw(context, preferredRawNames)) }
    val resId = resIdState.intValue // 0 ise no-op

    Column(modifier = modifier) {
        FilledTonalIconButton(
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                // Çalarken biraz daha “aktif” görünsün
                containerColor = if (isPlaying)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.secondaryContainer
            ),
            onClick = {
                // Tek yerden yönetelim: toggle her iki durumu da ele alır
                MusicPlayerManager.toggle(context, resId)
            }
        ) {
            // 🔧 DÜZELTME: İkon artık DURUMU gösteriyor (eylemi değil)
            Icon(
                imageVector = if (isPlaying) Icons.Filled.MusicNote else Icons.Filled.MusicOff,
                contentDescription = if (isPlaying) "Müzik çalıyor" else "Müzik kapalı"
            )
        }
        if (showLabel) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (isPlaying) "Müzik açık" else "Müzik kapalı",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

private fun findFirstExistingRaw(context: Context, names: List<String>): Int {
    val pkg = context.packageName
    val res = context.resources
    for (name in names) {
        val id = res.getIdentifier(name, "raw", pkg)
        if (id != 0) return id
    }
    return 0
}
