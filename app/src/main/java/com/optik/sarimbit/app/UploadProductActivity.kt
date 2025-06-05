package com.optik.sarimbit.app


import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.optik.sarimbit.app.adapter.CategoryAdminAdapter
import com.optik.sarimbit.app.model.Product
import com.optik.sarimbit.app.model.VariantModel
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.app.util.PermissionUtility
import com.optik.sarimbit.app.util.TimeConverter
import com.optik.sarimbit.databinding.ActivityUploadProductBinding

class UploadProductActivity : BaseView() {
    private lateinit var categoryFrameAdapter: CategoryAdminAdapter
    private var permissionUtility: PermissionUtility? = null
    private var permissionUtility13: PermissionUtility? = null
    private val PERMISSIONS = arrayListOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val PERMISSIONS_13 = arrayListOf(
        Manifest.permission.READ_MEDIA_IMAGES
    )
    private var filePath: Uri? = null
    private var storageReference: StorageReference? = null

    private lateinit var binding: ActivityUploadProductBinding
    override fun onGetData(intent: Intent?, requestCode: Int) {
        if (requestCode == 100) {
            filePath = intent?.data
            try {
                val bitmap = MediaStore.Images.Media
                    .getBitmap(
                        contentResolver,
                        filePath
                    )
                binding.llUploadImage.hideView()
                binding.ivImage.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categoryList = listOf("Pilih Kategori", "Designer", "Kids", "Sport")
        val colorList = listOf("Hitam", "Putih", "Merah", "Biru")

        binding.spinnerCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoryList)
        binding.spinnerColor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, colorList)

        binding.layoutUploadImage.setOnClickListener {
            Toast.makeText(this, "Upload foto belum diimplementasi", Toast.LENGTH_SHORT).show()
        }
        permissionUtility = PermissionUtility(this, PERMISSIONS)
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        storageReference = storage.reference
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionUtility13 = PermissionUtility(this, PERMISSIONS_13)
        }
        binding.layoutUploadImage.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionAos13()
            } else {
                permissionAos()
            }
        }
        binding.btnUpload.setOnClickListener {
            if (isValidInput()) {
                uploadImage()
            }
        }

        binding.btnClose.setOnClickListener {
            finish()
        }

        val categories = listOf(
            "round",
            "aviatar",
            "oversized",
            "rimless",
            "rectangle",
            "cat eye",
            "browline",
            "tinted",
            "retro",
            "metallic",
            "wooden",
            "clear"
        )
        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.flexWrap = FlexWrap.WRAP

        binding.recyclerCategories.layoutManager = layoutManager
        categoryFrameAdapter = CategoryAdminAdapter(categories) { selectedCategories ->
        }
        binding.recyclerCategories.adapter = categoryFrameAdapter
    }
    private fun permissionAos() {
        if (permissionUtility!!.arePermissionsEnabled()) {
            selectImage()
        } else {
            permissionUtility!!.requestMultiplePermissions()
        }
    }
    private fun permissionAos13() {
        if (permissionUtility13!!.arePermissionsEnabled()) {
            selectImage()
        } else {
            permissionUtility13!!.requestMultiplePermissions()
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
                permissionUtility13!!.requestMultiplePermissions()
                return
            }
        } else {
            if (!permissionUtility!!.onRequestPermissionsResult(
                    requestCode,
                    permissions,
                    grantResults
                )
            ) {
                permissionUtility!!.requestMultiplePermissions()
                return
            }
        }
        selectImage()
    }
    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        goToPageActivityResult(intent, 100)
    }
    private fun uploadImage() {
        if (filePath != null) {
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading...")
            progressDialog.show()
            val date: String = TimeConverter.INSTANCE.getDate(
                System.currentTimeMillis(),
                TimeConverter.INSTANCE.yyyY_MM_DD_HH_MM_SS
            )
            val ref: StorageReference = storageReference!!.child("images/image_owner_$date")
            ref.putFile(filePath!!)
                .addOnSuccessListener { _: UploadTask.TaskSnapshot? ->
                    progressDialog.dismiss()
                    ref.downloadUrl
                        .addOnCompleteListener { task: Task<Uri> ->
                            showLoading("")
                            val imageUrl = task.result.toString()
                            val product = Product(
                                imageUrl,
                                binding.etPrice.getValue().toInt(), "",
                                binding.etProductName.getValue(),
                                binding.etBrand.getValue(),
                                "Description Kaca mata ",
                                binding.spinnerCategory.selectedItem.toString(),
                                arrayListOf(binding.spinnerColor.selectedItem.toString()),
                                categoryFrameAdapter.getSelectedCategories(),
                                arrayListOf(
                                    VariantModel(450000, "Frame"),
                                    VariantModel(780000, "Blueray"),
                                    VariantModel(648000, "Photo Cromic"),
                                    VariantModel(740000, "Bluecromic")
                                ),
                                binding.etStock.getValue().toInt()
                            )
                            val db = FirebaseFirestore.getInstance()
                            val productRef = db.collection("products") // ⬅️ Simpan dalam koleksi "products"
                            productRef.add(product)
                                .addOnSuccessListener { documentReference ->
                                    hideLoading()
                                    val productId = documentReference.id // 🔥 Dapatkan ID dokumen yang dibuat
                                    println("✅ Product added with ID: $productId")
                                    showToast("Upload Product success")
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    showToast(e.message)
                                    hideLoading()
                                    println("❌ Error adding product: ${e.message}")
                                }
                        }
                }
                .addOnFailureListener { e: java.lang.Exception ->
                    progressDialog.dismiss()
                    showToast("Failed " + e.message)
                }
                .addOnProgressListener { taskSnapshot: UploadTask.TaskSnapshot ->
                    val progress =
                        100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                    progressDialog.setMessage("Uploaded " + progress.toInt() + "%")
                }
        }
    }
    private fun isValidInput(): Boolean {
        val name = binding.etProductName.text.toString().trim()
        val price = binding.etPrice.text.toString().trim()
        val brand = binding.etBrand.text.toString().trim()
        val stock = binding.etStock.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()
        val color = binding.spinnerColor.selectedItem.toString()
        val selectedFrames = categoryFrameAdapter.getSelectedCategories()

        if (filePath == null) {
            showToast("Silakan pilih gambar produk terlebih dahulu.")
            return false
        }
        if (name.isEmpty()) {
            showToast("Nama produk tidak boleh kosong.")
            return false
        }
        if (brand.isEmpty()) {
            showToast("Brand tidak boleh kosong.")
            return false
        }
        if (price.isEmpty() || price.toIntOrNull() == null || price.toInt() <= 0) {
            showToast("Harga harus diisi dan lebih dari 0.")
            return false
        }
        if (stock.isEmpty() || stock.toIntOrNull() == null || stock.toInt() <= 0) {
            showToast("Stok harus diisi dan lebih dari 0.")
            return false
        }
        if (category == "Pilih Kategori") {
            showToast("Pilih kategori produk yang valid.")
            return false
        }
        if (color.isEmpty()) {
            showToast("Pilih warna produk.")
            return false
        }
        if (selectedFrames.isEmpty()) {
            showToast("Pilih minimal satu kategori frame.")
            return false
        }
        return true
    }

}
