package com.optik.sarimbit.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.optik.sarimbit.R
import com.optik.sarimbit.app.model.CheckoutProduct
import com.optik.sarimbit.app.util.BaseView

class CheckoutProductAdapter(
    private val context: Context,
    private val productList: List<CheckoutProduct>
) : RecyclerView.Adapter<CheckoutProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.product_image)
        val productName: TextView = itemView.findViewById(R.id.product_name)
        val productLens: TextView = itemView.findViewById(R.id.product_lens)
        val productColor: TextView = itemView.findViewById(R.id.product_color)
        val quantity: TextView = itemView.findViewById(R.id.qtty)
        val productPrice: TextView = itemView.findViewById(R.id.product_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_checkout_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        Glide.with(context)
            .load(product.image)
            .into(holder.productImage)

        holder.productName.text = product.name
        holder.productLens.text = "Lens : ${product.frameVariant}"
        holder.productColor.text = "Color : ${product.colors}"
        holder.quantity.text = "Qty : ${product.selectedQuantity}"
        holder.quantity.text = "Qty : ${product.selectedQuantity}"
        holder.productPrice.text = "${(context as BaseView).convertToCurrency((product.selectedQuantity*product.price).toLong())}" // Update harga total per item

    }

    override fun getItemCount(): Int = productList.size
}
