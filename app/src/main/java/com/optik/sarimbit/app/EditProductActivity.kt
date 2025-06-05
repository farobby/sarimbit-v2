package com.optik.sarimbit.app
import com.optik.sarimbit.databinding.ActivityEditProductBinding
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.optik.sarimbit.R
import com.optik.sarimbit.app.model.Product
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.app.util.CallbackFireStore
import com.optik.sarimbit.app.util.Constants

class EditProductActivity : BaseView() {

    private lateinit var binding: ActivityEditProductBinding
    private var product: Product? = null
    private var isActive: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val product = getSerializable(intent, "product", Product::class.java)

        product.let {
            binding.etProductName.setText(it.name)
            binding.etPrice.setText(it.price.toString())
            binding.etStock.setText(it.quantity.toString())
            isActive = it.quantity > 0
        }

        binding.btnActive.setOnClickListener {
            isActive = true
            setActiveFilterButton(binding.btnActive)
        }
        binding.btnDelete.setOnClickListener {
            Log.d("btnDeleteproduct id ","--> ${product.id}")
            showLoading("")
            firestoreUtil.deleteDataById("",Constants.TABLE_PRODUCT, product.id, object :
                CallbackFireStore.TransactionData{
                override fun onSuccessTransactionData() {
                    hideLoading()
                    showToast("Delete Berhasil")
                    finish()
                }

                override fun onFailedTransactionData(message: String?) {
                    showToast(message)
                }
            })
        }
        setActiveFilterButton(binding.btnActive)

        binding.btnNonActive.setOnClickListener {
            isActive = false
            setActiveFilterButton(binding.btnNonActive)
        }

        binding.btnSave.setOnClickListener {
            updateProduct()
        }



        binding.btnClose.setOnClickListener {
            finish()
        }
    }
    private fun setActiveFilterButton(activeButton: TextView) {
        val buttons = listOf(binding.btnActive, binding.btnNonActive)
        buttons.forEach { button ->
            if (button == activeButton) {
                button.setTextColor(ContextCompat.getColor(this, R.color.white))
                button.background= ContextCompat.getDrawable(this, R.drawable.bg_tab_selected)
            } else {
                button.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                button.background= ContextCompat.getDrawable(this, R.drawable.bg_tab_unselected)
            }
        }
    }
    private fun updateProduct(){
        showLoading("")
        val product = getSerializable(intent, "product", Product::class.java)
        val name = binding.etProductName.text.toString()
        val price = binding.etPrice.text.toString().toIntOrNull() ?: 0
        val stock = binding.etStock.text.toString().toIntOrNull() ?: 0
        val updates = mapOf(
            Constants.PRODUCT_NAME to name,
            Constants.PRODUCT_PRICE to price,
            Constants.PRODUCT_QUANTITY to stock,
            Constants.PRODUCT_ACTIVE to isActive
        )
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection(Constants.TABLE_PRODUCT).document(product.id)
            .update(updates)
            .addOnSuccessListener {
                hideLoading()
                Toast.makeText(this, "Update Product Berhasil", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                hideLoading()
                Toast.makeText(this, "Gagal menyimpan data ke server", Toast.LENGTH_SHORT).show()
            }
    }

}
