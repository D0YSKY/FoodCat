package com.example.foodcatalog.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods ORDER BY name ASC") fun getAll(): List<FoodItem>
    @Query("SELECT * FROM foods WHERE isFavorite = 1 ORDER BY name ASC") fun getFavorites(): List<FoodItem>
    @Query("SELECT * FROM foods WHERE id = :id LIMIT 1") fun getById(id: Int): FoodItem?
    @Insert fun insert(food: FoodItem): Long
    @Update fun update(food: FoodItem)
    @Delete fun delete(food: FoodItem)
    @Query("UPDATE foods SET isFavorite = :favorite WHERE id = :id") fun setFavorite(id: Int, favorite: Boolean)

    @Query("SELECT * FROM cart ORDER BY foodId") fun getCart(): List<CartItem>
    @Query("SELECT * FROM cart WHERE foodId = :foodId LIMIT 1") fun getCartItem(foodId: Int): CartItem?
    @Insert(onConflict = OnConflictStrategy.REPLACE) fun saveCartItem(item: CartItem)
    @Query("DELETE FROM cart WHERE foodId = :foodId") fun removeCartItem(foodId: Int)
    @Query("DELETE FROM cart") fun clearCart()
}
