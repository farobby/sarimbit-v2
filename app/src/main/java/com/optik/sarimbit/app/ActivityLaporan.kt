package com.optik.sarimbit.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.firestore.FirebaseFirestore
import com.optik.sarimbit.app.model.CheckoutOrder
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.databinding.ActivityLaporanBinding
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ActivityLaporan : BaseView() {
    private lateinit var binding: ActivityLaporanBinding
    private val firestore = FirebaseFirestore.getInstance()
    private var selectedYear = 2025
    private var selectedMonth = 4 // April (contoh)
    private val monthlyOrders = mutableListOf<CheckoutOrder>()

    private val saveFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
        uri?.let {
            writeDataToUri(it)
        } ?: showToast("Penyimpanan file dibatalkan.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tidak perlu setupPermissions() lagi
        setupSpinner()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.ivClose.setOnClickListener { finish() }

        binding.btnLihatDetail.setOnClickListener {
            val intent = Intent(this, ActivityDetailLaporan::class.java).apply {
                putExtra("YEAR", selectedYear)
                putExtra("MONTH", selectedMonth)
            }
            startActivity(intent)
        }

        binding.btnDownloadExcel.setOnClickListener {
            val monthName = SimpleDateFormat("MMMM", Locale("id", "ID")).format(Calendar.getInstance().apply { set(Calendar.MONTH, selectedMonth - 1) }.time)
            val fileName = "Laporan_${monthName}_$selectedYear.csv"
            saveFileLauncher.launch(fileName)
        }

        binding.btnDelete.setOnClickListener {
            showToast("Fitur hapus data belum diimplementasikan")
        }
    }

    private fun setupSpinner() {
        val months = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months.map { "$it $selectedYear" })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMonth.adapter = adapter
        binding.spinnerMonth.setSelection(selectedMonth - 1)

        binding.spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedMonth = position + 1
                fetchReportData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun fetchReportData() {
        showLoading("Mengambil data...")
        val cal = Calendar.getInstance()
        cal.set(selectedYear, selectedMonth - 1, 1, 0, 0, 0)
        val startTime = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val endTime = cal.timeInMillis

        firestore.collection("orders")
            .whereGreaterThanOrEqualTo("timestamp", startTime)
            .whereLessThan("timestamp", endTime)
            .get()
            .addOnSuccessListener { documents ->
                hideLoading()
                monthlyOrders.clear()
                var totalPendapatan = 0L
                var pesananSelesai = 0

                for (doc in documents) {
                    val order = doc.toObject(CheckoutOrder::class.java)
                    monthlyOrders.add(order)
                    totalPendapatan += order.totalPrice
                    if (order.status == "Selesai") {
                        pesananSelesai++
                    }
                }
                updateSummaryUI(monthlyOrders.size, totalPendapatan, pesananSelesai)
            }
            .addOnFailureListener { e ->
                hideLoading()
                showToast("Gagal mengambil data: ${e.message}")
            }
    }

    private fun updateSummaryUI(totalPesanan: Int, totalPendapatan: Long, pesananSelesai: Int) {
        val monthName = SimpleDateFormat("MMMM", Locale("id", "ID")).format(Calendar.getInstance().apply { set(Calendar.MONTH, selectedMonth - 1) }.time)
        binding.tvSummaryTitle.text = "Ringkasan $monthName $selectedYear"
        binding.tvTotalPesanan.text = totalPesanan.toString()
        binding.tvTotalPendapatan.text = convertToCurrency(totalPendapatan)
        binding.tvPesananSelesai.text = pesananSelesai.toString()
    }

    private fun generateCsvContent(): String? {
        if (monthlyOrders.isEmpty()) {
            showToast("Tidak ada data untuk diekspor.")
            return null
        }
        val csvHeader = "OrderID,Tanggal,Nama Pelanggan,Status,Total Harga\n"
        val stringBuilder = StringBuilder().append(csvHeader)
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())

        for (order in monthlyOrders) {
            val date = dateFormat.format(Date(order.timestamp))
            val customerName = order.customerName.replace(",", ";")
            stringBuilder.append("${order.orderId},$date,$customerName,${order.status},${order.totalPrice}\n")
        }
        return stringBuilder.toString()
    }

    private fun writeDataToUri(uri: Uri) {
        try {
            val content = generateCsvContent()
            if (content != null) {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(content.toByteArray())
                }
                showToast("Laporan berhasil disimpan!")
            }
        } catch (e: IOException) {
            showToast("Gagal menyimpan file: ${e.message}")
            e.printStackTrace()
        }
    }
}