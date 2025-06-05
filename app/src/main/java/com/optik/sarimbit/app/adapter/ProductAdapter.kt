package com.optik.sarimbit.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.optik.sarimbit.R
import com.optik.sarimbit.app.model.Product
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class ProductAdapter(private var productList: List<Product>
    , val onclick: (product: Product) -> Unit
    , val onclickFave: (product: Product) -> Unit
    , val onclickCart: (product: Product) -> Unit
)  :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
    fun updateList(newList: List<Product>) {
        productList = newList
        notifyDataSetChanged() // Perbarui RecyclerView
    }

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgProduct: ImageView = view.findViewById(R.id.img_product)
        val txtPrice: TextView = view.findViewById(R.id.txt_price)
        val btnFavorite: ImageView = view.findViewById(R.id.btn_favorite)
        val btnCart: ImageView = view.findViewById(R.id.btn_cart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        Glide.with(holder.itemView.context).load(product.image)
            .into(holder.imgProduct)
        holder.txtPrice.text = "${convertToCurrency(product.price.toLong())}"

        // Click Listeners (Optional)
        holder.btnFavorite.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Added to Wishlist", Toast.LENGTH_SHORT).show()
        }
        holder.btnCart.setOnClickListener {
            onclickCart(product)
        }
        holder.itemView.setOnClickListener {
            onclick(product)
        }
        holder.btnFavorite.setOnClickListener {
            onclickFave(product)
        }
    }

    override fun getItemCount(): Int = productList.size
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
