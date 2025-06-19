package com.optik.sarimbit.app

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.optik.sarimbit.app.adapter.LaporanAdapter
import com.optik.sarimbit.app.model.CheckoutOrder
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.databinding.ActivityDetailLaporanBinding
import java.text.SimpleDateFormat
import java.util.*

class ActivityDetailLaporan : BaseView() {

    private lateinit var binding: ActivityDetailLaporanBinding
    private lateinit var adapter: LaporanAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private var year: Int = 0
    private var month: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        year = intent.getIntExtra("YEAR", 2025)
        month = intent.getIntExtra("MONTH", 4)

        val monthName = SimpleDateFormat("MMMM", Locale("id", "ID")).format(Calendar.getInstance().apply { set(Calendar.MONTH, month-1) }.time)
        binding.tvTitle.text = "Detail Laporan $monthName"

        setupRecyclerView()
        fetchDetailedReport()

        binding.btnKembali.setOnClickListener { finish() }
        binding.btnDownload.setOnClickListener { showToast("Fitur download belum diimplementasikan di halaman ini.") }
    }

    private fun setupRecyclerView() {
        adapter = LaporanAdapter(emptyList())
        binding.rvDetailLaporan.layoutManager = LinearLayoutManager(this)
        binding.rvDetailLaporan.adapter = adapter
    }

    private fun fetchDetailedReport() {
        showLoading("Memuat detail...")
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 0, 0, 0)
        val startTime = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val endTime = cal.timeInMillis

        firestore.collection("orders")
            .whereGreaterThanOrEqualTo("timestamp", startTime)
            .whereLessThan("timestamp", endTime)
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { documents ->
                hideLoading()
                val orders = documents.toObjects(CheckoutOrder::class.java)
                adapter.updateData(orders)
                binding.tvDisplaying.text = "Menampilkan ${orders.size} dari ${orders.size} pesanan."
            }
            .addOnFailureListener { e ->
                hideLoading()
                showToast("Gagal memuat detail: ${e.message}")
            }
    }
}