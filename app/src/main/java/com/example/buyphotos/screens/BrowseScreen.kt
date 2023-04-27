package com.example.buyphotos.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.example.buyphotos.R
import com.example.buyphotos.components.ArtPhotoApiStatusView
import com.example.buyphotos.model.ArtPhotoApiStatus
import com.example.buyphotos.model.ArtViewModel
import com.example.buyphotos.navigation.BottomBarScreen
import com.example.buyphotos.network.ArtPhoto
import com.example.buyphotos.network.ArtPhotoApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtPhotoCard(artPhoto: ArtPhoto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(),
        onClick = onClick
    ) {
        Column {
            val painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(data = artPhoto.url)
                    .apply(block = fun ImageRequest.Builder
                            .() {
                        crossfade(true)
                        placeholder(R.drawable.loading_animation)
                        error(R.drawable.ic_broken_image)
                            }).build()
            )
            Image(
                painter = painter,
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
    val title = stringResource(R.string.browseTitle)
    LaunchedEffect(Unit){
        viewModel.setTitle(title)
    }
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
        ArtPhotoGrid(artPhotos = artPhotos.value, navController, viewModel)
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
