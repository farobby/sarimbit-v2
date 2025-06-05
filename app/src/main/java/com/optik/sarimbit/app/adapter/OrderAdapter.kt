package com.optik.sarimbit.app.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.optik.sarimbit.R
import com.optik.sarimbit.app.OrdersDetailAdminActivity
import com.optik.sarimbit.app.model.CheckoutOrder
import com.optik.sarimbit.app.util.TimeConverter
import com.optik.sarimbit.databinding.ItemOrderBinding
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class OrderAdapter(private var orders: List<CheckoutOrder>) :
    RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        val b = holder.binding

        val id = order.timestamp.toString()
        b.tvOrderId.text = "Order #${id.substring(id.length-4, id.length)}"
        b.tvCustomer.text = "${order.customerName} - ${TimeConverter.INSTANCE.getDate(order.timestamp, TimeConverter.INSTANCE.dD_MM_YYYY)}"
        b.tvPrice.text =  "${convertToCurrency(order.totalPrice.toLong())}"

        if(order.status.isEmpty()){
            b.tvStatus.text = "Baru"
        }else{
            b.tvStatus.text = order.status
        }

        when (order.status) {
            "Baru" -> {
                b.tvStatus.setBackgroundResource(R.drawable.bg_status_yellow)
            }
            "Proses" -> {
                b.tvStatus.setBackgroundResource(R.drawable.bg_status_blue)
            }
            "Selesai" -> {
                b.tvStatus.setBackgroundResource(R.drawable.bg_status_green)
            }
        }
        holder.itemView.setOnClickListener {
            holder.itemView.context.let {
                val intent = Intent(it, OrdersDetailAdminActivity::class.java)
                intent.putExtra("id",order.orderId)
                it.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = orders.size

    fun updateList(newList: List<CheckoutOrder>) {
        orders = newList
        notifyDataSetChanged()
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

