package com.optik.sarimbit.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.optik.sarimbit.R
import com.optik.sarimbit.app.model.Product
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class FavoriteAdapter(private val productList: List<Product>,val isFavorite : Boolean= false) :
    RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.product_image)
        val productName: TextView = itemView.findViewById(R.id.product_name)
        val productInfo: TextView = itemView.findViewById(R.id.product_lens)
        val productPrice: TextView = itemView.findViewById(R.id.product_price)
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
        Glide.with(holder.itemView.context).load(product.image)
            .into(holder.productImage)
        holder.productName.text = product.name
        holder.productInfo.text = "Lens"
        holder.productPrice.text = "${convertToCurrency(product.price.toLong())}"
        if(isFavorite){
            holder.llTotalItem.visibility=View.VISIBLE
        }else{
            holder.llTotalItem.visibility=View.GONE
        }
//        holder.productImage.setImageResource(product.image)



        // Delete Click Listener
        holder.deleteIcon.setOnClickListener {
            Toast.makeText(holder.itemView.context, "${product.name} removed", Toast.LENGTH_SHORT)
                .show()
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
