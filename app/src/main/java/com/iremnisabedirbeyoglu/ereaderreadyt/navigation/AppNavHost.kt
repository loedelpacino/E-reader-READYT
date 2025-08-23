package com.iremnisabedirbeyoglu.ereaderreadyt.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.iremnisabedirbeyoglu.ereaderreadyt.screens.AddBookScreen
import com.iremnisabedirbeyoglu.ereaderreadyt.screens.BookListScreen
import com.iremnisabedirbeyoglu.ereaderreadyt.screens.BookReaderScreen
import com.iremnisabedirbeyoglu.ereaderreadyt.screens.SettingsScreen
import com.iremnisabedirbeyoglu.ereaderreadyt.screens.Home.HomeScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            HomeScreen()
        }
        composable("reader?uri={uri}") { backStackEntry ->
            val uriArg = backStackEntry.arguments?.getString("uri")
            BookReaderScreen(uriString = uriArg)
        }

        // Eğer giriş ekranı, splash, onboarding gibi ekranlar eklenecekse burada yer alabilir.
        // Şimdilik sadece HomeScreen'e yönlendiriyoruz çünkü uygulama girişsiz, direkt başlıyor.
    }
}
