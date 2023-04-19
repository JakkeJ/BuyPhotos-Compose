package com.example.buyphotos.database

import android.app.Application
import com.example.buyphotos.network.ArtPhotoApi
import com.example.buyphotos.repository.ArtPhotoRepository
import com.example.buyphotos.repository.ShoppingCartRepository

class ArtPhotoApplication : Application() {
    val database: AppDatabase by lazy {AppDatabase.getDatabase(this)}
    val shoppingCartRepository by lazy { ShoppingCartRepository(database.shoppingCartDao()) }
    val artPhotoRepository by lazy { ArtPhotoRepository(ArtPhotoApi.retrofitService) }
}