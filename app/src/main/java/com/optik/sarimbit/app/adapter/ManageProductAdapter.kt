package com.optik.sarimbit.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.optik.sarimbit.R
import com.optik.sarimbit.app.model.Product
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class ManageProductAdapter(
    private val productList: List<Product>,
    private val onMoreClick: (Product) -> Unit
) : RecyclerView.Adapter<ManageProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: ImageView = itemView.findViewById(R.id.imageProduct)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvStock: TextView = itemView.findViewById(R.id.tvStock)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val btnMore: ImageView = itemView.findViewById(R.id.btnMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_manage_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun getItemCount(): Int = productList.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        holder.tvName.text = product.name
        holder.tvStock.text = "Stok : ${product.quantity}"
        holder.tvPrice.text = "${convertToCurrency(product.price.toLong())}"
        holder.btnMore.setOnClickListener {
            onMoreClick(product)
        }

        // Load image if available
        if (product.image.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(product.image)
                .placeholder(R.drawable.baseline_image_24)
                .into(holder.imgProduct)
        } else {
            holder.imgProduct.setImageResource(R.drawable.baseline_image_24)
        }
    }
    fun convertToCurrency(price: Long): String {
        val kursIndonesia = DecimalFormat.getCurrencyInstance() as DecimalFormat
        val formatRp = DecimalFormatSymbols()

        formatRp.currencySymbol = "Rp. "
        formatRp.monetaryDecimalSeparator = ','
        formatRp.groupingSeparator = '.'

        kursIndonesia.decimalFormatSymbols = formatRp
        return kursIndonesia.format(price)
    }
}
