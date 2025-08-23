package com.iremnisabedirbeyoglu.ereaderreadyt.screens.Home

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.iremnisabedirbeyoglu.ereaderreadyt.screens.AddBookScreen
import com.iremnisabedirbeyoglu.ereaderreadyt.screens.BookListScreen
import com.iremnisabedirbeyoglu.ereaderreadyt.screens.BookReaderScreen
import com.iremnisabedirbeyoglu.ereaderreadyt.screens.SettingsScreen

@Composable
fun HomeScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomBar(navController = navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavigationGraph(navController = navController, modifier = Modifier.padding(innerPadding))
    }
}

@Composable
fun NavigationGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = "library",
        modifier = modifier
    ) {
        composable("library") { BookListScreen(navController) }
        composable("list") { AddBookScreen() }
        composable("settings") { SettingsScreen() }

        // PDF görüntüleyici yönlendirmesi
        composable("reader?uri={uri}") { backStackEntry ->
            val uriArg = backStackEntry.arguments?.getString("uri")
            BookReaderScreen(uriString = uriArg)
        }
    }
}
