// path: app/src/main/java/com/iremnisabedirbeyoglu/ereaderreadyt/MainActivity.kt
package com.iremnisabedirbeyoglu.ereaderreadyt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.iremnisabedirbeyoglu.ereaderreadyt.media.MusicPlayerManager
import com.iremnisabedirbeyoglu.ereaderreadyt.navigation.AppNavHost
import com.iremnisabedirbeyoglu.ereaderreadyt.ui.theme.ReadyttTheme
import com.iremnisabedirbeyoglu.ereaderreadyt.util.PageSfx
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // Hilt yoksa bu satırı ve import'u kaldır
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Sayfa çevirme SFX (raw/page_flip bulunmazsa no-op)
        PageSfx.init(this)

        setContent {
            ReadyttTheme {
                val navController = rememberNavController()
                AppNavHost(navController = navController)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MusicPlayerManager.stop()
        PageSfx.release()
    }
}
