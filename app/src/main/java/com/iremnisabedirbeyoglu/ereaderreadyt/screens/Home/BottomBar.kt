package com.iremnisabedirbeyoglu.ereaderreadyt.screens.Home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.graphics.Color

data class NavigationItem(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val route: String)

@Composable
fun BottomBar(navController: NavController) {
    val items = listOf(
        NavigationItem("Kütüphanem", Icons.Default.MoreVert, "library"),
        NavigationItem("Bir Sonraki", Icons.Default.Menu, "next"),
        NavigationItem("Listem", Icons.Default.List, "list"),
        NavigationItem("Ayarlar", Icons.Default.Settings, "settings")
    )

    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color(0xFFEFE6E1) // cozy bir arka plan
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route)
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

        // Müzik butonu
        MusicToggle()
    }
}
