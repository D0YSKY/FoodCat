package com.example.foodcatalog.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodcatalog.R
import com.example.foodcatalog.data.FoodItem
import java.text.NumberFormat
import java.util.Locale

class FoodAdapter(
    private val onClick: (FoodItem) -> Unit,
    private val onFavorite: (FoodItem) -> Unit,
    private val onAddCart: (FoodItem) -> Unit,
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {
    private val items = mutableListOf<FoodItem>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newItems: List<FoodItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder = FoodViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_food, parent, false),
    )

    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) = holder.bind(items[position])

    inner class FoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val emoji: TextView = view.findViewById(R.id.tvEmoji)
        private val name: TextView = view.findViewById(R.id.tvFoodName)
        private val category: TextView = view.findViewById(R.id.tvCategory)
        private val price: TextView = view.findViewById(R.id.tvPrice)
        private val favorite: TextView = view.findViewById(R.id.tvFavorite)
        private val addCart: Button = view.findViewById(R.id.btnAddCart)
        fun bind(item: FoodItem) {
            emoji.text = item.emoji
            name.text = item.name
            category.text = item.category
            price.text = rupiah(item.price)
            favorite.text = if (item.isFavorite) "♥" else "♡"
            itemView.setOnClickListener { onClick(item) }
            favorite.setOnClickListener { onFavorite(item) }
            addCart.setOnClickListener { onAddCart(item) }
        }
    }

    companion object {
        fun rupiah(value: Long): String = NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(value)
            .replace(",00", "").replace("Rp", "Rp ")
    }
}
