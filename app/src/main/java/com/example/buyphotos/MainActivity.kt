package com.example.buyphotos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.buyphotos.model.ArtViewModel
import com.example.buyphotos.model.ArtViewModelFactory
import com.example.buyphotos.database.ArtPhotoApplication
import com.example.buyphotos.ui.theme.BuyPhotosTheme


class MainActivity : ComponentActivity() {
    private val sharedViewModel: ArtViewModel by viewModels {
        val shoppingCartRepository = (application as ArtPhotoApplication).shoppingCartRepository
        val artPhotoRepository = (application as ArtPhotoApplication).artPhotoRepository
        ArtViewModelFactory(shoppingCartRepository, artPhotoRepository)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BuyPhotosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(sharedViewModel)
                }
            }
        }
    }
}