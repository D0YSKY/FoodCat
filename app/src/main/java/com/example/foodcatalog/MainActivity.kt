package com.example.foodcatalog

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodcatalog.data.AppDatabase
import com.example.foodcatalog.data.CartItem
import com.example.foodcatalog.data.FoodItem
import com.example.foodcatalog.ui.FoodAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var adapter: FoodAdapter
    private lateinit var search: EditText
    private lateinit var categories: LinearLayout
    private lateinit var sectionTitle: TextView
    private var selectedCategory = "Semua"
    private var page = Page.HOME
    private val categoryNames = listOf("Semua", "Nasi", "Mi", "Lauk", "Camilan", "Minuman", "Dessert")
    private enum class Page { HOME, FAVORITES, CART }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        db = AppDatabase.get(this)
        seedFoods()
        search = findViewById(R.id.etSearch)
        categories = findViewById(R.id.categoryContainer)
        sectionTitle = findViewById(R.id.tvSectionTitle)
        val recycler = findViewById<RecyclerView>(R.id.rvFoods)
        recycler.layoutManager = GridLayoutManager(this, 2)
        adapter = FoodAdapter(::showDetails, ::toggleFavorite, ::addToCart)
        recycler.adapter = adapter
        createCategoryButtons()

        search.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = refreshList()
                override fun afterTextChanged(s: Editable?) = Unit
            },
        )
        findViewById<Button>(R.id.btnHome).setOnClickListener { page = Page.HOME; refreshList() }
        findViewById<Button>(R.id.btnFavorites).setOnClickListener { page = Page.FAVORITES; refreshList() }
        findViewById<Button>(R.id.btnCart).setOnClickListener { page = Page.CART; search.setText(""); refreshList() }
        findViewById<Button>(R.id.btnAdmin).setOnClickListener { showAdminMenu() }
        refreshList()
    }

    private fun seedFoods() {
        if (db.foodDao().getAll().isNotEmpty()) return
        val foods = listOf(
            FoodItem(name = "Nasi Goreng Spesial", category = "Nasi", description = "Nasi goreng gurih dengan telur, ayam, dan sayuran.", price = 22000, emoji = "🍛"),
            FoodItem(name = "Nasi Ayam Bakar", category = "Nasi", description = "Nasi hangat dengan ayam bakar berbumbu dan sambal.", price = 26000, emoji = "🍗"),
            FoodItem(name = "Mie Goreng Jawa", category = "Mi", description = "Mi goreng khas Jawa dengan sayur dan telur.", price = 18000, emoji = "🍜"),
            FoodItem(name = "Soto Ayam", category = "Lauk", description = "Soto ayam berkuah hangat dengan koya dan jeruk nipis.", price = 17000, emoji = "🍲"),
            FoodItem(name = "Sate Ayam", category = "Lauk", description = "Sepuluh tusuk sate ayam dengan bumbu kacang.", price = 25000, emoji = "🍢"),
            FoodItem(name = "Kentang Goreng", category = "Camilan", description = "Kentang goreng renyah dengan taburan bumbu.", price = 14000, emoji = "🍟"),
            FoodItem(name = "Pisang Goreng", category = "Camilan", description = "Pisang goreng hangat dengan balutan tepung renyah.", price = 12000, emoji = "🍌"),
            FoodItem(name = "Es Teh Manis", category = "Minuman", description = "Teh manis dingin yang menyegarkan.", price = 6000, emoji = "🧋"),
            FoodItem(name = "Jus Alpukat", category = "Minuman", description = "Jus alpukat creamy, bisa pilih topping cokelat.", price = 15000, emoji = "🥑"),
            FoodItem(name = "Puding Cokelat", category = "Dessert", description = "Puding cokelat lembut dengan saus cokelat.", price = 10000, emoji = "🍮"),
        )
        foods.forEach { db.foodDao().insert(it) }
    }

    private fun createCategoryButtons() {
        categories.removeAllViews()
        categoryNames.forEach { category ->
            val button = Button(this).apply {
                text = category
                isAllCaps = false
                textSize = 12f
                minHeight = 0
                minimumHeight = 0
                setPadding(dp(12), 0, dp(12), 0)
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(40)).apply {
                    marginEnd = dp(6)
                }
                setOnClickListener { selectedCategory = category; page = Page.HOME; refreshList() }
            }
            categories.addView(button)
        }
    }

    private fun refreshList() {
        val query = search.text?.toString()?.trim().orEmpty()
        val foods = when (page) {
            Page.HOME -> db.foodDao().getAll().filter {
                ((selectedCategory == "Semua") || (it.category == selectedCategory)) &&
                    (it.name.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true))
            }
            Page.FAVORITES -> db.foodDao().getFavorites().filter { it.name.contains(query, ignoreCase = true) }
            Page.CART -> db.foodDao().getCart().asSequence().mapNotNull { cart ->
                val food = db.foodDao().getById(cart.foodId)
                food?.copy(name = "${food.name}  ×${cart.quantity}")
            }.filter { it.name.contains(query, ignoreCase = true) }.toList()
        }
        sectionTitle.text = when (page) {
            Page.HOME -> if (selectedCategory == "Semua") "Menu pilihan" else "Kategori: $selectedCategory"
            Page.FAVORITES -> "Makanan favorit ♥"
            Page.CART -> "Keranjang belanja 🛒"
        }
        adapter.submitList(foods)
        if (page == Page.CART) updateCartSummary() else removeCartSummaryIfPresent()
    }

    private fun updateCartSummary() {
        val root = findViewById<LinearLayout>(R.id.rootLayout)
        var summary = root.findViewWithTag<TextView>("cart_summary")
        if (summary == null) {
            summary = TextView(this).apply {
                tag = "cart_summary"
                textSize = 16f
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.orange))
                setPadding(dp(20), dp(8), dp(20), dp(8))
            }
            root.addView(summary, root.indexOfChild(findViewById<RecyclerView>(R.id.rvFoods)) + 1)
        }
        val cart = db.foodDao().getCart()
        val total = cart.sumOf { row -> (db.foodDao().getById(row.foodId)?.price ?: 0L) * row.quantity }
        summary.text = getString(R.string.cart_summary, FoodAdapter.rupiah(total), cart.sumOf { it.quantity })
        summary.setOnClickListener {
            if (cart.isEmpty()) toast("Keranjang masih kosong") else MaterialAlertDialogBuilder(this)
                .setTitle("Ringkasan keranjang")
                .setMessage("Jumlah item: ${cart.sumOf { it.quantity }}\nTotal: ${FoodAdapter.rupiah(total)}\n\nIni masih demo lokal; checkout/pembayaran belum terhubung.")
                .setNegativeButton("Kembali", null).setPositiveButton("Kosongkan") { _, _ -> db.foodDao().clearCart(); refreshList() }.show()
        }
    }

    private fun removeCartSummaryIfPresent() {
        val root = findViewById<LinearLayout>(R.id.rootLayout)
        root.findViewWithTag<TextView>("cart_summary")?.let { root.removeView(it) }
    }

    private fun showDetails(food: FoodItem) {
        val current = db.foodDao().getById(food.id) ?: food
        MaterialAlertDialogBuilder(this)
            .setTitle("${current.emoji}  ${current.name}")
            .setMessage("Kategori: ${current.category}\nHarga: ${FoodAdapter.rupiah(current.price)}\n\n${current.description}")
            .setNeutralButton(if (current.isFavorite) "Hapus favorit ♥" else "Tambah favorit ♡") { _, _ -> toggleFavorite(current) }
            .setNegativeButton("Tutup", null)
            .setPositiveButton("+ Keranjang") { _, _ -> addToCart(current) }
            .show()
    }

    private fun toggleFavorite(food: FoodItem) {
        val current = db.foodDao().getById(food.id) ?: return
        db.foodDao().setFavorite(food.id, !current.isFavorite)
        refreshList()
        toast(if (!current.isFavorite) "Ditambahkan ke favorit" else "Dihapus dari favorit")
    }

    private fun addToCart(food: FoodItem) {
        val old = db.foodDao().getCartItem(food.id)
        db.foodDao().saveCartItem(CartItem(food.id, (old?.quantity ?: 0) + 1))
        toast("${food.name} ditambahkan ke keranjang")
        if (page == Page.CART) refreshList()
    }

    private fun showAdminMenu() {
        val options = arrayOf("Tambah makanan", "Kelola / edit / hapus makanan")
        MaterialAlertDialogBuilder(this).setTitle("Admin menu")
            .setItems(options) { _, which -> if (which == 0) showFoodForm(null) else showManageFoods() }
            .setNegativeButton("Tutup", null).show()
    }

    private fun showManageFoods() {
        val foods = db.foodDao().getAll()
        if (foods.isEmpty()) { toast("Belum ada menu"); return }
        MaterialAlertDialogBuilder(this).setTitle("Pilih menu untuk dikelola")
            .setItems(foods.map { "${it.emoji} ${it.name} — ${FoodAdapter.rupiah(it.price)}" }.toTypedArray()) { _, which ->
                val selected = foods[which]
                MaterialAlertDialogBuilder(this).setTitle(selected.name)
                    .setItems(arrayOf("Edit menu", "Hapus menu")) { _, action ->
                        if (action == 0) showFoodForm(selected) else MaterialAlertDialogBuilder(this)
                            .setMessage("Hapus ${selected.name} dari katalog?")
                            .setNegativeButton("Batal", null)
                            .setPositiveButton("Hapus") { _, _ -> db.foodDao().delete(selected); db.foodDao().removeCartItem(selected.id); refreshList(); toast("Menu dihapus") }.show()
                    }.setNegativeButton("Batal", null).show()
            }.setNegativeButton("Batal", null).show()
    }

    private fun showFoodForm(existing: FoodItem?) {
        val container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(24), dp(8), dp(24), 0) }
        fun field(hint: String, value: String = ""): EditText = EditText(this).apply {
            this.hint = hint; setText(value); isSingleLine = hint != "Deskripsi"; textSize = 15f
            container.addView(this, LinearLayout.LayoutParams(-1, if (hint == "Deskripsi") dp(84) else dp(52)))
        }
        val name = field("Nama makanan", existing?.name.orEmpty())
        val category = field("Kategori (Nasi/Mi/Lauk/Camilan/Minuman/Dessert)", existing?.category.orEmpty())
        val price = field("Harga (angka, contoh 18000)", existing?.price?.toString().orEmpty()).apply { inputType = InputType.TYPE_CLASS_NUMBER }
        val description = field("Deskripsi", existing?.description.orEmpty())
        val emoji = field("Emoji makanan (contoh 🍜)", existing?.emoji ?: "🍽️")
        val dialog = MaterialAlertDialogBuilder(this).setTitle(if (existing == null) "Tambah makanan" else "Edit makanan")
            .setView(container).setNegativeButton("Batal", null).setPositiveButton("Simpan", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val n = name.text.toString().trim(); val c = category.text.toString().trim().ifBlank { "Lainnya" }
                val p = price.text.toString().toLongOrNull(); val d = description.text.toString().trim(); val e = emoji.text.toString().trim().ifBlank { "🍽️" }
                if (n.isBlank() || (p == null) || (p < 0)) { toast("Nama wajib diisi dan harga harus berupa angka >= 0"); return@setOnClickListener }
                val item = FoodItem(id = existing?.id ?: 0, name = n, category = c, description = d, price = p, emoji = e, isFavorite = existing?.isFavorite ?: false)
                if (existing == null) db.foodDao().insert(item) else db.foodDao().update(item)
                selectedCategory = "Semua"; page = Page.HOME; refreshList(); dialog.dismiss(); toast("Menu berhasil disimpan")
            }
        }
        dialog.show()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
