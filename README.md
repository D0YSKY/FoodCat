# FoodCatalog — Android Studio (Kotlin + XML + Room)

Aplikasi katalog makanan lokal untuk belajar Android. Fitur: pencarian, filter kategori, detail makanan, favorit, keranjang dengan kuantitas dan total, serta admin CRUD menu.

## Persyaratan
- Android Studio versi stabil yang mendukung Android Gradle Plugin 8.7.3
- JDK 17 (Android Studio biasanya sudah menyertakan JBR 17)
- Android SDK Platform 35
- Emulator atau perangkat Android API 24+
- Internet hanya diperlukan saat Gradle pertama kali mengunduh dependencies. Aplikasi saat berjalan tidak memerlukan internet.

## Cara membuka
1. Ekstrak `FoodCatalog.zip`.
2. Android Studio → **Open** → pilih folder `FoodCatalog` (folder yang berisi `settings.gradle.kts`).
3. Tunggu **Gradle Sync** selesai. Jika diminta, pilih JDK 17.
4. Buat emulator melalui Device Manager, atau sambungkan HP dengan USB debugging aktif.
5. Klik **Run ▶**.

## Struktur folder
```text
FoodCatalog/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
└── app/
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/example/foodcatalog/
        │   ├── MainActivity.kt
        │   ├── data/
        │   │   ├── AppDatabase.kt
        │   │   ├── FoodDao.kt
        │   │   ├── FoodItem.kt
        │   │   └── CartItem.kt
        │   └── ui/FoodAdapter.kt
        └── res/
            ├── drawable/ (background XML)
            ├── layout/activity_main.xml
            ├── layout/item_food.xml
            └── values/ (colors, strings, themes)
```

## Cara menggunakan
- **Menu**: lihat semua makanan, cari nama, dan pilih kategori.
- Tekan kartu makanan untuk melihat detail.
- Tekan ikon hati untuk favorit.
- Tekan **+ Keranjang** untuk menambah kuantitas.
- Buka **Favorit** untuk melihat makanan yang ditandai.
- Buka **Keranjang** dan tekan bagian total untuk ringkasan atau mengosongkan keranjang.
- Buka **Admin** untuk tambah atau edit/hapus menu.

## Database dan catatan
- Room menyimpan menu dan favorit pada tabel `foods`; keranjang pada tabel `cart` di database `food_catalog.db`.
- Sepuluh contoh menu otomatis dimasukkan pada peluncuran pertama jika database masih kosong.
- Tombol admin pada demo ini tidak dilindungi login. Tambahkan autentikasi/otorisasi sebelum aplikasi digunakan sungguhan.
- Implementasi database memakai `allowMainThreadQueries()` agar mudah dipahami saat praktikum. Untuk produksi, pindahkan akses database ke coroutine/Flow dan ViewModel.
- Keranjang hanya menyimpan data lokal. Belum ada checkout, pembayaran, stok dinamis, sinkronisasi server, atau cetak nota.
- Gambar menu menggunakan emoji agar project bisa langsung jalan tanpa file gambar tambahan; dapat diganti dengan drawable foto makanan.

## Jika Gradle Sync gagal
- Pastikan JDK 17 dipilih di **Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK**.
- Pastikan SDK Platform 35 terpasang di **SDK Manager**.
- Jalankan **File → Sync Project with Gradle Files**.
- Pastikan koneksi internet tersedia saat dependencies pertama kali diunduh.
