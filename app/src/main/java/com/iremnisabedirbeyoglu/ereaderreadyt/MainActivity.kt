package com.iremnisabedirbeyoglu.ereaderreadyt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.iremnisabedirbeyoglu.ereaderreadyt.navigation.AppNavHost
import com.iremnisabedirbeyoglu.ereaderreadyt.ui.theme.ReadyttTheme
import dagger.hilt.android.AndroidEntryPoint // Eğer Hilt kullanıyorsan bunu aktif tut

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReadyttTheme {
                val navController = rememberNavController()
                AppNavHost(navController = navController)
            }
        }
    }
}
