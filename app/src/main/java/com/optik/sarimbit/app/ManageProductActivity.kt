package com.optik.sarimbit.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.QuerySnapshot
import com.optik.sarimbit.R
import com.optik.sarimbit.app.adapter.ManageProductAdapter
import com.optik.sarimbit.app.model.Product
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.app.util.CallbackFireStore
import com.optik.sarimbit.app.util.Constants
import com.optik.sarimbit.databinding.ActivityManageProductBinding

class ManageProductActivity : BaseView() {
    private lateinit var binding: ActivityManageProductBinding
    private lateinit var adapter: ManageProductAdapter
    private val products = ArrayList<Product>()
    private val filteredProducts = ArrayList<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageProductBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.ivClose.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        setupRecyclerView()
        setupSearch()
        getProduct()
    }
    private fun setupRecyclerView() {
        adapter = ManageProductAdapter(filteredProducts) { product ->
            val bundle = Bundle()
            bundle.putSerializable("product", product)
            goToPage(EditProductActivity::class.java, bundle)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val query = p0.toString().lowercase()
                filteredProducts.clear()
                filteredProducts.addAll(
                    products.filter { product ->
                        product.name.lowercase().contains(query) ||
                                product.brand.lowercase().contains(query) ||
                                product.description.lowercase().contains(query)
                    }
                )
                adapter.notifyDataSetChanged()
                showOrHideClearIcon(query.isNotEmpty())
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
        val editText = binding.etSearch
        editText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = editText.compoundDrawables[2]
                if (drawableEnd != null) {
                    val bounds = drawableEnd.bounds
                    val x = event.x.toInt()
                    val width = editText.width
                    val padding = editText.paddingEnd
                    if (x >= width - padding - bounds.width()) {
                        // Clear the text and reset focus
                        editText.text?.clear()
                        editText.clearFocus()

                        // 👇 Inform accessibility services that a click occurred
                        v.performClick()
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
        showOrHideClearIcon(false)

    }
    private fun showOrHideClearIcon(show: Boolean) {
        val clearIcon = if (show) {
            ContextCompat.getDrawable(this, R.drawable.ic_close_24)
        } else null

        val searchIcon = ContextCompat.getDrawable(this, R.drawable.ic_search)

        binding.etSearch.setCompoundDrawablesWithIntrinsicBounds(
            searchIcon, null, clearIcon, null
        )
    }

    private fun getProduct() {
        showLoading("")
        firestoreUtil.readAllData(
            Constants.TABLE_PRODUCT,
            object : CallbackFireStore.ReadData {
                override fun onSuccessReadData(document: QuerySnapshot?) {
                    products.clear()
                    document?.forEach {
                        val product = Product().apply {
                            id = it.id
                            name = it.getString(Constants.PRODUCT_NAME) ?: ""
                            image = it.getString(Constants.PRODUCT_IMAGE) ?: ""
                            price = (it.get(Constants.PRODUCT_PRICE) as? Number)?.toInt() ?: 0
                            quantity = (it.get(Constants.PRODUCT_QUANTITY) as? Number)?.toInt() ?: 0
                            brand = it.getString(Constants.PRODUCT_BRAND) ?: ""
                            description = it.getString(Constants.PRODUCT_DESCRIPTION) ?: ""
                            categoryFrame = it.getString(Constants.PRODUCT_CATEGORY_FRAME) ?: ""
                            tags?.addAll(it.get(Constants.PRODUCT_TAGS) as? List<String> ?: emptyList())
                            colors?.addAll(it.get(Constants.PRODUCT_COLORS) as? List<String> ?: emptyList())
                        }
                        products.add(product)
                    }

                    // Show all at first
                    filteredProducts.clear()
                    filteredProducts.addAll(products)
                    adapter.notifyDataSetChanged()
                    hideLoading()
                }

                override fun onFailedReadData(message: String?) {
                    showToast(message)
                }
            }
        )
    }
}
