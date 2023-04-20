package com.example.buyphotos.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.buyphotos.components.ArtPhotoApiStatusView
import com.example.buyphotos.database.ShoppingCart
import com.example.buyphotos.model.ArtPhotoApiStatus
import com.example.buyphotos.model.ArtViewModel
import com.example.buyphotos.navigation.BottomBarScreen
import com.example.buyphotos.network.ArtPhoto
import com.example.buyphotos.network.ArtPhotoApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ArtPhotoCard(artPhoto: ArtPhoto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(),
        elevation = 4.dp,
        onClick = onClick
    ) {
        Column {
            Image(
                painter = rememberAsyncImagePainter(artPhoto.url),
                contentDescription = "Art Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
        }
    }
}

@Composable
fun BrowseScreen(viewModel: ArtViewModel, navController: NavHostController) {
    val artPhotos = remember { mutableStateOf(emptyList<ArtPhoto>()) }
    val apiStatus = remember { mutableStateOf(ArtPhotoApiStatus.LOADING) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val fetchedArtPhotos = fetchArtPhotos()
                artPhotos.value = fetchedArtPhotos
                apiStatus.value = ArtPhotoApiStatus.DONE
            } catch (e: Exception) {
                apiStatus.value = ArtPhotoApiStatus.ERROR
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Display your ArtPhotoGrid with the fetched ArtPhoto objects
        ArtPhotoGrid(artPhotos = artPhotos.value, navController, viewModel)

        // Display the appropriate animation or image based on the current ArtPhotoApiStatus
        ArtPhotoApiStatusView(status = apiStatus.value)
    }
}

@Composable
fun ArtPhotoGrid(artPhotos: List<ArtPhoto>, navController: NavHostController, viewModel: ArtViewModel) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(artPhotos.size) { index ->
            val artPhoto = artPhotos[index]
            ArtPhotoCard(artPhoto) {
                viewModel.setPicture(artPhoto)
                navController.navigate("${BottomBarScreen.Order.route}/${artPhoto.id}") {
                    launchSingleTop = true
                }
            }
        }
    }
}

suspend fun fetchArtPhotos(): List<ArtPhoto> {
    return ArtPhotoApi.retrofitService.getPhotos()
}
