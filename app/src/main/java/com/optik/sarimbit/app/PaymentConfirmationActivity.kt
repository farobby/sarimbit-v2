package com.optik.sarimbit.app

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.databinding.ActivityPaymentConfirmationBinding


class PaymentConfirmationActivity: BaseView() {
    private lateinit var binding: ActivityPaymentConfirmationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivClose.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.tvConfirm.setOnClickListener {
            val url = "https://api.whatsapp.com/send?phone=(0251)8327758"
            try {
                val pm: PackageManager = getPackageManager()
                pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
                val i = Intent(Intent.ACTION_VIEW)
                i.setData(Uri.parse(url))
                startActivity(i)
            } catch (e: PackageManager.NameNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }
        binding.payNow.setOnClickListener {
            goToPageAndClearPrevious(HomeActivity::class.java)
        }
    }

}