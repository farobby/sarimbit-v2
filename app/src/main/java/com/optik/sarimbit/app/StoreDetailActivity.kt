package com.optik.sarimbit.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.bumptech.glide.Glide
import com.optik.sarimbit.app.adapter.StoreAdapter
import com.optik.sarimbit.app.model.Store
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.databinding.ActivityStoreDetailBinding

class StoreDetailActivity : BaseView() {

    private lateinit var binding: ActivityStoreDetailBinding
    private lateinit var storeAdapter: StoreAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoreDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        val store = getSerializable(intent, "store", Store::class.java)
        binding.toolbar.title="Lokasi ${store.name}"
        binding.storeName.text = store.name
        binding.storeAddress.text = store.address
        binding.storeHours.text = store.hours
        binding.storePhone.text = store.phone.ifEmpty {
            "-"
        }
        if (store.photos!!.size > 0) {
            Glide.with(this).load(store.photos!![0])
                .into(binding.storeImage)
        }
        binding.btnChat.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${store.phone}"))
            startActivity(intent)
        }
        binding.btnMaps.setOnClickListener {
            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(store.linkGmaps))
            startActivity(mapIntent)
        }
    }
}