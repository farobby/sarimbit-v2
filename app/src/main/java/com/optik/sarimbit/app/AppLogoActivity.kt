package com.optik.sarimbit.app

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.app.util.SessionManager
import com.optik.sarimbit.databinding.ActivitySplashScreenBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AppLogoActivity  : BaseView() {
    private lateinit var binding: ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navigateAfterDelay()
    }
    private fun navigateAfterDelay() {
        lifecycleScope.launch {
            delay(3000)
            if(SessionManager.getIsLogin(this@AppLogoActivity)){
                if(SessionManager.getIsAdmin(this@AppLogoActivity)){
                    goToPage(ActivityAdminHome::class.java)
                }else{
                    goToPage(HomeActivity::class.java)
                }
            }else{
                goToPage(IntroductionActivity::class.java)
            }
            finish()
        }
    }
}