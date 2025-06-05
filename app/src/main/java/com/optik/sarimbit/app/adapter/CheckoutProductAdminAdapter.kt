package com.optik.sarimbit.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.optik.sarimbit.R
import com.optik.sarimbit.app.model.CheckoutProduct
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class CheckoutProductAdminAdapter(
    private val productList: List<CheckoutProduct>
) : RecyclerView.Adapter<CheckoutProductAdminAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtQty: TextView = itemView.findViewById(R.id.txtQty)
        val txtPrice: TextView = itemView.findViewById(R.id.txtPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_checkout_product_admin, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.txtName.text = product.name
        holder.txtQty.text = "Jumlah: ${product.selectedQuantity}"
        holder.txtPrice.text = "${convertToCurrency(product.price.toLong())}"

        // Load image from URL (gunakan Glide atau Picasso)
        Glide.with(holder.itemView.context)
            .load(product.image)
                .into(holder.imgProduct)
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
    override fun getItemCount(): Int = productList.size
}
