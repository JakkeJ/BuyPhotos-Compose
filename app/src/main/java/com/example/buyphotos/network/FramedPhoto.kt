package com.example.buyphotos.network

data class FramedPhoto (
    val imageId: String,
    val imageUrl: String,
    val imageTitle: String,
    val frameType: String,
    val imageSize: String,
    val price: Int,
    val numberOfPhotos: Int
)