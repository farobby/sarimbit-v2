package com.optik.sarimbit.app

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.QuerySnapshot
import com.optik.sarimbit.app.adapter.StoreAdapter
import com.optik.sarimbit.app.model.Store
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.app.util.CallbackFireStore
import com.optik.sarimbit.app.util.Constants
import com.optik.sarimbit.databinding.ActivityLocationOptikBinding

class LocationOptikActivity : BaseView() {

    private lateinit var binding: ActivityLocationOptikBinding
    private lateinit var storeAdapter: StoreAdapter
    private val storeList = mutableListOf<Store>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityLocationOptikBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Initialize Spinner

        // Setup RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        storeAdapter = StoreAdapter(){
            goToPage(StoreDetailActivity::class.java)
        }
        binding.recyclerView.adapter = storeAdapter
        getUserData()
    }



    private fun setupSpinner(cities: List<String>) {
        val adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cities.distinct())
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