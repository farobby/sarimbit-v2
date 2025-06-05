package com.optik.sarimbit.app

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.optik.sarimbit.app.adapter.ColorsFrameAdapter
import com.optik.sarimbit.app.adapter.FrameAdapter
import com.optik.sarimbit.app.model.CheckoutProduct
import com.optik.sarimbit.app.model.Product
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.app.util.SessionManager
import com.optik.sarimbit.databinding.ActivityDetailProductBinding

class DetailProductActivity : BaseView() {
    private lateinit var binding: ActivityDetailProductBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val product = getSerializable(intent, "product", Product::class.java)

        val colors = product.colors
        Glide.with(this).load(product.image)
            .into(binding.imageProduct)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val layoutManage2 = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewColors.layoutManager = layoutManager
        binding.recyclerViewVariants.layoutManager = layoutManage2
        binding.tvProductName.text = product.name
        binding.tvProductPrice.text = "${convertToCurrency(product.price.toLong())}"
        binding.tvBrand.text = product.brand
        binding.tvDescription.text = product.description
        binding.recyclerViewColors.adapter = colors.let { ColorsFrameAdapter(it) }

        product.variants?.let {
            val adapter = FrameAdapter(it) { variant ->
                binding.tvProductPrice.text = "${convertToCurrency(variant.price.toLong())}"
            }
            binding.recyclerViewVariants.adapter = adapter
        }
        binding.ivClose.setOnClickListener {
            finish()
        }
        binding.btnAddToCart.setOnClickListener {
            addToCart(product)
        }
        binding.ivCart.setOnClickListener {
            addToCart(product)
        }
    }

    private fun addToCart(product: Product) {
        val userId = SessionManager.getId(this)
        if (userId.isNullOrEmpty()) {
            showToast("User not logged in")
            return
        }

        val checkoutProduct = convertToCheckoutProduct(product)
        val cartRef = FirebaseFirestore.getInstance()
            .collection("cart")
            .document(userId)
            .collection("items")

        showLoading("")

        // Cari apakah item dengan kombinasi id + frameVariant + colors sudah ada
        cartRef.whereEqualTo("id", checkoutProduct.id)
            .whereEqualTo("frameVariant", checkoutProduct.frameVariant)
            .whereEqualTo("colors", checkoutProduct.colors)
            .whereEqualTo("checkedOut", false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Produk sudah ada dengan kombinasi yang sama, update quantity
                    val document = querySnapshot.documents[0]
                    val docRef = document.reference
                    val currentQuantity = document.getLong("selectedQuantity") ?: 1

                    docRef.update("selectedQuantity", currentQuantity + 1)
                        .addOnSuccessListener {
                            hideLoading()
                            showToast("Quantity updated in cart")
                        }
                        .addOnFailureListener { e ->
                            hideLoading()
                            showToast("Failed to update quantity: ${e.message}")
                        }
                } else {
                    // Produk dengan kombinasi tersebut belum ada, tambahkan baru
                    val newDocRef = cartRef.document() // Gunakan ID otomatis
                    newDocRef.set(checkoutProduct)
                        .addOnSuccessListener {
                            hideLoading()
                            showToast("Added to cart")
                        }
                        .addOnFailureListener { e ->
                            hideLoading()
                            showToast("Failed to add to cart: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                hideLoading()
                showToast("Error checking cart: ${e.message}")
            }
    }

    private fun convertToCheckoutProduct(product: Product): CheckoutProduct {
        return CheckoutProduct(
            id = product.id,
            name = product.name,
            image = product.image,
            price = (binding.recyclerViewVariants.adapter as FrameAdapter).getSelectedFrame().price,
            brand = product.brand,
            categoryFrame = product.categoryFrame,
            description = product.description,
            colors = (binding.recyclerViewColors.adapter as ColorsFrameAdapter).getSelectedColor(),
            frameVariant = (binding.recyclerViewVariants.adapter as FrameAdapter).getSelectedFrame().name,
            tags = product.tags,
            selectedQuantity = 1,
            totalPrice = product.price
        )
    }
}