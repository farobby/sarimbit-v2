package com.optik.sarimbit.app

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.QuerySnapshot
import com.optik.sarimbit.app.adapter.FavoriteAdapter
import com.optik.sarimbit.app.model.Product
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.app.util.CallbackFireStore
import com.optik.sarimbit.app.util.Constants
import com.optik.sarimbit.app.util.Constants.FAVORITE_ID_USER
import com.optik.sarimbit.app.util.SessionManager
import com.optik.sarimbit.databinding.ActivityFavoriteBinding

class FavoriteActivity : BaseView(){
    private lateinit var binding: ActivityFavoriteBinding
    private var productList: List<Product> = listOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title="Produk Favorite Anda"
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        getProduct()

    }
    private fun getProduct() {
        firestoreUtil.readDataSpecificCondition("Loading",
            Constants.TABLE_FAVORITES,
            FAVORITE_ID_USER,SessionManager.getId(this),
            object : CallbackFireStore.ReadData {
                override fun onSuccessReadData(document: QuerySnapshot?) {
                    val products = ArrayList<Product>()

                    document?.forEach {
                        val product = Product()
                        product.name = it.data[Constants.PRODUCT_ID].toString()
                        product.name = it.data[Constants.PRODUCT_NAME].toString()
                        product.image = it.data[Constants.PRODUCT_IMAGE].toString()
                        product.price = it.data[Constants.PRODUCT_PRICE].toString().toInt()
                        product.quantity = it.data[Constants.PRODUCT_QUANTITY].toString().toInt()
                        product.brand = it.data[Constants.PRODUCT_BRAND].toString()
                        product.description = it.data[Constants.PRODUCT_DESCRIPTION].toString()
                        product.categoryFrame = it.data[Constants.PRODUCT_CATEGORY_FRAME].toString()
                        val photos = it.data[Constants.PRODUCT_TAGS] as List<String>
                        val colors = it.data[Constants.PRODUCT_COLORS] as List<String>
                        product.tags?.addAll(photos)
                        product.colors?.addAll(colors)
                        products.add(product)
                    }

                    // Simpan data produk ke dalam variabel global
                    productList = products
                    binding.recyclerViewFavorites.layoutManager = LinearLayoutManager(this@FavoriteActivity)
                    binding.recyclerViewFavorites.adapter = FavoriteAdapter(products)

                    // Setup RecyclerView

                }

                override fun onFailedReadData(message: String?) {
                    showToast(message)
                }
            }
        )
    }

}