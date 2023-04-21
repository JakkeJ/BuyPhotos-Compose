package com.example.buyphotos.model

import androidx.lifecycle.LiveData
import android.text.Editable
import androidx.lifecycle.*
import com.example.buyphotos.repository.ShoppingCartRepository
import com.example.buyphotos.database.ShoppingCart
import com.example.buyphotos.network.ArtAlbum
import com.example.buyphotos.network.ArtArtist
import com.example.buyphotos.network.ArtPhoto
import com.example.buyphotos.repository.ArtPhotoRepository
import kotlinx.coroutines.launch
import androidx.compose.runtime.toMutableStateList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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

    val dbShoppingCart = (shoppingCartRepository.allShoppingCartItems).toMutableStateList()

    private val _dbShoppingCartFlow = MutableStateFlow(dbShoppingCart)

    val dbShoppingCartFlow: StateFlow<List<ShoppingCart>> get() = _dbShoppingCartFlow

    private val _status = MutableLiveData<ArtPhotoApiStatus>()
    val status: LiveData<ArtPhotoApiStatus> = _status

    private val _photos = MutableLiveData<List<ArtPhoto>>()
    val photos: LiveData<List<ArtPhoto>> = _photos

    private val _artists = MutableLiveData<List<ArtArtist>>()
    val artists: LiveData<List<ArtArtist>> = _artists

    private val _albums = MutableLiveData<List<ArtAlbum>>()
    val albums: LiveData<List<ArtAlbum>> = _albums

    private val _frame = MutableLiveData<String>()
    val frame: LiveData<String> = _frame

    private val _chosenImageSize = MutableLiveData<String>()
    val chosenImageSize: LiveData<String> = _chosenImageSize

    private val _price = MutableLiveData<Int>()
    val price: LiveData<Int> = _price

    private val _orderPrice = MutableLiveData<String>()
    val orderPrice: LiveData<String> = _orderPrice

    private val _picture = MutableLiveData<ArtPhoto>()
    val picture: LiveData<ArtPhoto> = _picture

    private val _totalNumberOfPhotos = MutableLiveData<Int>()
    val totalNumberOfPhotos: LiveData<Int> = _totalNumberOfPhotos

    private val _numberOfPhotos = MutableLiveData<Int>()
    val numberOfPhotos: LiveData<Int> = _numberOfPhotos

    private val _artistName = MutableLiveData<String>()
    val artistName: LiveData<String> = _artistName

    private val _artistEmail = MutableLiveData<String>()
    val artistEmail: LiveData<String> = _artistEmail

    private val _basketSize = MutableLiveData<Int>()
    val basketSize: LiveData<Int> = _basketSize

    private val _basketTotalPrice = MutableLiveData<Int>()
    val basketTotalPrice: LiveData<Int> = _basketTotalPrice




    init{
        getArtContent()
        setFrameAndImageSizeOptions()
        resetViewModel()
    }

    fun resetViewModel() {
        _basketTotalPrice.value = 0
        _totalNumberOfPhotos.value = 0
    }

    fun addToBasket(photo: ShoppingCart) {
        println("MOrdi: ${ photo }")
        if (dbShoppingCart.isNotEmpty()) {
            println(dbShoppingCart)
            var foundMatchingItem = false
            for (i in dbShoppingCart) {
                if (
                    i.imageId == photo.imageId &&
                    i.frameType == photo.frameType &&
                    i.imageSize == photo.imageSize
                ) {
                    foundMatchingItem = true
                    val currentPhoto = i.copy()
                    currentPhoto.amount += 1
                    var newNumberOfPhotos: Int = totalNumberOfPhotos.value!!
                    newNumberOfPhotos += 1
                    _totalNumberOfPhotos.value = newNumberOfPhotos
                    var newTotalPriceAmount: Int = basketTotalPrice.value!!
                    newTotalPriceAmount += photo.price
                    _basketTotalPrice.value = newTotalPriceAmount
                    viewModelScope.launch {
                        shoppingCartRepository.update(currentPhoto)
                    }
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

    fun setNumberOfPhotos(numberChoice: Editable?) {
        if (numberChoice.toString().toIntOrNull() == null){
            _numberOfPhotos.value = 1
            calculatePrice()
        } else {
            _numberOfPhotos.value = numberChoice.toString().toInt()
            calculatePrice()
        }
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
        _orderPrice.value = (price.value!!.toInt() * numberOfPhotos.value!!.toInt()).toString()
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
        val artistId = albums.value!!.get(index).userId
        val id = artistId.toInt()
        findArtistInfo(id)
    }

    fun findArtistInfo(id: Int){
        val index = id -1
        val artistName: String = artists.value!!.get(index).name
        val artistEmail: String = artists.value!!.get(index).email
        _artistName.value = artistName
        _artistEmail.value = artistEmail
    }

    private fun insertShoppingCartItem(shoppingCartItem: ShoppingCart) {
        viewModelScope.launch {
            shoppingCartRepository.insert(shoppingCartItem)
        }
    }

    fun emptyShoppingCart() {
        viewModelScope.launch {
            shoppingCartRepository.emptyShoppingCart()
        }
    }

    fun removePhotoFromDb(photo: ShoppingCart) {

        photo.amount -= 1
        var newNumberOfPhotos: Int = totalNumberOfPhotos.value!!
        newNumberOfPhotos -= 1
        _totalNumberOfPhotos.value = newNumberOfPhotos

        var newTotalPriceAmount: Int = basketTotalPrice.value!!
        newTotalPriceAmount -= photo.price
        _basketTotalPrice.value = newTotalPriceAmount

        if (photo.amount != 0) {
            viewModelScope.launch {
                shoppingCartRepository.update(photo)
            }
        } else {
            viewModelScope.launch {
                shoppingCartRepository.remove(photo)
            }
        }
    }

    fun getNewPrice() {
        var newPrice: Int = 0
        var newAmount: Int = 0

        for (photos in dbShoppingCart) {
            newPrice += (photos.amount * photos.price)
            newAmount += photos.amount
        }
        _basketTotalPrice.value = newPrice
        _totalNumberOfPhotos.value = newAmount
        _frame.value = "Treramme"
        _chosenImageSize.value = "Liten"
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
