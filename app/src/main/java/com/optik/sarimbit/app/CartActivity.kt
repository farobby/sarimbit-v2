package com.optik.sarimbit.app

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.optik.sarimbit.app.adapter.CartAdapter
import com.optik.sarimbit.app.model.CheckoutProduct
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.app.util.CartUpdateListener
import com.optik.sarimbit.app.util.SessionManager
import com.optik.sarimbit.databinding.ActivityCartBinding

class CartActivity : BaseView(), CartUpdateListener {
    private lateinit var cartAdapter: CartAdapter
    private lateinit var binding: ActivityCartBinding
    val firestore = FirebaseFirestore.getInstance()
    private val cartProducts = mutableListOf<CheckoutProduct>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title="Keranjang"
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.addToCart.setOnClickListener {
//            checkout()
            if(cartProducts.isNotEmpty()){
                goToPage(DetailOrderActivity::class.java);
            }else{
                showToast("Maaf, belum ada item di keranjang")
            }
        }
        binding.recyclerViewFavorites.layoutManager = LinearLayoutManager(this)
        cartAdapter = CartAdapter(this, cartProducts, this) // Pass this as listener
        binding.recyclerViewFavorites.adapter = cartAdapter
        fetchCartData()
    }
    override fun onCartUpdated(totalPrice: Double) {
        binding.tvProductTotal.text = "Total: ${convertToCurrency(totalPrice.toLong())}"
    }
    private fun fetchCartData() {
        showLoading("Loading cart items...")

        val userId = SessionManager.getId(this)
        if (userId.isNullOrEmpty()) {
            showToast("User not logged in")
            hideLoading()
            return
        }

        firestore.collection("cart")
            .document(userId)
            .collection("items")
            .whereEqualTo("checkedOut", false)
            .get()
            .addOnSuccessListener { documents ->
                cartProducts.clear() // Kosongkan list sebelum menambahkan data baru
                for (document in documents) {
                    val product = CheckoutProduct(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        image = document.getString("image") ?: "",
                        price = document.getLong("price")?.toInt() ?: 0,
                        quantity = document.getLong("quantity")?.toInt() ?: 1,
                        selectedQuantity = document.getLong("selectedQuantity")?.toInt() ?: 1,
                        brand = document.getString("brand") ?: "",
                        categoryFrame = document.getString("categoryFrame") ?: "",
                        description = document.getString("description") ?: "",
                        frameVariant = document.getString("frameVariant") ?: "",
                        colors = document.getString("colors") ?: "",
                        tags = document["tags"] as? ArrayList<String> ?: arrayListOf()
                    )
                    cartProducts.add(product)
                }
                if(cartProducts.isEmpty()){
                    binding.llNoData.showView()
                }else{
                    binding.llNoData.hideView()
                }
                updateTotalPrice()
                binding.recyclerViewFavorites.adapter?.notifyDataSetChanged()
                hideLoading()
            }
            .addOnFailureListener { e ->
                hideLoading()
                showToast("Failed to load cart: ${e.message}")
            }
    }

    private fun updateTotalPrice() {
        val totalPrice = cartProducts.sumOf { it.price * it.selectedQuantity }
        binding.tvProductTotal.text = "Total: ${convertToCurrency(totalPrice.toLong())}"
    }

}