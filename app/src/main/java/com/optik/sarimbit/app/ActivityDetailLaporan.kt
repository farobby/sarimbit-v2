// app/src/main/java/com/optik/sarimbit/app/ActivityDetailLaporan.kt

package com.optik.sarimbit.app

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.optik.sarimbit.app.adapter.LaporanAdapter
import com.optik.sarimbit.app.model.CheckoutOrder
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.app.util.PermissionUtility // Import PermissionUtility
import com.optik.sarimbit.app.util.TimeConverter
import com.optik.sarimbit.databinding.ActivityDetailLaporanBinding
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ActivityDetailLaporan : BaseView() {

    private lateinit var binding: ActivityDetailLaporanBinding
    private lateinit var adapter: LaporanAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private var year: Int = 0
    private var month: Int = 0
    private val currentMonthOrders = mutableListOf<CheckoutOrder>() // Untuk menyimpan data bulan yang ditampilkan

    private var permissionUtility: PermissionUtility? = null // Deklarasi untuk permission
    private var permissionUtility13: PermissionUtility? = null // Deklarasi untuk permission API 33+
    private val PERMISSIONS = arrayListOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE // Izin untuk menulis ke penyimpanan eksternal
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val PERMISSIONS_13 = arrayListOf(
        android.Manifest.permission.READ_MEDIA_IMAGES // Untuk API 33+
    )

    private val saveFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
        uri?.let {
            writeDataToUri(it, currentMonthOrders)
        } ?: showToast("Penyimpanan file dibatalkan.")
    }

    companion object {
        private const val DOWNLOAD_CODE = 100 // Kode untuk unduhan di DetailLaporan
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        year = intent.getIntExtra("YEAR", Calendar.getInstance().get(Calendar.YEAR))
        month = intent.getIntExtra("MONTH", Calendar.getInstance().get(Calendar.MONTH) + 1) // Default bulan saat ini

        val monthName = SimpleDateFormat("MMMM", Locale("id", "ID")).format(Calendar.getInstance().apply { set(Calendar.MONTH, month-1) }.time)
        binding.tvTitle.text = "Detail Laporan $monthName $year" // Tambahkan tahun ke judul

        setupRecyclerView()
        fetchDetailedReport()

        binding.btnKembali.setOnClickListener { finish() }
        binding.btnDownload.setOnClickListener {
            // Meminta izin sebelum mengunduh laporan bulanan
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionAos13(DOWNLOAD_CODE)
            } else {
                permissionAos(DOWNLOAD_CODE)
            }
        }

        // Inisialisasi PermissionUtility
        permissionUtility = PermissionUtility(this, PERMISSIONS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionUtility13 = PermissionUtility(this, PERMISSIONS_13)
        }
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
                currentMonthOrders.clear() // Kosongkan sebelum mengisi
                val orders = documents.toObjects(CheckoutOrder::class.java)
                currentMonthOrders.addAll(orders) // Simpan data yang diambil
                adapter.updateData(orders)
                binding.tvDisplaying.text = "Menampilkan ${orders.size} dari ${orders.size} pesanan."
            }
            .addOnFailureListener { e ->
                hideLoading()
                showToast("Gagal memuat detail: ${e.message}")
            }
    }

    // Fungsi untuk mengunduh laporan bulanan
    private fun downloadMonthlyReport() {
        if (currentMonthOrders.isEmpty()) {
            showToast("Tidak ada data untuk diunduh bulan ini.")
            return
        }

        val monthName = SimpleDateFormat("MMMM_yyyy", Locale("id", "ID")).format(Calendar.getInstance().apply { set(Calendar.MONTH, month - 1); set(Calendar.YEAR, year) }.time)
        val fileName = "Laporan_Bulanan_${monthName}.csv"
        saveFileLauncher.launch(fileName)
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
            val customerName = order.customerName.replace(",", ";")

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
            DOWNLOAD_CODE -> downloadMonthlyReport() // Panggil downloadMonthlyReport untuk tombol "Download"
        }
    }
}