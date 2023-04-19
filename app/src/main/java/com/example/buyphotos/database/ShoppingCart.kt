package com.example.buyphotos.database

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_cart")
data class ShoppingCart(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @NonNull @ColumnInfo(name = "image_id") val imageId: String,
    @NonNull @ColumnInfo(name = "image_url") val imageUrl: String,
    @NonNull @ColumnInfo(name = "image_title") val imageTitle: String,
    @NonNull @ColumnInfo(name = "frame_type") val frameType: String,
    @NonNull @ColumnInfo(name = "image_size") val imageSize: String,
    @NonNull @ColumnInfo val price: Int,
    @NonNull @ColumnInfo(name = "amount") var amount: Int
)

// TODO: Convert database item into FramedPhoto object
fun ShoppingCart.subTotalPrice(): Int {
    return (price * amount)
}
