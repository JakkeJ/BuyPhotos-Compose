package com.example.buyphotos.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.buyphotos.model.ArtViewModel
import com.example.buyphotos.model.ArtViewModelFactory
import com.example.buyphotos.screens.*

@Composable
fun BottomNavGraph(navController: NavHostController, viewModel: ArtViewModel) {
    NavHost(
        navController = navController,
        startDestination = BottomBarScreen.ShoppingCart.route
    ) {
        composable(route = BottomBarScreen.ShoppingCart.route) {
            ShoppingCartScreen(viewModel = viewModel)
        }
        composable(route = BottomBarScreen.Browse.route) {
            BrowseScreen(viewModel = viewModel, navController)
        }
        composable(
            route = BottomBarScreen.Order.route + "/{imageId}",
            arguments = listOf(
                navArgument("imageId") {
                    type = NavType.IntType
                    defaultValue = 1
                }
            )
        ) {entry ->
            OrderScreen(imageId = entry.arguments!!.getInt("imageId"), viewModel = viewModel)
        }
    }
}