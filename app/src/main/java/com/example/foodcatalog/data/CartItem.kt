package com.example.foodcatalog.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart")
data class CartItem(
    @PrimaryKey val foodId: Int,
    val quantity: Int,
)
