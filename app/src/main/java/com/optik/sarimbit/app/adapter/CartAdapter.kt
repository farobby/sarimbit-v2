package com.optik.sarimbit.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.optik.sarimbit.R
import com.optik.sarimbit.app.model.CheckoutProduct
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.app.util.CartUpdateListener
import com.optik.sarimbit.app.util.SessionManager

class CartAdapter(
    private val context: Context,
    private val productList: MutableList<CheckoutProduct>,
    private val cartUpdateListener: CartUpdateListener
) : RecyclerView.Adapter<CartAdapter.FavoriteViewHolder>() {

    private val firestore = FirebaseFirestore.getInstance()
    private var userId: String? = null

    init {
        userId = SessionManager.getId(context) // Ambil userId dari SessionManager
    }

    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.product_image)
        val productName: TextView = itemView.findViewById(R.id.product_name)
        val productInfo: TextView = itemView.findViewById(R.id.product_lens)
        val productColor: TextView = itemView.findViewById(R.id.product_color)
        val productPrice: TextView = itemView.findViewById(R.id.product_price) // Harga total per item
        val itemQuantity: TextView = itemView.findViewById(R.id.itemQuantity)
        val decreaseButton: ImageView = itemView.findViewById(R.id.decreaseButton)
        val increaseButton: ImageView = itemView.findViewById(R.id.increaseButton)
        val deleteIcon: ImageView = itemView.findViewById(R.id.deleteButton)
        val llTotalItem: LinearLayout = itemView.findViewById(R.id.llTotalItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_item_favorite_product, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val product = productList[position]

        // Load Image
        Glide.with(holder.itemView.context).load(product.image).into(holder.productImage)

        // Set UI Components
        holder.productName.text = product.name
        holder.productName.text = product.name
        holder.productInfo.text = "Lens : ${product.frameVariant}"
        holder.productColor.text = "Color : ${product.colors}"
        holder.productPrice.text = "${(context as BaseView).convertToCurrency(product.price.toLong())}" // Update harga total per item
        holder.itemQuantity.text = "${product.selectedQuantity}"
        holder.llTotalItem.visibility = View.VISIBLE

        // Increase Quantity
        holder.increaseButton.setOnClickListener {
            product.selectedQuantity++
            updateUIAndFirestore(holder, product)
        }

        // Decrease Quantity
        holder.decreaseButton.setOnClickListener {
            if (product.selectedQuantity > 1) {
                product.selectedQuantity--
                updateUIAndFirestore(holder, product)
            }
        }

        // Delete Item
        holder.deleteIcon.setOnClickListener {
            removeProductFromCart(product, position)
        }
    }

    override fun getItemCount(): Int = productList.size

    /**
     * Update UI dan Firestore setelah perubahan quantity
     */
    private fun updateUIAndFirestore(holder: FavoriteViewHolder, product: CheckoutProduct) {
        holder.itemQuantity.text = "${product.selectedQuantity}"
        holder.productPrice.text =
            (context as BaseView).convertToCurrency((product.price * product.selectedQuantity).toLong()) // Update harga total per item

        updateProductQuantity(product)
        notifyCartTotalUpdated()
    }

    /**
     * Update selectedQuantity di Firestore
     */
    private fun updateProductQuantity(product: CheckoutProduct) {
        userId?.let { uid ->
            firestore.collection("cart")
                .document(uid)
                .collection("items")
                .document(product.id)
                .update("selectedQuantity", product.selectedQuantity)
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to update quantity: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    /**
     * Hapus produk dari Firestore
     */
    private fun removeProductFromCart(product: CheckoutProduct, position: Int) {
        userId?.let { uid ->
            firestore.collection("cart")
                .document(uid)
                .collection("items")
                .document(product.id)
                .delete()
                .addOnSuccessListener {
                    productList.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, productList.size)
                    notifyCartTotalUpdated()
                    Toast.makeText(context, "${product.name} removed", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to remove item: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    /**
     * Notifikasi total harga berubah
     */
    private fun notifyCartTotalUpdated() {
        val totalPrice = productList.sumOf { it.price * it.selectedQuantity }
        cartUpdateListener.onCartUpdated(totalPrice.toDouble())
    }
}

