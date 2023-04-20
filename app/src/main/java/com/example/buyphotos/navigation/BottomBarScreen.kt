package com.example.buyphotos.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object ShoppingCart : BottomBarScreen(
        route = "shopping_cart",
        title = "Shopping Cart",
        icon = Icons.Default.ShoppingCart,
    )

    object Browse : BottomBarScreen(
        route = "browse",
        title = "Browse",
        icon = Icons.Default.GridView,
    )

    object Order : BottomBarScreen(
        route = "order",
        title = "Order",
        icon = Icons.Default.List,
    )
}