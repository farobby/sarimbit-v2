package com.optik.sarimbit.app.fragment

import com.optik.sarimbit.databinding.ActivityLocationOptikBinding


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.QuerySnapshot
import com.optik.sarimbit.app.StoreDetailActivity
import com.optik.sarimbit.app.adapter.StoreAdapter
import com.optik.sarimbit.app.model.Store
import com.optik.sarimbit.app.util.BaseFragment
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.app.util.CallbackFireStore
import com.optik.sarimbit.app.util.Constants
import com.optik.sarimbit.app.util.FireStoreUtil

class LocationOptikFragment : BaseFragment() {

    private var _binding: ActivityLocationOptikBinding? = null
    private val binding get() = _binding!!
    private lateinit var storeAdapter: StoreAdapter
    private val storeList = mutableListOf<Store>()
    private lateinit var firestoreUtil: FireStoreUtil

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = ActivityLocationOptikBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        firestoreUtil = FireStoreUtil(activity as BaseView)
        getUserData()

    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }
    }

    private fun setupSpinner(cities: List<String>) {
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, cities.distinct())
        binding.spinnerCity.adapter = adapter

        binding.spinnerCity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedCity = parent?.getItemAtPosition(position).toString()
                filterStoresByCity(selectedCity)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }
    private fun filterStoresByCity(city: String) {
        if(city=="All"){
            storeAdapter.addData(storeList)
            storeAdapter.notifyDataSetChanged()
            return
        }
        val filteredList = storeList.filter { it.city == city }
        storeAdapter.addData(ArrayList(filteredList))
        storeAdapter.notifyDataSetChanged()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        storeAdapter = StoreAdapter { store ->
            val bundle = Bundle()
            bundle.putSerializable("store", store)
            goToPage(StoreDetailActivity::class.java, bundle)
        }
        binding.recyclerView.adapter = storeAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getUserData() {
        firestoreUtil.readAllData(
            "store",
            object : CallbackFireStore.ReadData {
                override fun onSuccessReadData(document: QuerySnapshot?) {
                    val cities = ArrayList<String>()
                    cities.add("All")
                    document?.forEach {
                        val store = Store()
                        store.name = it.data[Constants.STORE_NAME].toString()
                        store.hours = it.data[Constants.STORE_HOURS].toString()
                        store.phone = it.data[Constants.STORE_PHONE].toString()
                        store.imageUrl = it.data[Constants.STORE_IMAGE_URL].toString()
                        store.address = it.data[Constants.STORE_ADDRESS].toString()
                        store.date = it.data[Constants.STORE_DATE].toString()
                        store.linkGmaps = it.data[Constants.STORE_GMAPS].toString()
                        store.city = it.data[Constants.STORE_CITY].toString()
                        val photos = it.data["photos"] as List<String>
                        store.photos?.addAll(photos)
                        storeList.add(store)
                        cities.add(store.city)
                    }
                    setupSpinner(cities)
                    storeAdapter.addData(storeList as ArrayList<Store>)
                    storeAdapter.notifyDataSetChanged()
                }

                override fun onFailedReadData(message: String?) {
                    showToast(message)
                }
            }
        )
    }

}
