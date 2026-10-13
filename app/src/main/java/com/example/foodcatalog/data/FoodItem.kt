package com.example.foodcatalog.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foods")
data class FoodItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val description: String,
    val price: Long,
    val emoji: String,
    val isFavorite: Boolean = false,
)
