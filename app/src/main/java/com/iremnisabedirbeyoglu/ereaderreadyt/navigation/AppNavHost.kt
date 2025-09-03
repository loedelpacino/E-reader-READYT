package com.iremnisabedirbeyoglu.ereaderreadyt.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.iremnisabedirbeyoglu.ereaderreadyt.screens.*
import com.iremnisabedirbeyoglu.ereaderreadyt.screens.BottomBar

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Scaffold(
        bottomBar = { BottomBar(navController = navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = modifier.padding(innerPadding)
        ) {
            composable("dashboard") { DashboardScreen(navController) }
            composable("library") { BookListScreen(navController) }
            composable("add") { AddBookScreen(navController) }
            composable("settings") { SettingsScreen() }

            // Reader: uri opsiyonel string argüman
            composable(
                route = "reader?uri={uri}",
                arguments = listOf(
                    navArgument("uri") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val uriArg = backStackEntry.arguments?.getString("uri")
                BookReaderScreen(
                    uriString = uriArg,
                    onBack = { navController.popBackStack() } // 🔙 geri davranışı
                )
            }
        }
    }
}

