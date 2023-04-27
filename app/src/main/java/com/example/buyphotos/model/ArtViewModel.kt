package com.example.buyphotos.model

import android.content.Intent
import androidx.lifecycle.LiveData
import android.text.Editable
import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.example.buyphotos.repository.ShoppingCartRepository
import com.example.buyphotos.database.ShoppingCart
import com.example.buyphotos.network.ArtAlbum
import com.example.buyphotos.network.ArtArtist
import com.example.buyphotos.network.ArtPhoto
import com.example.buyphotos.repository.ArtPhotoRepository
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.buyphotos.MainActivity
import com.example.buyphotos.R
import com.example.buyphotos.network.FramedPhoto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

const val BASE_IMAGE_PRICE: Int = 100

const val SMALL_IMAGE_PRICE: Int = 0
const val MEDIUM_IMAGE_PRICE: Int = 80
const val LARGE_IMAGE_PRICE: Int = 150

const val WOOD_FRAME_PRICE: Int = 0
const val SILVER_FRAME_PRICE: Int = 50
const val GOLD_FRAME_PRICE: Int = 120

enum class ArtPhotoApiStatus { LOADING, ERROR, DONE }

class ArtViewModel(
    private val shoppingCartRepository: ShoppingCartRepository,
    private val artPhotoRepository: ArtPhotoRepository
) : ViewModel() {

    private val _dbShoppingCart = MutableStateFlow<List<ShoppingCart>>(emptyList())
    val dbShoppingCart: StateFlow<List<ShoppingCart>> = _dbShoppingCart

    private val _status = MutableStateFlow<ArtPhotoApiStatus?>(null)
    val status: StateFlow<ArtPhotoApiStatus?> = _status

    private val _photos = MutableStateFlow<List<ArtPhoto>>(emptyList())
    val photos: StateFlow<List<ArtPhoto>> = _photos

    private val _artists = MutableStateFlow<List<ArtArtist>>(emptyList())
    val artists: StateFlow<List<ArtArtist>> = _artists

    private val _albums = MutableStateFlow<List<ArtAlbum>>(emptyList())
    val albums: StateFlow<List<ArtAlbum>> = _albums

    private val _frame = MutableStateFlow("Treramme")
    val frame: StateFlow<String> = _frame

    private val _chosenImageSize = MutableStateFlow("Liten")
    val chosenImageSize: StateFlow<String> = _chosenImageSize

    private val _price = MutableStateFlow<Int>(0)
    val price: StateFlow<Int> = _price

    private val _orderPrice = MutableStateFlow<String>("")
    val orderPrice: StateFlow<String> = _orderPrice

    private val _picture = MutableStateFlow<ArtPhoto?>(null)
    val picture: StateFlow<ArtPhoto?> = _picture

    private val _totalNumberOfPhotos = MutableStateFlow<Int>(0)
    val totalNumberOfPhotos: StateFlow<Int> = _totalNumberOfPhotos

    private val _numberOfPhotos = MutableStateFlow<Int>(0)
    val numberOfPhotos: StateFlow<Int> = _numberOfPhotos

    private val _artistName = MutableStateFlow<String>("")
    val artistName: StateFlow<String> = _artistName

    private val _artistEmail = MutableStateFlow<String>("")
    val artistEmail: StateFlow<String> = _artistEmail

    private val _basketTotalPrice = MutableStateFlow(0)
    val basketTotalPrice: StateFlow<Int> = _basketTotalPrice

    private val _screenTitle = MutableStateFlow("")
    val screenTitle: StateFlow<String> = _screenTitle

    var updateCount by mutableStateOf(0)

    init{
        getArtContent()
        setFrameAndImageSizeOptions()
        resetViewModel()
        fetchShoppingCartItems()
        updateShoppingCartData()
    }

    fun setTitle(newTitle: String) {
        _screenTitle.value = newTitle
    }

    fun updateShoppingCartData() {
        resetViewModel()
        var amount = 0
        var price = 0
        for (i in dbShoppingCart.value) {
            amount += i.amount
            price += (i.price * i.amount)
        }
        _totalNumberOfPhotos.value = amount
        _basketTotalPrice.value = price
        updateCount++
    }

    private fun fetchShoppingCartItems() {
        viewModelScope.launch {
            shoppingCartRepository.allShoppingCartItems.collect { shoppingCartItems ->
                _dbShoppingCart.value = shoppingCartItems
            }
        }
        updateShoppingCartData()
    }

    fun resetViewModel() {
        _basketTotalPrice.value = 0
        _totalNumberOfPhotos.value = 0
        updateCount++
    }

    fun getBorderColor(frame: String?): Color {
        val brown = Color(150, 75, 0)
        val gold = Color(225,185,0)
        val silver = Color(100,100,100)
        return when (frame) {
            "Treramme"-> brown
            "Sølvramme" -> silver
            "Gullramme" -> gold
            else -> Color.Black
        }
    }

    fun addToBasket(photo: ShoppingCart) {
        if (dbShoppingCart.value!!.isNotEmpty()) {
            var foundMatchingItem = false
            for (i in dbShoppingCart.value!!) {
                if (
                    i.imageId == photo.imageId &&
                    i.frameType == photo.frameType &&
                    i.imageSize == photo.imageSize
                ) {
                    foundMatchingItem = true
                    val currentPhoto = i.copy()
                    if (photo.amount > 1){
                        currentPhoto.amount += photo.amount
                    } else {
                        currentPhoto.amount += 1
                    }
                    var newNumberOfPhotos: Int = totalNumberOfPhotos.value!!
                    newNumberOfPhotos += 1
                    _totalNumberOfPhotos.value = newNumberOfPhotos
                    var newTotalPriceAmount: Int = basketTotalPrice.value!!
                    newTotalPriceAmount += photo.price
                    _basketTotalPrice.value = newTotalPriceAmount
                    viewModelScope.launch {
                        shoppingCartRepository.update(currentPhoto)
                    }
                    updateShoppingCartData()
                    break
                }
            }
            if (!foundMatchingItem) {
                insertShoppingCartItem(photo)
            }
        } else {
            insertShoppingCartItem(photo)
        }
    }

    fun increasePhotoAmount(photo: ShoppingCart) {
        photo.amount += 1
        viewModelScope.launch {
            shoppingCartRepository.update(photo)
            updateShoppingCartData()
        }

    }

    fun removePhotoFromDb(photo: ShoppingCart) {
        viewModelScope.launch {
            shoppingCartRepository.remove(photo)
            updateShoppingCartData()
        }
    }

    fun decreasePhotoAmount(photo: ShoppingCart){
        if (photo.amount > 1) {
            photo.amount -= 1
            viewModelScope.launch {
                shoppingCartRepository.update(photo)
                updateShoppingCartData()
            }
        } else if (photo.amount == 1){
            photo.amount = 0
            photo.price = 0
            removePhotoFromDb(photo)
        }
    }

    private fun getArtContent() {
        viewModelScope.launch {
            _status.value = ArtPhotoApiStatus.LOADING
            try{
                _photos.value = artPhotoRepository.getPhotos()
                _artists.value = artPhotoRepository.getArtists()
                _albums.value = artPhotoRepository.getAlbums()
                _status.value = ArtPhotoApiStatus.DONE
            } catch (e: Exception) {
                _status.value = ArtPhotoApiStatus.ERROR
            }
        }
    }

    fun setFrame(frameChoice: String) {
        _frame.value = frameChoice
        calculatePrice()
    }

    fun setImageSize(imageSizeChoice: String) {
        _chosenImageSize.value = imageSizeChoice
        calculatePrice()
    }

    fun calculatePrice() {
        val framePrice = when (frame.value) {
            "Treramme" -> WOOD_FRAME_PRICE
            "Sølvramme" -> SILVER_FRAME_PRICE
            "Gullramme" -> GOLD_FRAME_PRICE
            else -> 0
        }
        val imageSizePrice = when (chosenImageSize.value) {
            "Liten" -> SMALL_IMAGE_PRICE
            "Medium" -> MEDIUM_IMAGE_PRICE
            "Stort" -> LARGE_IMAGE_PRICE
            else -> 0
        }

        _price.value = BASE_IMAGE_PRICE + framePrice + imageSizePrice
        _orderPrice.value = (price.value * numberOfPhotos.value).toString()
    }

    fun setFrameAndImageSizeOptions() {
        _numberOfPhotos.value = 1
        _frame.value = "Treramme"
        _chosenImageSize.value = "Liten"
        calculatePrice()
    }

    fun setPicture(photo: ArtPhoto) {
        _picture.value = photo
        findAlbumId()
    }

    fun findAlbumId() {
        val albumId = picture.value!!.albumId
        val id = albumId.toInt()
        findArtistId(id)
    }

    fun findArtistId(id: Int) {
        val index = id -1
        if (albums.value.isNotEmpty()) {
            val artistId = albums.value[index].userId
            val id = artistId.toInt()
            findArtistInfo(id)
        }
    }

    fun findArtistInfo(id: Int){
        val index = id -1
        val artistName: String = artists.value[index].name
        val artistEmail: String = artists.value[index].email
        _artistName.value = artistName
        _artistEmail.value = artistEmail
    }

    private fun insertShoppingCartItem(shoppingCartItem: ShoppingCart) {
        viewModelScope.launch {
            shoppingCartRepository.insert(shoppingCartItem)
        }
        updateShoppingCartData()
    }

    fun emptyShoppingCart() {
        viewModelScope.launch {
            shoppingCartRepository.emptyShoppingCart()
        }
        updateShoppingCartData()
    }

    suspend fun getShoppingCartItems(): List<ShoppingCart> {
        return dbShoppingCart.first()
    }
}



class ArtViewModelFactory(
    private val shoppingCartRepository: ShoppingCartRepository,
    private val artPhotoRepository: ArtPhotoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArtViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ArtViewModel(shoppingCartRepository, artPhotoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
