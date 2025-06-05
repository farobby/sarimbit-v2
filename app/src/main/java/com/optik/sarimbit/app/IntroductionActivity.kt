package com.optik.sarimbit.app

import android.os.Bundle
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.databinding.ActivityIntroductionBinding

class IntroductionActivity  : BaseView() {
    private lateinit var binding: ActivityIntroductionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroductionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnNext.setOnClickListener {
            goToPage(ActivityLoginBy::class.java)
        }
    }
}