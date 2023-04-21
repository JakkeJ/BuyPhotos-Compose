package com.example.buyphotos.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.buyphotos.database.ShoppingCart
import com.example.buyphotos.model.ArtPhotoApiStatus
import com.example.buyphotos.model.ArtViewModel

@Composable
fun ShoppingCartScreen(viewModel: ArtViewModel, navController: NavController) {
    val artPhotos = viewModel.dbShoppingCart
    val apiStatus = remember { mutableStateOf(ArtPhotoApiStatus.LOADING) }
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ShoppingCartList(navController = navController, viewModel = viewModel)
    }
}

@Composable
fun ShoppingCartList(navController: NavController, viewModel: ArtViewModel) {
    val artPhotos = viewModel.dbShoppingCartFlow
    for (i in artPhotos.value){
        println(i.imageTitle)
    }
    LazyColumn(
        contentPadding = PaddingValues(8.dp)
    ) {
        items(artPhotos.value.size) { index ->
            val artPhoto = artPhotos[index]
            ShoppingCartPhotoCard(artPhoto)
            }
        }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ShoppingCartPhotoCard(artPhoto: ShoppingCart) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column {
            Image(
                painter = rememberAsyncImagePainter(artPhoto.imageUrl),
                contentDescription = "Art Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
        }
    }
}