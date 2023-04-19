package com.example.buyphotos.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil.compose.rememberImagePainter
import com.example.buyphotos.R
import com.example.buyphotos.model.ArtPhotoApiStatus

@Composable
fun ArtPhotoApiStatusView(status: ArtPhotoApiStatus) {
    when (status) {
        ArtPhotoApiStatus.LOADING -> {
            Image(
                painter = rememberImagePainter(
                    data = R.drawable.loading_animation,
                    builder = {
                        crossfade(true)
                    }
                ),
                contentDescription = "Loading animation",
                modifier = Modifier.fillMaxSize()
            )
        }
        ArtPhotoApiStatus.ERROR -> {
            Image(
                painter = rememberImagePainter(
                    data = R.drawable.ic_connection_error,
                    builder = {
                        crossfade(true)
                    }
                ),
                contentDescription = "Error icon",
                modifier = Modifier.fillMaxSize()
            )
        }
        ArtPhotoApiStatus.DONE -> {
            // Do nothing when the status is DONE
        }
    }
}