package com.optik.sarimbit.app.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.optik.sarimbit.app.ActivityChat
import com.optik.sarimbit.app.AppLogoActivity
import com.optik.sarimbit.app.HistoryOrderActivity
import com.optik.sarimbit.app.LocationOptikActivity
import com.optik.sarimbit.app.util.BaseFragment
import com.optik.sarimbit.app.util.SessionManager
import com.optik.sarimbit.databinding.FragmentProfileBinding


class ProfileFragment : BaseFragment() {
    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        (requireActivity() as AppCompatActivity).supportActionBar?.title ="Akun Saya"
        binding.btnStoreLocation.setOnClickListener {
            goToPage(LocationOptikActivity::class.java)
        }
        binding.profileName.text=SessionManager.getName(requireContext())
        binding.btnChatbotService.setOnClickListener {
            goToPage(ActivityChat::class.java)
        }
        binding.btnOrders.setOnClickListener {
            goToPage(HistoryOrderActivity::class.java)
        }
        binding.btnLogout.setOnClickListener {
            SessionManager.clearData(requireContext())
            goToPageAndClearPrevious(AppLogoActivity::class.java)
        }
    }
}
