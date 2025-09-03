package com.iremnisabedirbeyoglu.ereaderreadyt.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.iremnisabedirbeyoglu.ereaderreadyt.screens.*
import com.iremnisabedirbeyoglu.ereaderreadyt.screens.home.BottomBar

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

            composable("reader?uri={uri}") { backStackEntry ->
                val uriArg = backStackEntry.arguments?.getString("uri")
                BookReaderScreen(uriString = uriArg)
            }
        }
    }
}
