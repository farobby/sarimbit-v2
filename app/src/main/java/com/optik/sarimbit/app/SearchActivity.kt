package com.optik.sarimbit.app

import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.optik.sarimbit.app.adapter.CategoryAdapter
import com.optik.sarimbit.app.adapter.ProductAdapter
import com.optik.sarimbit.app.model.CheckoutProduct
import com.optik.sarimbit.app.model.Product
import com.optik.sarimbit.app.model.VariantModel
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.app.util.CallbackFireStore
import com.optik.sarimbit.app.util.Constants
import com.optik.sarimbit.app.util.Constants.TABLE_FAVORITES
import com.optik.sarimbit.app.util.SessionManager
import com.optik.sarimbit.databinding.ActivitySearchBinding

class SearchActivity : BaseView() {

    private lateinit var binding: ActivitySearchBinding
    private var productList: List<Product> = listOf()
    private lateinit var productAdapter: ProductAdapter
    private lateinit var categoryFrameAdapter: CategoryAdapter
    private lateinit var filterByAdapter: CategoryAdapter
    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val categories = listOf(
            "round",
            "rimless",
            "cat eye",
            "clear"
        )
        val filter = listOf("Harga", "Brand")

        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.flexWrap = FlexWrap.WRAP

        binding.recyclerCategories.layoutManager = layoutManager
        categoryFrameAdapter = CategoryAdapter(categories) {}
        binding.recyclerCategories.adapter = categoryFrameAdapter

        val layoutManager2 = FlexboxLayoutManager(this)
        layoutManager2.flexDirection = FlexDirection.ROW
        layoutManager2.flexWrap = FlexWrap.WRAP

        binding.recyclerFilterBy.layoutManager = layoutManager2
        filterByAdapter = CategoryAdapter(filter) {}
        binding.recyclerFilterBy.adapter = filterByAdapter

        binding.recyclerProducts.layoutManager =
            GridLayoutManager(this@SearchActivity, 2)

        productAdapter = ProductAdapter(
            productList,
            onclick = { product ->
                val bundle = Bundle()
                bundle.putSerializable("product", product)
                goToPage(DetailProductActivity::class.java, bundle)
            },
            onclickFave = { product ->
                val productRef = db.collection(TABLE_FAVORITES)
                productRef.add(product)
                    .addOnSuccessListener { documentReference ->
                        val productId = documentReference.id
                        println("✅ Product added with ID: $productId")
                    }
                    .addOnFailureListener { e ->
                        println("❌ Error adding product: ${e.message}")
                    }
                Toast.makeText(
                    this@SearchActivity,
                    "Favorited: ${product.name}",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onclickCart = { product ->
                addToCart(product)
            }

        )

        binding.recyclerProducts.adapter = productAdapter
        binding.search.setOnClickListener {
            filterProducts(binding.etSearch.getValue(), categoryFrameAdapter.getSelectedCategory(), filterByAdapter.getSelectedCategory())
        }
        binding.back.setOnClickListener {
            finish()
        }
        getProduct()
    }

    private fun filterProducts(query: String,categoryFrame: String, sortBy: String = "price", ascending: Boolean = true) {
        val filteredList = productList.filter { product ->
            product.categoryFrame.contains(categoryFrame, ignoreCase = true) ||
                    product.name.contains(query, ignoreCase = true) ||
                    product.brand.contains(query, ignoreCase = true)
        }

        val sortedList = when (sortBy) {
            "price" -> if (ascending) filteredList.sortedBy { it.price } else filteredList.sortedByDescending { it.price }
            "brand" -> if (ascending) filteredList.sortedBy { it.brand } else filteredList.sortedByDescending { it.brand }
            else -> filteredList
        }

        productAdapter.updateList(sortedList)
    }


    private fun getProduct() {
        firestoreUtil.readAllData(
            Constants.TABLE_PRODUCT,
            object : CallbackFireStore.ReadData {
                override fun onSuccessReadData(document: QuerySnapshot?) {
                    val products = ArrayList<Product>()

                    document?.forEach {
                        val product = Product()
                        product.id = it.id
                        product.name = it.data[Constants.PRODUCT_NAME].toString()
                        product.image = it.data[Constants.PRODUCT_IMAGE].toString()
                        product.price = it.data[Constants.PRODUCT_PRICE].toString().toInt()
                        product.quantity = it.data[Constants.PRODUCT_QUANTITY].toString().toInt()
                        product.brand = it.data[Constants.PRODUCT_BRAND].toString()
                        product.description = it.data[Constants.PRODUCT_DESCRIPTION].toString()
                        product.categoryFrame = it.data[Constants.PRODUCT_CATEGORY_FRAME].toString()
                        val photos = it.data[Constants.PRODUCT_TAGS] as List<String>
                        val colors = it.data[Constants.PRODUCT_COLORS] as List<String>
                        val variantsList = (it.data["variants"] as? List<Map<String, Any>>)?.map { variantMap ->
                            VariantModel(
                                price = (variantMap["price"] as? Long)?.toInt() ?: 0,
                                name = variantMap["name"] as? String ?: ""
                            )
                        } ?: listOf()
                        product.tags?.addAll(photos)
                        product.colors?.addAll(colors)
                        product.variants?.addAll(variantsList)
                        products.add(product)
                    }

                    // Simpan data produk ke dalam variabel global
                    productList = products

                    // Setup RecyclerView

                }

                override fun onFailedReadData(message: String?) {
                    showToast(message)
                }
            }
        )
    }
    private fun convertToCheckoutProduct(product: Product): CheckoutProduct {
        return CheckoutProduct(
            id = product.id,
            name = product.name,
            image = product.image,
            price = product.price,
            brand = product.brand,
            categoryFrame = product.categoryFrame,
            description = product.description,
            colors = product.colors[0],
            frameVariant = product.variants[0].name,
            tags = product.tags,
            selectedQuantity = 1,
            totalPrice = product.price
        )
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


}