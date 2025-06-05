package com.optik.sarimbit.app

import android.os.Bundle
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.app.util.SessionManager
import com.optik.sarimbit.databinding.ActivityAdminHomeBinding

class ActivityAdminHome: BaseView() {
    private lateinit var binding: ActivityAdminHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnUploadProduk.setOnClickListener {
            goToPage(UploadProductActivity::class.java)
        }
        binding.btnKelolaPesanan.setOnClickListener {
            goToPage(OrdersActivity::class.java)
        }
        binding.btnKelolaProduk.setOnClickListener {
            goToPage(ManageProductActivity::class.java)
        }
        binding.ivLogOut.setOnClickListener {
            SessionManager.clearData(this)
            goToPageAndClearPrevious(AppLogoActivity::class.java)
        }
    }
}