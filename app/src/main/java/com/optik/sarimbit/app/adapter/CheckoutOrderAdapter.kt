package com.optik.sarimbit.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.optik.sarimbit.R
import com.optik.sarimbit.app.model.CheckoutOrder
import com.optik.sarimbit.app.util.TimeConverter

class CheckoutOrderAdapter(
    private val context: Context,
    private val orderList: List<CheckoutOrder> // 1 order = 1 tanggal + list produk
) : RecyclerView.Adapter<CheckoutOrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderDate: TextView = itemView.findViewById(R.id.tv_order_date)
        val estimatePickUp: TextView = itemView.findViewById(R.id.tv_estimate_pick_up)
        val productsRecyclerView: RecyclerView = itemView.findViewById(R.id.rv_products)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_ite_checkout_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orderList[position]

        holder.orderDate.text = "Order Date : ${TimeConverter.INSTANCE.getDate(order.timestamp, TimeConverter.INSTANCE.dD__MM__YYYY)}" // contoh field
        holder.estimatePickUp.text = "Estimate Pick Up : ${TimeConverter.INSTANCE.getDatePlusTwoDays(order.timestamp, TimeConverter.INSTANCE.dD__MM__YYYY)}" // contoh field

        // Setup inner RecyclerView
        holder.productsRecyclerView.layoutManager = LinearLayoutManager(context)
        holder.productsRecyclerView.adapter = CheckoutProductAdapter(context, order.items)
    }

    override fun getItemCount(): Int = orderList.size
}

