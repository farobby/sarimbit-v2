// app/src/main/java/com/optik/sarimbit/app/ActivityLaporan.kt

package com.optik.sarimbit.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.firestore.FirebaseFirestore
import com.optik.sarimbit.app.model.CheckoutOrder
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.app.util.PermissionUtility // Import PermissionUtility
import com.optik.sarimbit.app.util.TimeConverter
import com.optik.sarimbit.databinding.ActivityLaporanBinding
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ActivityLaporan : BaseView() {
    private lateinit var binding: ActivityLaporanBinding
    private val firestore = FirebaseFirestore.getInstance()
    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR) // Tahun saat ini sebagai default
    private var selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1 // Bulan saat ini sebagai default
    private val monthlyOrders = mutableListOf<CheckoutOrder>()
    private val yearlyOrders = mutableListOf<CheckoutOrder>() // Tambahkan ini untuk menyimpan data satu tahun

    private var permissionUtility: PermissionUtility? = null // Deklarasi untuk permission
    private var permissionUtility13: PermissionUtility? = null // Deklarasi untuk permission API 33+
    private val PERMISSIONS = arrayListOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE // Izin untuk menulis ke penyimpanan eksternal
    )

    private val saveFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
        uri?.let {
            // Tentukan apakah ini download bulanan atau tahunan berdasarkan data yang ada
            if (currentDownloadType == DOWNLOAD_MONTHLY_CODE) {
                writeDataToUri(it, monthlyOrders)
            } else if (currentDownloadType == DOWNLOAD_YEARLY_CODE) {
                writeDataToUri(it, yearlyOrders)
            }
        } ?: showToast("Penyimpanan file dibatalkan.")
    }

    private var currentDownloadType: Int = 0 // Untuk melacak jenis unduhan (bulanan/tahunan)

    companion object {
        private const val DOWNLOAD_MONTHLY_CODE = 1 // Kode untuk unduhan bulanan
        private const val DOWNLOAD_YEARLY_CODE = 2 // Kode untuk unduhan tahunan
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi PermissionUtility
        permissionUtility = PermissionUtility(this, PERMISSIONS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionUtility13 = PermissionUtility(this, arrayListOf(android.Manifest.permission.READ_MEDIA_IMAGES)) // Untuk API 33+
        }

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
            // Meminta izin sebelum mengunduh laporan tahunan
            currentDownloadType = DOWNLOAD_YEARLY_CODE // Set jenis unduhan
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionAos13(DOWNLOAD_YEARLY_CODE)
            } else {
                permissionAos(DOWNLOAD_YEARLY_CODE)
            }
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
        showLoading("Mengambil data bulanan...")
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

    // Fungsi untuk mengunduh laporan tahunan
    private fun downloadYearlyReport() {
        showLoading("Mengambil data tahunan...")
        val calStart = Calendar.getInstance()
        calStart.set(selectedYear, 0, 1, 0, 0, 0) // Awal tahun
        val startTime = calStart.timeInMillis

        val calEnd = Calendar.getInstance()
        calEnd.set(selectedYear, 11, 31, 23, 59, 59) // Akhir tahun
        val endTime = calEnd.timeInMillis

        firestore.collection("orders")
            .whereGreaterThanOrEqualTo("timestamp", startTime)
            .whereLessThanOrEqualTo("timestamp", endTime)
            .get()
            .addOnSuccessListener { documents ->
                hideLoading()
                yearlyOrders.clear()
                for (doc in documents) {
                    val order = doc.toObject(CheckoutOrder::class.java)
                    yearlyOrders.add(order)
                }

                if (yearlyOrders.isEmpty()) {
                    showToast("Tidak ada data untuk diunduh pada tahun ini.")
                    return@addOnSuccessListener
                }

                val fileName = "Laporan_Tahunan_${selectedYear}.csv"
                saveFileLauncher.launch(fileName)
            }
            .addOnFailureListener { e ->
                hideLoading()
                showToast("Gagal mengambil data tahunan: ${e.message}")
            }
    }

    // Fungsi untuk menulis data ke URI yang dipilih pengguna
    private fun writeDataToUri(uri: Uri, ordersToExport: List<CheckoutOrder>) {
        try {
            val content = generateCsvContent(ordersToExport)
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

    // Fungsi untuk menghasilkan konten CSV
    private fun generateCsvContent(orders: List<CheckoutOrder>): String? {
        if (orders.isEmpty()) {
            return null
        }
        val header = arrayOf(
            "OrderID", "Tanggal", "Nama Pelanggan", "Nomor Telepon", "Status", "Total Harga",
            "Nama Produk", "Harga Produk Satuan", "Varian Frame", "Warna", "Kuantitas"
        )
        val stringBuilder = StringBuilder()
        val csvPrinter = CSVPrinter(stringBuilder, CSVFormat.DEFAULT.withHeader(*header))
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())

        for (order in orders) {
            val date = dateFormat.format(Date(order.timestamp))
            val customerName = order.customerName.replace(",", ";") // Ganti koma agar tidak merusak CSV

            // Iterasi melalui setiap produk dalam pesanan
            for (item in order.items) {
                csvPrinter.printRecord(
                    order.orderId,
                    date,
                    customerName,
                    order.customerPhone,
                    order.status,
                    order.totalPrice,
                    item.name,
                    item.price,
                    item.frameVariant,
                    item.colors,
                    item.selectedQuantity
                )
            }
        }
        csvPrinter.flush()
        return stringBuilder.toString()
    }

    private fun permissionAos13(requestCode: Int) {
        if (permissionUtility13!!.arePermissionsEnabled()) {
            handleDownloadRequest(requestCode)
        } else {
            permissionUtility13!!.requestMultiplePermissions()
        }
    }

    private fun permissionAos(requestCode: Int) {
        if (permissionUtility!!.arePermissionsEnabled()) {
            handleDownloadRequest(requestCode)
        } else {
            permissionUtility!!.requestMultiplePermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!permissionUtility13!!.onRequestPermissionsResult(
                    requestCode,
                    permissions,
                    grantResults
                )
            ) {
                showToast("Permission denied to read media.")
                return
            }
        } else {
            if (!permissionUtility!!.onRequestPermissionsResult(
                    requestCode,
                    permissions,
                    grantResults
                )
            ) {
                showToast("Permission denied to write external storage.")
                return
            }
        }
        handleDownloadRequest(requestCode)
    }

    private fun handleDownloadRequest(requestCode: Int) {
        when (requestCode) {
            DOWNLOAD_YEARLY_CODE -> downloadYearlyReport() // Panggil downloadYearlyReport untuk tombol "Download Excel"
        }
    }
}