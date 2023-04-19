package com.example.buyphotos.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

private const val BASE_URL =
    "https://jsonplaceholder.typicode.com/"

/**
 * Build the Moshi object with Kotlin adapter factory that Retrofit will be using.
 */
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

/**
 * The Retrofit object with the Moshi converter.
 */
private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface ArtPhotoApiService {
    @GET("photos")
    suspend fun getPhotos(): List<ArtPhoto>

    @GET("photos/{id}")
    suspend fun getPhotoById(@Path("id") id: Int): ArtPhoto

    @GET("users")
    suspend fun getArtists(): List<ArtArtist>

    @GET("albums")
    suspend fun getAlbums(): List<ArtAlbum>
}

/**
 * A public Api object that exposes the lazy-initialized Retrofit service
 */
object ArtPhotoApi {
    val retrofitService: ArtPhotoApiService by lazy {
        retrofit.create(ArtPhotoApiService::class.java)
    }
}