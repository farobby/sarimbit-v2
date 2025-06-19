package com.optik.sarimbit.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.optik.sarimbit.app.model.CheckoutOrder
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.databinding.ItemLaporanBinding
import java.text.SimpleDateFormat
import java.util.*

class LaporanAdapter(private var orders: List<CheckoutOrder>) : RecyclerView.Adapter<LaporanAdapter.LaporanViewHolder>() {

    inner class LaporanViewHolder(val binding: ItemLaporanBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LaporanViewHolder {
        val binding = ItemLaporanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LaporanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LaporanViewHolder, position: Int) {
        val order = orders[position]
        val context = holder.itemView.context as BaseView
        val dateFormat = SimpleDateFormat("dd MMMM", Locale("id", "ID"))
        val fullDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val date = Date(order.timestamp)

        val orderIdDisplay = order.orderId.takeLast(6)

        holder.binding.tvOrderDate.text = "${dateFormat.format(date)} - Order #${orderIdDisplay}"
        holder.binding.tvCustomerInfo.text = "${order.customerName} - ${fullDateFormat.format(date)}"
        holder.binding.tvOrderPrice.text = context.convertToCurrency(order.totalPrice.toLong())
    }

    override fun getItemCount(): Int = orders.size

    fun updateData(newOrders: List<CheckoutOrder>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}