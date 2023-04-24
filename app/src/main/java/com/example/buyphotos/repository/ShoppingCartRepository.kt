package com.example.buyphotos.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.buyphotos.database.ShoppingCart
import com.example.buyphotos.database.ShoppingCartDao
import kotlinx.coroutines.flow.Flow

class ShoppingCartRepository(private val shoppingCartDao: ShoppingCartDao) {
    val allShoppingCartItems: Flow<List<ShoppingCart>> = shoppingCartDao.getAll()

    suspend fun insert(shoppingCartItem: ShoppingCart) {
        shoppingCartDao.insert(shoppingCartItem)
    }

    suspend fun update(shoppingCartItem: ShoppingCart) {
        shoppingCartDao.update(shoppingCartItem)
    }

    suspend fun remove(shoppingCartItem: ShoppingCart) {
        shoppingCartDao.remove(shoppingCartItem)
    }

    suspend fun emptyShoppingCart() {
        shoppingCartDao.emptyShoppingCart()
    }
}