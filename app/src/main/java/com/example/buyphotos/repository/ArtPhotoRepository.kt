package com.example.buyphotos.repository

import com.example.buyphotos.network.ArtAlbum
import com.example.buyphotos.network.ArtArtist
import com.example.buyphotos.network.ArtPhoto
import com.example.buyphotos.network.ArtPhotoApiService

class ArtPhotoRepository(private val artPhotoApiService: ArtPhotoApiService) {

    suspend fun getPhotos(): List<ArtPhoto> {
        return artPhotoApiService.getPhotos()
    }

    suspend fun getArtists(): List<ArtArtist> {
        return artPhotoApiService.getArtists()
    }

    suspend fun getAlbums(): List<ArtAlbum> {
        return artPhotoApiService.getAlbums()
    }
}