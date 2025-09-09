// path: app/src/main/java/com/iremnisabedirbeyoglu/ereaderreadyt/screens/home/BottomBar.kt
package com.iremnisabedirbeyoglu.ereaderreadyt.screens.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

data class NavigationItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)

@Composable
fun BottomBar(navController: NavController) {
    val items = listOf(
        NavigationItem("Ana Sayfa", Icons.Filled.Home, "dashboard"),
        // Not: AppNavHost'ta kütüphane rotası "bookList" olarak tanımlı.
        NavigationItem("Kütüphane", Icons.Filled.Book, "bookList"),
        NavigationItem("Ekle", Icons.Filled.Add, "add"),
        NavigationItem("Ayarlar", Icons.Filled.Settings, "settings")
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    // Reader ekranında alt bar'ı gizle (örn: "reader?uri=...")
    if (currentRoute.startsWith("reader")) return

    NavigationBar(containerColor = Color(0xFFEFE6E1)) {
        items.forEach { item ->
            val selected =
                currentDestination?.hierarchy?.any { it.route == item.route } == true

            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF4E342E),
                    selectedTextColor = Color(0xFF4E342E),
                    indicatorColor = Color(0xFFA1887F)
                )
            )
        }
    }
}
