package com.optik.sarimbit.app

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.optik.sarimbit.app.adapter.CheckoutOrderAdapter
import com.optik.sarimbit.app.model.CheckoutOrder
import com.optik.sarimbit.app.model.CheckoutProduct
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.app.util.SessionManager
import com.optik.sarimbit.databinding.ActivityHistoryOrderBinding

class HistoryOrderActivity : BaseView() {
    private lateinit var checkoutOrderAdapter: CheckoutOrderAdapter

    private val allOrders = mutableListOf<CheckoutOrder>()
    private lateinit var binding: ActivityHistoryOrderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Orderan Saya"
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.recyclerViewFavorites.layoutManager = LinearLayoutManager(this)
        checkoutOrderAdapter = CheckoutOrderAdapter(this, allOrders) // Pass this as listener
        binding.recyclerViewFavorites.adapter = checkoutOrderAdapter
        loadAllOrders()
    }

    val firestore = FirebaseFirestore.getInstance()


    private fun loadAllOrders() {
        showLoading("Loading...");
        val ordersRef = firestore.collection("orders")

        ordersRef.get()
            .addOnSuccessListener { ordersSnapshot ->

                if (ordersSnapshot.isEmpty) {
                    hideLoading()
                    showToast("No orders found")
                    return@addOnSuccessListener
                }

                for (orderDoc in ordersSnapshot.documents) {
                    val userId = orderDoc.getString("userId") ?: ""
                    if (userId == SessionManager.getId(this)) {

                        val orderId = orderDoc.id
                        val totalPrice = (orderDoc.getLong("totalPrice") ?: 0).toInt()
                        val customerName = orderDoc.getString("customerName") ?: ""
                        val customerPhone = orderDoc.getString("customerPhone") ?: ""
                        val timestamp = orderDoc.getLong("timestamp") ?: 0L

                        // Load items inside this order
                        orderDoc.reference.collection("items").get()
                            .addOnSuccessListener { itemsSnapshot ->
                                val itemsList = mutableListOf<CheckoutProduct>()

                                for (itemDoc in itemsSnapshot.documents) {
                                    val item = CheckoutProduct(
                                        id = itemDoc.id,
                                        name = itemDoc.getString("name") ?: "",
                                        price = (itemDoc.getLong("price") ?: 0).toInt(),
                                        frameVariant = itemDoc.getString("frameVariant") ?: "",
                                        colors = itemDoc.getString("colors") ?: "",
                                        image = itemDoc.getString("productImage") ?: "",
                                        selectedQuantity = (itemDoc.getLong("quantity")
                                            ?: 0).toInt(),
                                        totalPrice = (itemDoc.getLong("total") ?: 0).toInt()
                                    )
                                    itemsList.add(item)
                                }

                                val orderData = CheckoutOrder(
                                    orderId = orderId,
                                    userId = userId,
                                    totalPrice = totalPrice,
                                    customerName = customerName,
                                    customerPhone = customerPhone,
                                    timestamp = timestamp,
                                    items = itemsList
                                )

                                allOrders.add(orderData)
                                if(allOrders.isEmpty()){
                                    binding.llNoData.showView()
                                }else{
                                    binding.llNoData.hideView()
                                }
                                // Optionally: if all orders loaded, you can notify here
                                if (allOrders.size == ordersSnapshot.size()) {
                                    handleLoadedAllOrders(allOrders)
                                }
                                binding.recyclerViewFavorites.adapter?.notifyDataSetChanged()
                                hideLoading()
                            }
                            .addOnFailureListener { e ->
                                hideLoading()
                                showToast("Failed to load items for order $orderId: ${e.message}")
                            }
                    }
                }
                hideLoading()
            }
            .addOnFailureListener { e ->
                showToast("Failed to load orders: ${e.message}")
                hideLoading()
            }
    }

    private fun handleLoadedAllOrders(orders: List<CheckoutOrder>) {
        // Do something with all loaded orders
//        showToast("Loaded ${orders.size} orders")
    }


}