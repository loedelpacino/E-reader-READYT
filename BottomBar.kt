package com.iremnisabedirbeyoglu.ereaderreadyt.screens.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination

data class NavigationItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)

@Composable
fun BottomBar(navController: NavController) {
    val items = listOf(
        NavigationItem("Ana Sayfa", Icons.Default.Home, "dashboard"),
        NavigationItem("Kütüphane", Icons.Default.List, "library"),
        NavigationItem("Ekle", Icons.Default.Add, "add"),
        NavigationItem("Ayarlar", Icons.Default.Settings, "settings")
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(containerColor = Color(0xFFEFE6E1)) {
        items.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        // Tüm bottom tab geçişlerini ana başlangıca göre stabilize et
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

