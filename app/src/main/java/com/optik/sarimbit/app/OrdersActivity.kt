package com.optik.sarimbit.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.optik.sarimbit.R
import com.optik.sarimbit.app.adapter.OrderAdapter
import com.optik.sarimbit.app.model.CheckoutOrder
import com.optik.sarimbit.app.model.CheckoutProduct
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.app.util.PermissionUtility
import com.optik.sarimbit.app.util.TimeConverter
import com.optik.sarimbit.databinding.ActivityOrdersAdminBinding
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class OrdersActivity : BaseView() {

    private lateinit var binding: ActivityOrdersAdminBinding
    private lateinit var adapter: OrderAdapter
    private val allOrders = mutableListOf<CheckoutOrder>()
    private val displayedOrders = mutableListOf<CheckoutOrder>() // Tambahkan ini untuk menyimpan pesanan yang sedang ditampilkan
    private var permissionUtility: PermissionUtility? = null
    private var permissionUtility13: PermissionUtility? = null
    private val PERMISSIONS = arrayListOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE //
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val PERMISSIONS_13 = arrayListOf(
        Manifest.permission.READ_MEDIA_IMAGES //
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi adapter dengan `displayedOrders` agar filter dapat mengubah daftar yang ditampilkan
        adapter = OrderAdapter(displayedOrders)
        binding.orderRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.orderRecyclerView.adapter = adapter

        binding.btnAll.setOnClickListener {
            displayedOrders.clear()
            displayedOrders.addAll(allOrders)
            adapter.updateList(displayedOrders)
            setActiveFilterButton(binding.btnAll)
        }

        binding.btnNew.setOnClickListener {
            displayedOrders.clear()
            displayedOrders.addAll(allOrders.filter { it.status == "Baru" })
            adapter.updateList(displayedOrders)
            setActiveFilterButton(binding.btnNew)
        }

        binding.btnDone.setOnClickListener {
            displayedOrders.clear()
            displayedOrders.addAll(allOrders.filter { it.status == "Selesai" })
            adapter.updateList(displayedOrders)
            setActiveFilterButton(binding.btnDone)
        }

        // Default: tampilkan semua pesanan
        binding.btnAll.performClick()
        loadAllOrders()

        binding.ivClose.setOnClickListener {
            finish()
        }

        // Inisialisasi PermissionUtility
        permissionUtility = PermissionUtility(this, PERMISSIONS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { //
            permissionUtility13 = PermissionUtility(this, PERMISSIONS_13) //
        }

        binding.btnDownloadMonthly.setOnClickListener { // Tombol untuk download bulanan
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { //
                permissionAos13(DOWNLOAD_MONTHLY_CODE) //
            } else {
                permissionAos(DOWNLOAD_MONTHLY_CODE) //
            }
        }

        binding.btnDownloadYearly.setOnClickListener { // Tombol untuk download tahunan
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { //
                permissionAos13(DOWNLOAD_YEARLY_CODE) //
            } else {
                permissionAos(DOWNLOAD_YEARLY_CODE) //
            }
        }
    }

    // Kode ini di luar onCreate
    val firestore = FirebaseFirestore.getInstance()

    private fun loadAllOrders() {
        showLoading("Loading...");
        val ordersRef = firestore.collection("orders")

        ordersRef.get()
            .addOnSuccessListener { ordersSnapshot ->
                hideLoading()
                if (ordersSnapshot.isEmpty) {
                    showToast("No orders found")
                    return@addOnSuccessListener
                }

                allOrders.clear()
                for (orderDoc in ordersSnapshot.documents) {
                    val orderId = orderDoc.id
                    val totalPrice = (orderDoc.getLong("totalPrice") ?: 0).toInt()
                    val customerName = orderDoc.getString("customerName") ?: ""
                    val status = orderDoc.getString("status") ?: ""
                    val customerPhone = orderDoc.getString("customerPhone") ?: ""
                    val receipt = orderDoc.getString("receipt") ?: "" // Ambil URL receipt
                    val timestamp = orderDoc.getLong("timestamp") ?: 0L

                    orderDoc.reference.collection("items").get()
                        .addOnSuccessListener { itemsSnapshot ->
                            val itemsList = mutableListOf<CheckoutProduct>()
                            for (itemDoc in itemsSnapshot.documents) {
                                val item = CheckoutProduct(
                                    id = itemDoc.id,
                                    name = itemDoc.getString("name") ?: "",
                                    price = (itemDoc.getLong("price") ?: 0).toInt(),
                                    frameVariant = itemDoc.getString("frameVariant") ?: "",
                                    colors = itemDoc.getString("colors") ?: "",
                                    image = itemDoc.getString("productImage") ?: "",
                                    selectedQuantity = (itemDoc.getLong("quantity") ?: 0).toInt(),
                                    totalPrice = (itemDoc.getLong("total") ?: 0).toInt()
                                )
                                itemsList.add(item)
                            }

                            val orderData = CheckoutOrder(
                                orderId = orderId,
                                userId = "userId", // Sesuaikan jika ada userId di sini
                                totalPrice = totalPrice,
                                customerName = customerName,
                                customerPhone = customerPhone,
                                status = status,
                                receipt = receipt, // Simpan URL receipt
                                timestamp = timestamp,
                                items = itemsList
                            )
                            allOrders.add(orderData)

                            // Setelah semua data dimuat, perbarui RecyclerView
                            if (allOrders.size == ordersSnapshot.size()) {
                                allOrders.sortByDescending { it.timestamp } // Urutkan berdasarkan timestamp terbaru
                                displayedOrders.clear()
                                displayedOrders.addAll(allOrders)
                                adapter.updateList(displayedOrders)
                            }
                        }
                        .addOnFailureListener { e ->
                            showToast("Failed to load items for order $orderId: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                hideLoading()
                showToast("Failed to load orders: ${e.message}")
            }
    }

    private fun setActiveFilterButton(activeButton: TextView) {
        val buttons = listOf(binding.btnAll, binding.btnNew, binding.btnDone)
        buttons.forEach { button ->
            if (button == activeButton) {
                button.setTextColor(ContextCompat.getColor(this, R.color.white))
                button.background = ContextCompat.getDrawable(this, R.drawable.bg_tab_selected)
            } else {
                button.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                button.background = ContextCompat.getDrawable(this, R.drawable.bg_tab_unselected)
            }
        }
    }

    private fun permissionAos13(requestCode: Int) { //
        if (permissionUtility13!!.arePermissionsEnabled()) { //
            handleDownloadRequest(requestCode) //
        } else {
            permissionUtility13!!.requestMultiplePermissions() //
        }
    }

    private fun permissionAos(requestCode: Int) { //
        if (permissionUtility!!.arePermissionsEnabled()) { //
            handleDownloadRequest(requestCode) //
        } else {
            permissionUtility!!.requestMultiplePermissions() //
        }
    }

    override fun onRequestPermissionsResult( //
        requestCode: Int, //
        permissions: Array<out String>, //
        grantResults: IntArray //
    ) { //
        super.onRequestPermissionsResult(requestCode, permissions, grantResults) //
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { //
            if (!permissionUtility13!!.onRequestPermissionsResult( //
                    requestCode, //
                    permissions, //
                    grantResults //
                ) //
            ) { //
                showToast("Permission denied to read media.") //
                return //
            } //
        } else {
            if (!permissionUtility!!.onRequestPermissionsResult( //
                    requestCode, //
                    permissions, //
                    grantResults //
                ) //
            ) { //
                showToast("Permission denied to write external storage.") //
                return //
            } //
        }
        handleDownloadRequest(requestCode) //
    }

    private fun handleDownloadRequest(requestCode: Int) { //
        when (requestCode) {
            DOWNLOAD_MONTHLY_CODE -> downloadMonthlyReport() //
            DOWNLOAD_YEARLY_CODE -> downloadYearlyReport() //
        }
    }

    private fun downloadMonthlyReport() {
        if (displayedOrders.isEmpty()) {
            showToast("Tidak ada data untuk diunduh bulan ini.")
            return
        }

        // Asumsi displayedOrders sudah terfilter berdasarkan bulan yang sedang dilihat
        val calendar = Calendar.getInstance()
        if (displayedOrders.isNotEmpty()) {
            calendar.timeInMillis = displayedOrders[0].timestamp
        }
        val monthYearFormat = SimpleDateFormat("MMMM_yyyy", Locale.getDefault())
        val fileName = "Laporan_Bulanan_${monthYearFormat.format(calendar.time)}.csv"
        exportOrdersToCsv(displayedOrders, fileName)
    }

    private fun downloadYearlyReport() {
        if (allOrders.isEmpty()) {
            showToast("Tidak ada data untuk diunduh tahun ini.")
            return
        }

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val yearlyOrders = allOrders.filter {
            val orderCalendar = Calendar.getInstance()
            orderCalendar.timeInMillis = it.timestamp
            orderCalendar.get(Calendar.YEAR) == currentYear
        }

        if (yearlyOrders.isEmpty()) {
            showToast("Tidak ada pesanan untuk tahun ${currentYear}.")
            return
        }

        val fileName = "Laporan_Tahunan_${currentYear}.csv"
        exportOrdersToCsv(yearlyOrders, fileName)
    }

    private fun exportOrdersToCsv(orders: List<CheckoutOrder>, fileName: String) {
        val header = arrayOf( //
            "Order ID", //
            "Customer Name", //
            "Customer Phone", //
            "Order Date", //
            "Total Price", //
            "Status", //
            "Receipt URL", //
            "Product Name", //
            "Product Price", //
            "Frame Variant", //
            "Color", //
            "Quantity" //
        ) //

        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) //
            val file = File(downloadsDir, fileName) //
            FileWriter(file).use { writer -> //
                val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(*header)) //
                for (order in orders) { //
                    for (item in order.items) { //
                        csvPrinter.printRecord( //
                            order.orderId, //
                            order.customerName, //
                            order.customerPhone, //
                            TimeConverter.INSTANCE.getDate(order.timestamp, TimeConverter.INSTANCE.dD_MM_YYYY), //
                            order.totalPrice, //
                            order.status, //
                            order.receipt, //
                            item.name, //
                            item.price, //
                            item.frameVariant, //
                            item.colors, //
                            item.selectedQuantity //
                        ) //
                    }
                }
                csvPrinter.flush() //
            }
            showToast("Laporan berhasil diunduh ke ${file.absolutePath}")
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Gagal mengunduh laporan: ${e.message}")
        }
    }

    companion object {
        private const val DOWNLOAD_MONTHLY_CODE = 101 //
        private const val DOWNLOAD_YEARLY_CODE = 102 //
    }
}