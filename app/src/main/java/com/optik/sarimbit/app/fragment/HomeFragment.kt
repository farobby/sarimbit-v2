package com.optik.sarimbit.app.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.optik.sarimbit.R
import com.optik.sarimbit.app.CartActivity
import com.optik.sarimbit.app.DetailProductActivity
import com.optik.sarimbit.app.FavoriteActivity
import com.optik.sarimbit.app.SearchActivity
import com.optik.sarimbit.app.adapter.CategoryAdapter
import com.optik.sarimbit.app.adapter.ProductAdapter
import com.optik.sarimbit.app.model.CheckoutProduct
import com.optik.sarimbit.app.model.FavoriteProduct
import com.optik.sarimbit.app.model.Product
import com.optik.sarimbit.app.model.VariantModel
import com.optik.sarimbit.app.util.BaseFragment
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.app.util.CallbackFireStore
import com.optik.sarimbit.app.util.Constants
import com.optik.sarimbit.app.util.Constants.DESIGNER_FRAME
import com.optik.sarimbit.app.util.Constants.FAVORITE_ID_USER
import com.optik.sarimbit.app.util.Constants.KACA_MATA_BACA
import com.optik.sarimbit.app.util.Constants.OTHERS
import com.optik.sarimbit.app.util.Constants.SPORT_FRAME
import com.optik.sarimbit.app.util.Constants.SUNGLASSES
import com.optik.sarimbit.app.util.Constants.TABLE_FAVORITES
import com.optik.sarimbit.app.util.FireStoreUtil
import com.optik.sarimbit.app.util.SessionManager
import com.optik.sarimbit.databinding.FragmentHomeBinding


class HomeFragment : BaseFragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var firestoreUtil: FireStoreUtil
    private var productList: List<Product> = listOf()
    private lateinit var productAdapter: ProductAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)

        // Set the Toolbar as ActionBar
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)

        // Enable menu in Fragment
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_meunu, menu) // Inflate the menu file
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_favorite -> {
                goToPage(FavoriteActivity::class.java)
                return true
            }

            R.id.action_cart -> {
                goToPage(CartActivity::class.java)
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        firestoreUtil = FireStoreUtil(activity as BaseView)


        val categories =
            listOf(SUNGLASSES, KACA_MATA_BACA, DESIGNER_FRAME, SPORT_FRAME, OTHERS)
        val layoutManager = FlexboxLayoutManager(context)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.flexWrap = FlexWrap.WRAP

        binding.recyclerCategories.layoutManager = layoutManager
        binding.recyclerCategories.adapter = CategoryAdapter(categories) { productTags ->
            filterProductsByTags(productTags)
        }

        binding.seeAll.setOnClickListener {
            goToPage(SearchActivity::class.java)
        }
        binding.etSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text.toString().trim()
                filterProducts(query)
                true // Mengembalikan true untuk menandakan event telah ditangani
            } else {
                false
            }
        }
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isEmpty()) {
                    filterProducts(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        getProduct()

    }

    private fun filterProducts(query: String) {
        val filteredList = productList.filter {
            it.name.contains(query, ignoreCase = true)
        }

        productAdapter.updateList(filteredList)
    }

    private fun filterProductsByTags(tag: String) {
        val filteredList = productList.filter { product ->
            product.tags?.any { it.equals(tag, ignoreCase = true) } == true
        }

        productAdapter.updateList(filteredList)
    }


    private fun getProduct() {
        val db = FirebaseFirestore.getInstance()
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
                        product.categoryFrame = it.data[Constants.PRODUCT_CATEGORY_FRAME].toString()
                        product.description = it.data[Constants.PRODUCT_DESCRIPTION].toString()
                        val photos = it.data[Constants.PRODUCT_TAGS] as List<String>
                        val colors = it.data[Constants.PRODUCT_COLORS] as List<String>
                        val variantsList = (it.data["variants"] as? List<Map<String, Any>>)?.map { variantMap ->
                            VariantModel(
                                price = (variantMap["price"] as? Long)?.toInt() ?: 0,
                                name = variantMap["name"] as? String ?: ""
                            )
                        } ?: listOf()
                        product.tags?.addAll(photos)
                        product.colors.addAll(colors)
                        product.variants?.addAll(variantsList)
                        products.add(product)
                    }

                    // Simpan data produk ke dalam variabel global
                    productList = products

                    // Setup RecyclerView
                    binding.recyclerProducts.layoutManager =
                        GridLayoutManager(requireContext(), 2)

                    productAdapter = ProductAdapter(
                        productList,
                        onclick = { product ->
                            val bundle = Bundle()
                            bundle.putSerializable("product", product)
                            goToPage(DetailProductActivity::class.java, bundle)
                        },
                        onclickFave = { product ->
                            val favoriteProduct = FavoriteProduct(
                                image = product.image,
                                price = product.price,
                                id = product.id,
                                name = product.name,
                                idUser = SessionManager.getId(requireContext()),
                                brand = product.brand,
                                description = product.description,
                                categoryFrame = product.categoryFrame,
                                colors = product.colors,
                                tags = product.tags,
                                quantity = product.quantity
                            )
                            checkProduct(product.id) {
                                val productRef = db.collection(TABLE_FAVORITES)
                                productRef.add(favoriteProduct)
                                    .addOnSuccessListener { documentReference ->
                                        val productId = documentReference.id
                                        println("✅ Product added with ID: $productId")
                                    }
                                    .addOnFailureListener { e ->
                                        println("❌ Error adding product: ${e.message}")
                                    }
                                Toast.makeText(
                                    context,
                                    "Favorited: ${product.name}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        },
                        onclickCart = { product ->
                            addToCart(product)
                        }

                    )

                    binding.recyclerProducts.adapter = productAdapter
                }

                override fun onFailedReadData(message: String?) {
                    showToast(message)
                }
            }
        )
    }

    private fun checkProduct(id: String, onGranted: () -> Unit) {
        firestoreUtil.readDataSpecificCondition("",
            TABLE_FAVORITES,
            FAVORITE_ID_USER, SessionManager.getId(requireContext()),
            object : CallbackFireStore.ReadData {
                override fun onSuccessReadData(document: QuerySnapshot?) {
                    val productsIds = ArrayList<String>()
                    document?.forEach {
                        productsIds.add(it.data[Constants.PRODUCT_ID].toString())
                    }
                    if (!productsIds.contains(id)) {
                        onGranted()
                    } else {
                        Toast.makeText(
                            context,
                            "Sudah ada di Favorit",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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
        val userId = SessionManager.getId(requireContext())
        if (userId.isNullOrEmpty()) {
            showToast("User not logged in")
            return
        }

        val checkoutProduct = convertToCheckoutProduct(product)
        val cartRef = FirebaseFirestore.getInstance()
            .collection("cart")
            .document(userId)
            .collection("items")

        (requireActivity() as BaseView).showLoading("")

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
                            (requireActivity() as BaseView).hideLoading()
                            showToast("Quantity updated in cart")
                        }
                        .addOnFailureListener { e ->
                            (requireActivity() as BaseView).hideLoading()
                            showToast("Failed to update quantity: ${e.message}")
                        }
                } else {
                    // Produk dengan kombinasi tersebut belum ada, tambahkan baru
                    val newDocRef = cartRef.document() // Gunakan ID otomatis
                    newDocRef.set(checkoutProduct)
                        .addOnSuccessListener {
                            (requireActivity() as BaseView).hideLoading()
                            showToast("Added to cart")
                        }
                        .addOnFailureListener { e ->
                            (requireActivity() as BaseView).hideLoading()
                            showToast("Failed to add to cart: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                (requireActivity() as BaseView).hideLoading()
                showToast("Error checking cart: ${e.message}")
            }
    }



}
