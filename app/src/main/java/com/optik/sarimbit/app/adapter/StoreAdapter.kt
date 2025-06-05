package com.optik.sarimbit.app.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.optik.sarimbit.app.model.Store
import com.optik.sarimbit.databinding.RvItemStoreBinding


class StoreAdapter(val onclick: (store: Store) -> Unit) :
    RecyclerView.Adapter<StoreAdapter.StoreViewHolder>() {
    private var stores: ArrayList<Store> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
        val binding = RvItemStoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoreViewHolder(binding)
    }

    fun addData(stores: MutableList<Store>) {
        this.stores.clear()
        this.stores.addAll(stores)
    }

    fun getData() = stores

    override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
        holder.bind(stores[position])
        holder.itemView.setOnClickListener {
            onclick(stores[position])
        }
    }

    override fun getItemCount(): Int = stores.size

    class StoreViewHolder(private val binding: RvItemStoreBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(store: Store) {
            binding.storeName.text = store.name
            binding.storeHours.text = store.hours
            binding.storePhone.text = store.phone.ifEmpty {
                "-"
            }
            if (store.photos!!.size > 0) {
                Glide.with(itemView.context).load(store.photos!![0])
                    .into(binding.storeImage)
            }
            binding.btnChat.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${store.phone}"))
                itemView.context.startActivity(intent)
            }
            binding.btnMaps.setOnClickListener {
                val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(store.linkGmaps))
                itemView.context.startActivity(mapIntent)
            }

            // Load Image using Glide or Coil if needed
        }
    }
}

