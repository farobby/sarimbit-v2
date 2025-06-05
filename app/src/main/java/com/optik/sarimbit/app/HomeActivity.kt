package com.optik.sarimbit.app

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.optik.sarimbit.R
import com.optik.sarimbit.app.fragment.HomeFragment
import com.optik.sarimbit.app.fragment.LocationOptikFragment
import com.optik.sarimbit.app.fragment.ProfileFragment
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.databinding.ActivityHomeBinding

class HomeActivity : BaseView() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Load default fragment (HomeFragment)
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        // Handle navigation selection
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_person -> loadFragment(ProfileFragment())
                R.id.nav_location -> loadFragment(LocationOptikFragment())
                R.id.nav_chat -> {
                    goToPage(ActivityChat::class.java)
                    return@setOnItemSelectedListener false
                }
            }

            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
