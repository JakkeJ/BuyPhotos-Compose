package com.example.buyphotos

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.buyphotos.model.ArtViewModel
import com.example.buyphotos.navigation.BottomBarScreen
import com.example.buyphotos.navigation.BottomNavGraph
import kotlin.reflect.jvm.internal.impl.types.checker.TypeRefinementSupport.Enabled

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MainScreen(viewModel: ArtViewModel) {
    val navController = rememberNavController()
    val title = viewModel.screenTitle.collectAsState().value
    Scaffold(
        topBar = {
            topAppBar(title)
        } ,
        bottomBar = { BottomBar(navController = navController, viewModel = viewModel) }
    )
    { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            BottomNavGraph(navController = navController, viewModel = viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun topAppBar(title: String) {
    TopAppBar(
        title = {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp), Arrangement.SpaceBetween) {
                Text(
                    text = stringResource(R.string.app_name),
                    modifier = Modifier,
                    textAlign = TextAlign.Start
                )
                Text(
                    text = title,
                    textAlign = TextAlign.End
                )
            }
        },
        modifier = Modifier
            .background(MaterialTheme.colorScheme.onSurface)
    )
}

@Composable
fun BottomBar(navController: NavHostController, viewModel: ArtViewModel) {
    val screens = listOf(
        BottomBarScreen.ShoppingCart,
        BottomBarScreen.Browse,
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        screens.forEach { screen ->
            AddItem(
                screen = screen,
                currentDestination = currentDestination,
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RowScope.AddItem(
    screen: BottomBarScreen,
    currentDestination: NavDestination?,
    navController: NavHostController,
    viewModel: ArtViewModel
) {
    NavigationBarItem(
        icon = {
            BadgedBox(badge = {
                if (viewModel.totalNumberOfPhotos.collectAsState().value > 0 && screen.title == "Shopping Cart") {
                    Badge {
                        Text("${viewModel.totalNumberOfPhotos.collectAsState().value}")
                    }
                }
            }) {
                Icon(
                    imageVector = screen.icon,
                    contentDescription = "Navigation Icon",
                    modifier = Modifier
                        .size(40.dp)
                )
            }
        },
        selected = currentDestination?.hierarchy?.any {
            it.route == screen.route
        } == true,
        onClick = {
            navController.navigate(screen.route) {
                popUpTo(navController.graph.findStartDestination().id)
                launchSingleTop = true
            }
        }
    )
}