package com.optik.sarimbit.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.optik.sarimbit.app.model.CheckoutProduct
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.app.util.SessionManager
import com.optik.sarimbit.databinding.ActivityPaymentBinding

class PaymentActivity : BaseView() {
    val firestore = FirebaseFirestore.getInstance()
    private val cartProducts = mutableListOf<CheckoutProduct>()
    private lateinit var binding: ActivityPaymentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchCartData()
        binding.payNow.setOnClickListener {
            checkout()
        }
        binding.tvCopy.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", "1330012688875")
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Teks disalin", Toast.LENGTH_SHORT).show()

        }
    }

    private fun checkout() {
        val bundle = intent.extras
        if (bundle != null) {
            val customerName = bundle.getString("customerName")
            val customerPhone = bundle.getString("customerPhone")
            val imageUrl = bundle.getString("imageUrl")
             showLoading("Processing Checkout...")

            val userId = SessionManager.getId(this)
            if (userId.isNullOrEmpty()) {
                showToast("User not logged in")
                hideLoading()
                return
            }
            val batch = firestore.batch()
            val cartRef = firestore.collection("cart").document(userId!!).collection("items")

            for (product in cartProducts) {
                val productRef = cartRef.document(product.id)
                batch.update(productRef, "checkedOut", true) // Update Firestore
            }

            batch.commit().addOnSuccessListener {
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Checkout failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }


            val orderId = getNewDocumentId("orders")
            val totalPrice = cartProducts.sumOf { it.selectedQuantity * it.price }

            val orderData = hashMapOf(
                "userId" to userId,
                "totalPrice" to totalPrice,
                "customerName" to customerName,
                "customerPhone" to customerPhone,
                "receipt" to imageUrl,
                "status" to "Baru",
                "timestamp" to System.currentTimeMillis()
            )

            val orderRef = getDocumentReference("orders", orderId)

            firestore.runTransaction { transaction ->
                transaction.set(orderRef, orderData)

                cartProducts.forEach { product ->
                    val itemRef = orderRef.collection("items").document(product.id)
                    val itemData = hashMapOf(
                        "name" to product.name,
                        "price" to product.price,
                        "frameVariant" to product.frameVariant,
                        "colors" to product.colors,
                        "quantity" to product.selectedQuantity,
                        "productImage" to product.image,
                        "total" to (product.price * product.selectedQuantity)
                    )
                    transaction.set(itemRef, itemData)
                }
            }.addOnSuccessListener {
                hideLoading()
                showToast("Checkout successful!")
                goToPage(PaymentConfirmationActivity::class.java)
            }.addOnFailureListener { e ->
                hideLoading()
                showToast("Checkout failed: ${e.message}")
            }
        }
    }

    private fun getNewDocumentId(collection: String): String {
        return firestore.collection(collection).document().id
    }

    private fun getDocumentReference(collection: String, documentId: String): DocumentReference {
        return firestore.collection(collection).document(documentId)
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
                hideLoading()
            }
            .addOnFailureListener { e ->
                hideLoading()
                showToast("Failed to load cart: ${e.message}")
            }
    }

}