package com.optik.sarimbit.app

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.optik.sarimbit.R
import com.optik.sarimbit.app.adapter.OrderAdapter
import com.optik.sarimbit.app.model.CheckoutOrder
import com.optik.sarimbit.app.model.CheckoutProduct
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.databinding.ActivityOrdersAdminBinding

class OrdersActivity : BaseView() {

    private lateinit var binding: ActivityOrdersAdminBinding
    private lateinit var adapter: OrderAdapter
    private val allOrders = mutableListOf<CheckoutOrder>()

//    private val allOrders = listOf(
//        OrderModel("120425", "Jennie", "26/04/2025", "Rp 200.000", "Baru"),
//        OrderModel("120525", "Frank", "26/04/2025", "Rp 350.000", "Baru"),
//        OrderModel("120325", "Abdur", "24/04/2025", "Rp 500.000", "Selesai"),
//        OrderModel("120425", "Lisa", "22/04/2025", "Rp 200.000", "Selesai")
//    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = OrderAdapter(allOrders)
        binding.orderRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.orderRecyclerView.adapter = adapter

        binding.btnAll.setOnClickListener {
            adapter.updateList(allOrders)
            setActiveFilterButton(binding.btnAll)
        }

        binding.btnNew.setOnClickListener {
            adapter.updateList(allOrders.filter { it.status == "Baru" })
            setActiveFilterButton(binding.btnNew)
        }

        binding.btnDone.setOnClickListener {
            adapter.updateList(allOrders.filter { it.status == "Selesai" })
            setActiveFilterButton(binding.btnDone)
        }
        binding.btnAll.performClick()
        loadAllOrders()

        binding.ivClose.setOnClickListener {
            finish()
        }
    }
    private fun setActiveFilterButton(activeButton: TextView) {
        val buttons = listOf(binding.btnAll, binding.btnNew, binding.btnDone)
        buttons.forEach { button ->
            if (button == activeButton) {
                button.setTextColor(ContextCompat.getColor(this, R.color.white))
                button.background=ContextCompat.getDrawable(this, R.drawable.bg_tab_selected)
            } else {
                button.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                button.background=ContextCompat.getDrawable(this, R.drawable.bg_tab_unselected)
            }
        }
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

                    val orderId = orderDoc.id
                    val totalPrice = (orderDoc.getLong("totalPrice") ?: 0).toInt()
                    val customerName = orderDoc.getString("customerName") ?: ""
                    val status = orderDoc.getString("status") ?: ""
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
                                userId = "userId",
                                totalPrice = totalPrice,
                                customerName = customerName,
                                customerPhone = customerPhone,
                                timestamp = timestamp,
                                status = status,
                                items = itemsList
                            )

                            allOrders.add(orderData)
                            if (allOrders.size == ordersSnapshot.size()) {
                                handleLoadedAllOrders(allOrders)
                            }
                            binding.orderRecyclerView.layoutManager = LinearLayoutManager(this)
                            val sortedOrders = allOrders.sortedByDescending  { it.timestamp }
                            adapter = OrderAdapter(sortedOrders)
                            binding.orderRecyclerView.adapter = adapter
                            Log.d("testinga","--> ${allOrders[0].items.size}")
                            hideLoading()
                        }
                        .addOnFailureListener { e ->
                            hideLoading()
                            showToast("Failed to load items for order $orderId: ${e.message}")
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

