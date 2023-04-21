package com.example.buyphotos.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingCartDao {
    @Query("SELECT * FROM shopping_cart")
    fun getAll(): List<ShoppingCart>

    @Query("DELETE FROM shopping_cart")
    suspend fun emptyShoppingCart()

    @Query("SELECT * FROM shopping_cart WHERE id = :index")
    fun getPhoto(index: Int): ShoppingCart

    @Query("SELECT COUNT(*) FROM shopping_cart")
    suspend fun countDatabase(): Int

    @Insert
    suspend fun insert(shoppingCartItem: ShoppingCart)

    @Delete
    suspend fun remove(shoppingCartItem: ShoppingCart)

    @Update
    suspend fun update(shoppingCartItem: ShoppingCart)
}