package com.optik.sarimbit.app

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.optik.sarimbit.R
import com.optik.sarimbit.app.adapter.CheckoutProductAdminAdapter
import com.optik.sarimbit.app.model.CheckoutOrder
import com.optik.sarimbit.app.model.CheckoutProduct
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.databinding.ActivityDetailOrderAdminBinding

class OrdersDetailAdminActivity : BaseView() {
    private lateinit var binding: ActivityDetailOrderAdminBinding
    private val allOrders = mutableListOf<CheckoutProduct>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailOrderAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadAllOrders()
        val id = intent.getStringExtra("id")
        binding.btnProses.setOnClickListener {
            updateOrderStatus(
                orderId = id?:"",
                newStatus = "Proses",
                onSuccess = {
                    showToast("Status berhasil diperbarui")
                },
                onFailure = { e ->
                    showToast("Gagal update: ${e.message}")
                }
            )

        }
        binding.btnFinish.setOnClickListener {
            updateOrderStatus(
                orderId = id?:"",
                newStatus = "Selesai",
                onSuccess = {
                    showToast("Status berhasil diperbarui")
                },
                onFailure = { e ->
                    showToast("Gagal update: ${e.message}")
                }
            )

        }
    }

    val firestore = FirebaseFirestore.getInstance()


    private fun loadAllOrders() {
        showLoading("Loading...");
        val id = intent.getStringExtra("id")
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
                    val customerPhone = orderDoc.getString("customerPhone") ?: ""
                    val status = orderDoc.getString("status") ?: ""
                    val receipt = orderDoc.getString("receipt") ?: ""
                    val timestamp = orderDoc.getLong("timestamp") ?: 0L
                    if (orderId == id) {
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
                                    receipt = receipt,
                                    timestamp = timestamp,
                                    items = itemsList
                                )
                                binding.tvIdOrder.text= "Detail Pesanan #${timestamp.toString().substring(timestamp.toString().length-4, timestamp.toString().length)}"
                                binding.tvPhoneName.text= customerPhone
                                binding.tvCustName.text= customerName
                                binding.tvTotal.text =  "${convertToCurrency(totalPrice.toLong())}"
                                binding.tvSubTotal.text =  "${convertToCurrency(totalPrice.toLong())}"
                                binding.tvStatus.text = status
                                if(status.isEmpty()){
                                    binding.tvStatus.text = "Baru"
                                }else{
                                    binding.tvStatus.text = status
                                }
                                Glide.with(this).load(receipt)
                                    .into(binding.ivReceipt)
                                when (status) {
                                    "Baru" -> {
                                        binding.tvStatus.setBackgroundResource(R.drawable.bg_status_yellow)
                                    }
                                    "Proses" -> {
                                        binding.tvStatus.setBackgroundResource(R.drawable.bg_status_blue)
                                    }
                                    "Selesai" -> {
                                        binding.tvStatus.setBackgroundResource(R.drawable.bg_status_green)
                                    }
                                }
                                allOrders.addAll(itemsList)
                                if (allOrders.size == ordersSnapshot.size()) {
                                    handleLoadedAllOrders(allOrders)
                                }
                                binding.rvProductList.layoutManager = LinearLayoutManager(this)
                                binding.rvProductList.adapter =
                                    CheckoutProductAdminAdapter(allOrders)
                                Log.d("testinga", "--> ${allOrders.size}")
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

    private fun handleLoadedAllOrders(orders: List<CheckoutProduct>) {
        // Do something with all loaded orders
        showToast("Loaded ${orders.size} orders")
    }

    fun updateOrderStatus(orderId: String, newStatus: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val orderRef = db.collection("orders").document(orderId)

        orderRef.update("status", newStatus)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

}