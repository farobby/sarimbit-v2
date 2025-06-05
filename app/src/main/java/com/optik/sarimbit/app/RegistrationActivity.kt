package com.optik.sarimbit.app

import android.graphics.PorterDuff
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.optik.sarimbit.R
import com.optik.sarimbit.app.model.UserModelFirestore
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.app.util.CallbackFireStore
import com.optik.sarimbit.app.util.SessionManager
import com.optik.sarimbit.databinding.ActivityRegisterPageBinding

class RegistrationActivity : BaseView() {
    private lateinit var binding: ActivityRegisterPageBinding
    private lateinit var auth: FirebaseAuth
    private var tableName = ""
    private var typeUser = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        auth.firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
        typeUser = intent.getIntExtra("typeUser", TYPE_STUDENT)
        binding.tvLogin.setOnClickListener {
            finish()
        }
        binding.btnRegister.setOnClickListener {

            if (binding.etName.isTextNotEmpty() && binding.etEmail.isTextNotEmpty() && binding.etPassword.isTextNotEmpty()) {
                register(binding.etEmail.getValue(), binding.etPassword.getValue())
            }
        }
        binding.ivShowPass.setOnClickListener {
            if (binding.ivShowPass.tag == "0") {
                binding.ivShowPass.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.colorSecondary
                    ), PorterDuff.Mode.SRC_IN
                );
                binding.ivShowPass.tag = "1"
                binding.etPassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            } else {
                binding.ivShowPass.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.light_gray_2
                    ), PorterDuff.Mode.SRC_IN
                );
                binding.ivShowPass.tag = "0"
                binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            }
            binding.etPassword.setSelection(binding.etPassword.getValue().length)
        }

    }

    private fun register(email: String, password: String) {
        showLoading("Registering...")
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        registerData(it.uid)
                    }
                } else {
                    showToast(
                        task.exception?.message.toString()
                    )
                }
            }
    }

    private fun registerData(id: String) {
        val data = HashMap<String, String>()
        data[UserModelFirestore.ID] = id
        data[UserModelFirestore.NAME] = binding.etName.getValue()
        data[UserModelFirestore.EMAIL] = binding.etEmail.getValue()
        firestoreUtil.insertData(
            TABLE_CUSTOMER, id,
            data,
            object : CallbackFireStore.TransactionData {
                override fun onSuccessTransactionData() {
                    SessionManager.setCustData(
                        this@RegistrationActivity,
                        data[UserModelFirestore.NAME], id,
                        data[UserModelFirestore.EMAIL],
                    )
                    hideLoading()
                    goToPageAndClearPrevious(HomeActivity::class.java)
                }

                override fun onFailedTransactionData(message: String?) {
                    hideLoading()
                    showToast(message)
                }

            })
    }

}