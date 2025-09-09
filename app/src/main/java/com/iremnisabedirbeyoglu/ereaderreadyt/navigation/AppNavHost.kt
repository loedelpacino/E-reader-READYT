// path: app/src/main/java/com/iremnisabedirbeyoglu/ereaderreadyt/navigation/AppNavHost.kt
package com.iremnisabedirbeyoglu.ereaderreadyt.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.iremnisabedirbeyoglu.ereaderreadyt.screens.AddBookScreen
import com.iremnisabedirbeyoglu.ereaderreadyt.screens.BookListScreen
import com.iremnisabedirbeyoglu.ereaderreadyt.screens.BookReaderScreen
import com.iremnisabedirbeyoglu.ereaderreadyt.screens.SettingsScreen
import com.iremnisabedirbeyoglu.ereaderreadyt.screens.home.BottomBar
import com.iremnisabedirbeyoglu.ereaderreadyt.screens.home.DashboardScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Reader ekranında alt bar'ı gizleyelim
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""
    val showBottomBar = !currentRoute.startsWith("reader")

    Scaffold(
        bottomBar = {
            if (showBottomBar) BottomBar(navController = navController)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = modifier.padding(innerPadding)
        ) {
            composable("dashboard") { DashboardScreen(navController) }

            // Kütüphane
            composable("bookList") { BookListScreen(navController) }

            // Ayarlar
            composable("settings") { SettingsScreen(navController) }

            // --- Ekle ekranı ---
            // Asıl rota
            composable(
                route = "addBook",
                deepLinks = listOf(
                    // Bazı yerlerde NavDeepLinkRequest ile "android-app://androidx.navigation/add" çağrılıyor olabilir.
                    navDeepLink { uriPattern = "android-app://androidx.navigation/add" }
                )
            ) { AddBookScreen(navController) }

            // Alias rota (projedeki farklı çağrılar için)
            composable("add") { AddBookScreen(navController) }

            // --- Reader ekranı ---
            composable(
                route = "reader?uri={uri}",
                arguments = listOf(
                    navArgument("uri") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStack ->
                val uriArg = backStack.arguments?.getString("uri")
                BookReaderScreen(
                    uriString = uriArg,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
