package com.optik.sarimbit.app
import android.os.Bundle
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.databinding.ActivityLoginByBinding

class ActivityLoginBy  : BaseView() {
    private lateinit var binding: ActivityLoginByBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginByBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.llAdmin.setOnClickListener {
            goToPage(LoginAdminActivity::class.java)
        }
        binding.llUser.setOnClickListener {
            goToPage(LoginActivity::class.java)
        }
    }
}