package com.optik.sarimbit.app

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.optik.sarimbit.app.adapter.CartAdapter
import com.optik.sarimbit.app.model.CheckoutProduct
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.app.util.CartUpdateListener
import com.optik.sarimbit.app.util.PermissionUtility
import com.optik.sarimbit.app.util.SessionManager
import com.optik.sarimbit.app.util.TimeConverter
import com.optik.sarimbit.databinding.ActivityDetailOrderBinding

class DetailOrderActivity : BaseView(), CartUpdateListener {
    private lateinit var cartAdapter: CartAdapter
    private lateinit var binding: ActivityDetailOrderBinding
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

    val firestore = FirebaseFirestore.getInstance()
    private val cartProducts = mutableListOf<CheckoutProduct>()
    override fun onGetData(intent: Intent?, requestCode: Int) {
        if (requestCode == 100) {
            filePath = intent?.data
            try {
                val bitmap = MediaStore.Images.Media
                    .getBitmap(
                        contentResolver,
                        filePath
                    )
                binding.ivImage.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Pembayaran"
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.addToCart.setOnClickListener {
            if (binding.etName.isTextNotEmpty() && binding.etPhone.isTextNotEmpty() && filePath!= null) {
                uploadImage()
            }
        }// Pass this as listener
        binding.recyclerViewFavorites.layoutManager = LinearLayoutManager(this)
        cartAdapter = CartAdapter(this, cartProducts, this) // Pass this as listener
        binding.recyclerViewFavorites.adapter = cartAdapter
        fetchCartData()
        permissionUtility = PermissionUtility(this, PERMISSIONS)
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        storageReference = storage.reference
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionUtility13 = PermissionUtility(this, PERMISSIONS_13)
        }

        binding.ivImage.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionAos13()
            } else {
                permissionAos()
            }
        }
    }

    override fun onCartUpdated(totalPrice: Double) {
        binding.tvProductTotal.text = "Total: ${convertToCurrency(totalPrice.toLong())}"
    }


    private fun clearCart() {
        cartProducts.clear()
        binding.recyclerViewFavorites.adapter?.notifyDataSetChanged()
    }

    private fun fetchCartData() {
        showLoading("Loading cart items...")

        val userId = SessionManager.getId(this)
        if (userId.isNullOrEmpty()) {
            showToast("User not logged in")
            hideLoading()
            return
        }

        firestore.collection("cart")
            .document(userId)
            .collection("items")
            .whereEqualTo("checkedOut", false)
            .get()
            .addOnSuccessListener { documents ->
                cartProducts.clear() // Kosongkan list sebelum menambahkan data baru
                for (document in documents) {
                    val product = CheckoutProduct(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        image = document.getString("image") ?: "",
                        price = document.getLong("price")?.toInt() ?: 0,
                        quantity = document.getLong("quantity")?.toInt() ?: 1,
                        selectedQuantity = document.getLong("selectedQuantity")?.toInt() ?: 1,
                        brand = document.getString("brand") ?: "",
                        categoryFrame = document.getString("categoryFrame") ?: "",
                        description = document.getString("description") ?: "",
                        frameVariant = document.getString("frameVariant") ?: "",
                        colors = document.getString("colors") ?: "",
                        tags = document["tags"] as? ArrayList<String> ?: arrayListOf()
                    )
                    cartProducts.add(product)
                }
                updateTotalPrice()
                binding.recyclerViewFavorites.adapter?.notifyDataSetChanged()
                hideLoading()
            }
            .addOnFailureListener { e ->
                hideLoading()
                showToast("Failed to load cart: ${e.message}")
            }
    }

    private fun updateTotalPrice() {
        val totalPrice = cartProducts.sumOf { it.price * it.selectedQuantity }
        binding.tvProductTotal.text = "Total: ${convertToCurrency(totalPrice.toLong())}"
    }
    private fun permissionAos13() {
        if (permissionUtility13!!.arePermissionsEnabled()) {
            selectImage()
        } else {
            permissionUtility13!!.requestMultiplePermissions()
        }
    }

    private fun permissionAos() {
        if (permissionUtility!!.arePermissionsEnabled()) {
            selectImage()
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
                            val imageUrl = task.result.toString()
                            val bundle = Bundle()
                            bundle.putString("customerName", binding.etName.getValue())
                            bundle.putString("customerPhone", binding.etPhone.getValue())
                            bundle.putString("imageUrl", imageUrl)
                            goToPage(PaymentActivity::class.java,bundle)
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
}